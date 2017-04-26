package com.eluo.project.intranet;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.eluo.project.intranet.leave.Leave;
import com.eluo.project.intranet.main.MainNoticeListData;
import com.eluo.project.intranet.main.MainOutsideListData;
import com.eluo.project.intranet.map.Map;
import com.eluo.project.intranet.meeting.Meeting;
import com.eluo.project.intranet.member.Staff;
import com.eluo.project.intranet.network.NetworkUtil;
import com.eluo.project.intranet.notice.Notice;
import com.eluo.project.intranet.notice.NoticeDetails;
import com.eluo.project.intranet.program.ProgramInformation;
import com.eluo.project.intranet.utils.ThreadPolicy;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


/*
 * Project	    : Eluo Intranet
 * Program    : 표기 하지 않음
 * Description	: 엘루오 씨엔시 인트라넷 앱 메인 화면
 * Environment	:
 * Notes	    : Developed by
 *
 * @(#) MainActivity.java
 * @since 2017-01-19
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-03-13][ggmario@eluocnc.com][CREATE: STATEMENT]
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int REQUEST_CODE_LOCATION = 2; //전화번호 권한 체크시 사용
    private final long FINISH_INTERVAL_TIME = 2000; //뒤로가기 종료 2초
    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE= 5469;  //오버레이(전화 수신 팝업 목적) 다른 앱 위에 표시 되기

    private long backPressedTime = 0;   //뒤로가기 종료
    private Bitmap bmp = null;
    private ListView m_ListView;
    private TextView TextView;
    private ArrayAdapter<String> m_Adapter;
    private String psMid = null;
    private String psMidx = null;
    private String psMpath = null;
    private String psMdept = null;
    private String psMname = null;
    private String sTelephone = null;
    private String  sToken = null;
    private ListView mListView = null;
    private ListViewAdapter mAdapter = null;
    private ListView mListViewOutside = null;
    private ListViewAdapterOutside mAdapterOutside = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_NO_TITLE);  // 화면위 타이틀 없애기
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  // 전체화면 만들기
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // 스레드 생성하고 시작
        new ThreadPolicy();
        if (NetworkUtil.isNetworkConnected(this)) {
            //공지 사항 리스트
            String result = SendByHttp(" ","2"); // 메시지를 서버에 보냄
            String[][] parsedData = jsonParserList1(result); // JSON 데이터 파싱

            m_Adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.main_item_type01);   // Android에서 제공하는 string 문자열 하나를 출력 가능한 layout으로 어댑터 생성
            m_ListView = (ListView) findViewById(R.id.listNotice);  // Xml에서 추가한 ListView 연결
            m_ListView.setAdapter(m_Adapter);    // ListView에 어댑터 연결
            m_ListView.setOnItemClickListener(onClickListItem); // ListView 아이템 터치 시 이벤트

            //커스텀 처리 부분
            mListView = (ListView) findViewById(R.id.listNotice);
            mAdapter = new ListViewAdapter(this);
            mListView.setAdapter(mAdapter);

            if (result.lastIndexOf("RESULT") > 0) {
                mAdapter.addItem("등록된 공지가 없습니다 ","0000-00-00",getResources().getDrawable(R.mipmap.ic_new));
            } else {
                String sTitle = "";
                String sDate = "";
                String sDateInt = "";
                if (parsedData.length > 0 && parsedData != null) {

                    //현재 날짜 구함
                    long now = System.currentTimeMillis();
                    Date date = new Date(now);
                    SimpleDateFormat CurYearFormat = new SimpleDateFormat("yyyy");
                    SimpleDateFormat CurMonthFormat = new SimpleDateFormat("MM");
                    SimpleDateFormat CurDayFormat = new SimpleDateFormat("dd");

                    String strCurYear = CurYearFormat.format(date);
                    String strCurMonth = CurMonthFormat.format(date);
                    String strCurDay = CurDayFormat.format(date);

                    String sIDateTmp ="";
                    sIDateTmp = strCurYear+strCurMonth+strCurDay;
                    for (int i = 0; i < parsedData.length; i++) {
                        if (parsedData[i][1].length() > 26) {
                            sTitle = parsedData[i][1].substring(0, 23) + "...";
                        } else {
                            sTitle = parsedData[i][1];
                        }
                        sDate = parsedData[i][3].substring(0, 4) + "." + parsedData[i][3].substring(5, 7) + "." + parsedData[i][3].substring(8, 10);
                        sDateInt =  parsedData[i][3].substring(0, 4) +parsedData[i][3].substring(5, 7) + parsedData[i][3].substring(8, 10);
                        int iDataDate = Integer.parseInt(sDateInt);
                        int iSysDate = Integer.parseInt(sIDateTmp);
                        if((iSysDate-iDataDate) <= 7 ){
                            mAdapter.addItem(sTitle, sDate,getResources().getDrawable(R.mipmap.ic_new));
                        }else{
                            mAdapter.addItem(sTitle, sDate,null);
                        }
                    }
                }else{
                    Toast.makeText(getApplicationContext(), R.string.network_error_retry, Toast.LENGTH_SHORT).show();
                }
            }
            //휴가  리스트
            result = SendByHttp(" ","3"); // 메시지를 서버에 보냄
            parsedData = jsonParserList2(result); // JSON 데이터 파싱

            // Xml에서 추가한 ListView 연결
            TextView = (TextView) findViewById(R.id.listLeave);
            TextView.setMovementMethod(new ScrollingMovementMethod());  //스크롤

            // ListView에 어댑터 연결
            if (result.lastIndexOf("RESULT") > 0) {
                TextView.setText("등록된 휴가 없습니다");
            } else {
                String tag = "";
                int iTag = 2;
                if(parsedData != null){
                    if(parsedData.length > 0 ) {
                        for (int i = 0; i < parsedData.length; i++) {
                            if (iTag == i) {
                                tag = "\n";
                                iTag = iTag + 3;
                            }
                            TextView.append(" [" + parsedData[i][1] + parsedData[i][3] + "]" + " " + parsedData[i][2] + "  " + tag);
                            tag = "";
                        }
                    }
                }else{
                    Toast.makeText(getApplicationContext(), R.string.network_error_retry, Toast.LENGTH_SHORT).show();
                }
            }
            //외근  리스트
            result = SendByHttp(" ","4"); // 메시지를 서버에 보냄
            parsedData = jsonParserList1(result); // JSON 데이터 파싱

            // Android에서 제공하는 string 문자열 하나를 출력 가능한 layout으로 어댑터 생성
            m_Adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.main_item_type02);

            // Xml에서 추가한 ListView 연결
            m_ListView = (ListView) findViewById(R.id.listGoOut);

            // ListView에 어댑터 연결
            m_ListView.setAdapter(m_Adapter);
//            m_ListView.setOnItemLongClickListener(onClickListItem2);

            mListViewOutside = (ListView) findViewById(R.id.listGoOut);
            mAdapterOutside = new ListViewAdapterOutside(this);
            mListViewOutside.setAdapter(mAdapterOutside);

            if (result.lastIndexOf("RESULT") > 0) {
                mAdapterOutside.addItemOutside("등록된 외근정보가 없습니다", "","");
            } else {
                if (parsedData.length > 0) {
                    for (int i = 0; i < parsedData.length; i++) {
                        mAdapterOutside.addItemOutside(parsedData[i][3], parsedData[i][1], parsedData[i][2]);
                    }
                }else{
                    Toast.makeText(getApplicationContext(), R.string.network_error_retry, Toast.LENGTH_SHORT).show();
                }
            }
        }else{
            Toast.makeText(MainActivity.this, R.string.network_error_retry,Toast.LENGTH_SHORT).show();
        }
        //이미지 넣기
        toolbar.setLogo(R.mipmap.h1_logo); //상단 중안 메뉴바에 이미지 출력
    }

    //비트맵 외부 URL로  변환하는 코드
    public Bitmap getBitmapFromURL(String src) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(src);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }finally{
            if(connection!=null)connection.disconnect();
        }
    }

    //디바이스 전화 번호 가져오기
    public String getPhoneNumber(){
        TelephonyManager mgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        return mgr.getLine1Number();
    }

    @Override
    public void onBackPressed() { //뒤로가기 터치 app 종료
        long tempTime = System.currentTimeMillis(); //시간을 재는거
        long intervalTime = tempTime - backPressedTime;

        //FINISH_INTERVAL_TIME 종료 기능 intervalTime 시간의 숫자값은 매우 커 예외문 수행
        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
        }else {
            //예외에서 담는시간 그리고 한번 더 터치시 생긴 시간과의 차이가 생김 그래서 두번째 터치사 IF문 수행 종료
            backPressedTime = tempTime;

            //toast 부분 custom
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.toast_custom, (ViewGroup) findViewById(R.id.toast_layout_root));
            ImageView image = (ImageView) layout.findViewById(R.id.image);
            image.setImageResource(R.mipmap.h1_logo);
            TextView text = (TextView) layout.findViewById(R.id.text);
            text.setText(R.string.T_exit_meg);
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 900);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            toast.show();
        }
    }

    //옵션 메뉴
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.program_info) {
            if (NetworkUtil.isNetworkConnected(this)) {
                Intent intent = new Intent(MainActivity.this, ProgramInformation.class);//엑티비티 생성 작성 화면
                intent.putExtra("idx", psMidx); //조회 키 값을 넘겨준다
                intent.putExtra("id", psMid);
                intent.putExtra("name", psMname);
                intent.putExtra("path", psMpath);
                intent.putExtra("dept", psMdept);
                intent.putExtra("sTelephone", sTelephone);
                startActivityForResult(intent, 1); // Sub_Activity 호출
                finish();//현재 실행중인 엑티비티 종료(엑티비티가 계속 쌓이게 되면 메모리 및 OS전체 부담을 줌)
            }else{
                Toast.makeText(this, R.string.network_error_chk,Toast.LENGTH_SHORT).show();
            }
        }

        if (id == R.id.action_exit) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);     // 여기서 this는 Activity의 this
            // 여기서 부터는 알림창의 속성 설정
            builder.setTitle(R.string.D_TitleName)        // 제목 설정
                    .setMessage(R.string.D_Question)        // 메세지 설정
                    .setCancelable(false)        // 뒤로 버튼 클릭시 취소 가능 설정
                    .setPositiveButton(R.string.D_Approval, new DialogInterface.OnClickListener(){
                        // 확인 버튼 클릭시 설정
                        public void onClick(DialogInterface dialog, int whichButton){
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.D_Canceled, new DialogInterface.OnClickListener(){
                        // 취소 버튼 클릭시 설정
                        public void onClick(DialogInterface dialog, int whichButton){
                            dialog.cancel();
                        }
                    });
            AlertDialog dialog = builder.create();    // 알림창 객체 생성
            dialog.show();    // 알림창 띄우기
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (NetworkUtil.isNetworkConnected(MainActivity.this)) {
            if (id == R.id.action_home) {         //메인 화면
                Intent intent = new Intent(MainActivity.this, MainActivity.class);//엑티비티 생성 작성 화면
                startActivity(intent);  //액티비티 시작
                finish();//현재 실행중인 엑티비티 종료(엑티비티가 계속 쌓이게 되면 메모리 및 OS전체 부담을 줌)
            } else if (id == R.id.action_notice) {    //공지사항
                Intent intent = new Intent(MainActivity.this, Notice.class);
                intent.putExtra("idx", psMidx); //조회 키 값을 넘겨준다
                intent.putExtra("id", psMid);
                intent.putExtra("name", psMname);
                intent.putExtra("path", psMpath);
                intent.putExtra("dept", psMdept);
                intent.putExtra("sTelephone", sTelephone);
                startActivityForResult(intent, 1); // Sub_Activity 호출
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                finish();
            } else if (id == R.id.action_member) {   //직원 조회
                Intent intent = new Intent(MainActivity.this, Staff.class);
                intent.putExtra("idx", psMidx); //조회 키 값을 넘겨준다
                intent.putExtra("id", psMid);
                intent.putExtra("name", psMname);
                intent.putExtra("path", psMpath);
                intent.putExtra("dept", psMdept);
                intent.putExtra("sTelephone", sTelephone);
                startActivityForResult(intent, 1); // Sub_Activity 호출
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                finish();
            } else if (id == R.id.action_meeting) {   //회의실 예약
                Intent intent = new Intent(MainActivity.this, Meeting.class);
                intent.putExtra("idx", psMidx); //조회 키 값을 넘겨준다
                intent.putExtra("id", psMid);
                intent.putExtra("name", psMname);
                intent.putExtra("path", psMpath);
                intent.putExtra("dept", psMdept);
                intent.putExtra("sTelephone", sTelephone);
                startActivityForResult(intent, 1); // Sub_Activity 호출
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                finish();
            } else if (id == R.id.action_leave) {   //휴가 조회
                Intent intent = new Intent(MainActivity.this, Leave.class);
                intent.putExtra("idx", psMidx); //조회 키 값을 넘겨준다
                intent.putExtra("id", psMid);
                intent.putExtra("name", psMname);
                intent.putExtra("path", psMpath);
                intent.putExtra("dept", psMdept);
                intent.putExtra("sTelephone", sTelephone);
                startActivityForResult(intent, 1); // Sub_Activity 호출
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                finish();
            } else if (id == R.id.action_map) {     //회사 위치
                Intent intent = new Intent(MainActivity.this, Map.class);
                intent.putExtra("idx", psMidx); //조회 키 값을 넘겨준다
                intent.putExtra("id", psMid);
                intent.putExtra("name", psMname);
                intent.putExtra("path", psMpath);
                intent.putExtra("dept", psMdept);
                intent.putExtra("sTelephone", sTelephone);
                startActivityForResult(intent, 1); // Sub_Activity 호출
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                finish();
            } else {
                Log.e("Error", "예외 발생");
            }
        }else{
            Toast.makeText(this, R.string.network_error_chk,Toast.LENGTH_SHORT).show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //오버레이(전화수신 팝업 목적)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE ){
            //Toast.makeText(this, "앱 실행을 위해 오버레이 권한을 설정해야 합니다",Toast.LENGTH_LONG).show();
            if (Build.VERSION.SDK_INT < 23) {

            }else{
                if (Settings.canDrawOverlays(this)) {
                    // You have permission
                    // 오버레이 설정창 이동 후 이벤트 처리합니다.
                  //  onResume();
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);//엑티비티 생성 작성 화면
                    startActivity(intent);  //액티비티 시작
                    finish();//현재 실행중인 엑티비티 종료(엑티비티가 계속 쌓이게 되면 메모리 및 OS전체 부담을 줌)
                }
            }
        }
    }
    //오버레이(전화수신 팝업 목적)
    @TargetApi(Build.VERSION_CODES.M) //M 버전 이상 API를 타겟으로,
    public void PermissionOverlay() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:" + getPackageName())); // 현재 패키지 명을 넘겨 설정화면을 노출하게 됩니다.
        startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
    }

//    자기 자신의 전화 번호 가기 오기 위한 권한 확인 및 오버레이 설정 창 이동(M 마시멜로우 위한 조치)
    public void onResume() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT){
            // 유심이 없는 경우
            Toast.makeText(this, "유심이 없습니다.",Toast.LENGTH_LONG).show();
            finish(); //종료
        } else {
            // 유심이 존재하는 경우
            super.onResume();
            String sVer = "N";
            if (Build.VERSION.SDK_INT < 23) {
                Log.v("info","6.0 마시멜로우 보다 버전이 낮음");
                sVer = "Y";
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M // M 버전(안드로이드 6.0 마시멜로우 버전) 보다 같거나 큰 API에서만 설정창 이동 가능합니다.,
                        && !Settings.canDrawOverlays(this)) { //지금 창이 오버레이 설정창이 아니라면 조건 입니다.
                    PermissionOverlay();
                } else {
                    Log.e("ver_err","버전이 낮거나 오버레이설정창이 아니라면");
                    sVer = "Y";
                }
            }

            if(sVer.equals("Y")){
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)  != PackageManager.PERMISSION_GRANTED) {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)){
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},REQUEST_CODE_LOCATION );
                        Toast.makeText(this, "앱 실행을 위해 전화 관리 권한을 설정해야 합니다",Toast.LENGTH_LONG).show();
                    }else{
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},REQUEST_CODE_LOCATION );
                    }
                }else {
                    Log.i("디바이스 전화번호 : ", getPhoneNumber());

                    if(getPhoneNumber() != null){
                        if(getPhoneNumber().indexOf("+82") == -1){
                            if(getPhoneNumber().length() == 11 ){
                                sTelephone = getPhoneNumber().substring(0, 3) + "-" + getPhoneNumber().substring(3, 7) + "-" + getPhoneNumber().substring(7, 11);
                            }else if (getPhoneNumber().length() == 10) {
                                sTelephone = getPhoneNumber().substring(0, 3) + "-" + getPhoneNumber().substring(3, 6) + "-" + getPhoneNumber().substring(6, 10);
                            }else{
                                Log.e("err82","length err: "+sTelephone);
                            }
                        }else{
                            String sTemp = "0"+getPhoneNumber().substring(3, getPhoneNumber().length());
                            if(sTemp.length() == 11 ){
                                sTelephone = sTemp.substring(0, 3) + "-" + sTemp.substring(3, 7) + "-" + sTemp.substring(7, 11);
                            }else if (sTemp.length() == 10) {
                                sTelephone = sTemp.substring(0, 3) + "-" + sTemp.substring(3, 6) + "-" + sTemp.substring(6, 10);
                            }else{
                                Log.e("err10","length err: "+sTelephone);
                            }
                        }

                    }else{
                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                        alert.setPositiveButton(R.string.D_Approval, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish(); //종료
                            }
                        });
                        alert.setMessage(R.string.D_aspiration_chip);
                        alert.show();
                    }
                }
                if (NetworkUtil.isNetworkConnected(this)) {
                    String sMidx = "";
                    new ThreadPolicy();
                    if(sTelephone != null){
                        if (REQUEST_CODE_LOCATION == 2) {
                            String result = SendByHttp(sTelephone,"1"); // 메시지를 서버에 보냄
                            if (result != null) {
                                String[][] parsedData = jsonParserList(result); // JSON 데이터 파싱
                                if(parsedData != null){
                                    if(parsedData[0][0].length() > 0) {
                                        if (parsedData[0][0].toString().equals("N")) {
                                            sMidx = "ERR";
                                        } else {
                                            sMidx = parsedData[0][0];
                                        }
                                    }
                                }else{
                                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                    alert.setPositiveButton(R.string.D_Approval, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish(); //종료
                                        }
                                    });
                                    alert.setMessage(R.string.network_error_msg);
                                    alert.show();
                                    Log.e("JSON 데이터 파싱 에러", parsedData[0][0].toString());
                                }
                            } else {
                                Log.e("JSON 데이터 파싱 실패", "");
                            }
                        }
                        if(!sMidx.equals("ERR")){
                            new ThreadPolicy();
                            loadScore();
                            Log.d("로그인  되었습니다.",sTelephone);
                            String result = SendByHttp(sTelephone,"1"); // 메시지를 서버에 보냄
                            String[][] parsedData = jsonParserList(result); // JSON 데이터 파싱

                            if (parsedData!= null && parsedData.length  > 0) {
                                for (int i = 0; i < parsedData.length; i++) {
                                    psMidx =parsedData[i][0];
                                    psMpath = parsedData[i][3]+parsedData[i][4];
                                    psMid = parsedData[i][1];
                                    psMdept = parsedData[i][5];
                                    psMname = parsedData[i][2];
                                }
                                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                                navigationView.setNavigationItemSelectedListener(this);
                                View nev_header_view = navigationView.getHeaderView(0);

                                //네비게이터 사진
                                ImageView nav_header_image = (ImageView) nev_header_view.findViewById(R.id.imageView);

                                bmp = getBitmapFromURL(psMpath);
                                int width=(int)(getWindowManager().getDefaultDisplay().getWidth()/6.6); // 가로 사이즈 지정
                                int height=(int)(getWindowManager().getDefaultDisplay().getHeight() * 0.11); // 세로 사이즈 지정
                                Bitmap resizedbitmap=Bitmap.createScaledBitmap(bmp, width, height, true); // 이미지 사이즈 조정
                                nav_header_image.setImageBitmap(resizedbitmap);

                                //네비게이터 이름
                                TextView nav_header_nm_text = (TextView)nev_header_view.findViewById(R.id.loginName);
                                nav_header_nm_text.setText(psMname);
                                //네이게이터에 소속
                                TextView nav_header_id_text = (TextView)nev_header_view.findViewById(R.id.textView);
                                nav_header_id_text.setText(psMdept);
                            }
                        }else{
                            Log.v("전화번호가 일치 하지 않습니다.", "");
                            Log.i("디바이스 전화번호 : ", getPhoneNumber());
                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                            alert.setPositiveButton(R.string.D_Approval, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish(); //종료
                                }
                            });
                            alert.setMessage(R.string.D_account);
                            alert.show();
                        }
                    }
                }else{
                    Toast.makeText(MainActivity.this, R.string.network_error_retry,Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    /**
     * 서버에 데이터를 보내는 메소드
     * @param msg
     * @return
     */
    private String SendByHttp(String msg, String type) {
        if(msg == null)
            msg = "";
        String URL ="";
        if(type.equals("1")){
            URL ="http://www.eluocnc.com/GW_V3/app/loginProc.asp";
        }else{
            URL ="http://www.eluocnc.com/GW_V3/app/bbsList.asp";
        }

        DefaultHttpClient client = new DefaultHttpClient();
        try {
			/* 체크할 id와 pwd값 서버로 전송 */
            String sRear = "";
            if(type.equals("1")){
                if(sToken != null){
                    sRear = "?hp="+msg+"&uugb=2&uuid="+sToken;
                    sToken =null;
                }else{
                    sRear = "?hp="+msg;
                }
            }else{
                if(type.equals("2")){
                    sRear = "?gb=not&page=1&pageUnit=5";
                }else if(type.equals("3")){
                    sRear ="?gb=vac&page=1&pageUnit=5";
                }else if(type.equals("4")){
                    sRear ="?gb=out&page=1&pageUnit=5";
                }else{
                }
            }
            HttpPost post = new HttpPost(URL+sRear);
			/* 지연시간 최대 3초 */
            HttpParams params = client.getParams();
            HttpConnectionParams.setConnectionTimeout(params, 3000);
            HttpConnectionParams.setSoTimeout(params, 3000);

			/* 데이터 보낸 뒤 서버에서 데이터를 받아오는 과정 */
            HttpResponse response = null;
            try{
                ConnectivityManager conManager =(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = conManager.getActiveNetworkInfo();
                if(netInfo != null && netInfo.isConnected()){
                    response = client.execute(post);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            BufferedReader bufreader = new BufferedReader( new InputStreamReader(response.getEntity().getContent(),"utf-8"));
            String line = null;
            String result = "";
            while ((line = bufreader.readLine()) != null) {
                result += line;
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            client.getConnectionManager().shutdown();	// 연결 지연 종료
            Toast.makeText(MainActivity.this, R.string.network_error_retry,Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    /**
     * 받은 JSON 객체를 파싱하는 메소드
     * @param
     * @return
     */
    private String[][] jsonParserList(String pRecvServerPage) {
        Log.i("서버에서 받은 전체 내용 : ", pRecvServerPage);
        try {
            JSONObject json = new JSONObject(pRecvServerPage);
            JSONArray jArr = json.getJSONArray("member");

            // 받아온 pRecvServerPage를 분석하는 부분
            String jsonName[];
            if(pRecvServerPage.lastIndexOf("RESULT") > 0) {
                jsonName = new String[]{"RESULT"};
            }else {
                jsonName = new String[]{"MIDX", "USERID", "USERNM", "USERPATH", "USERIMG", "DEPTNM" };
            }
            String[][] parseredData = new String[jArr.length()][jsonName.length];
            for (int i = 0; i < jArr.length(); i++) {
                json = jArr.getJSONObject(i);
                for(int j = 0; j < jsonName.length; j++) {
                    try{
                        if(parseredData[i][j] == null ){
                            parseredData[i][j] = json.getString(jsonName[j]);
                            Log.i("JSON을 분석한 데이터 " + i + " : ", parseredData[i][j] );
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            // 분해 된 데이터를 확인하기 위한 부분
            for(int i=0; i<parseredData.length; i++){
                String sText = parseredData[i][0];
            }
            return parseredData;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 받은 JSON 객체를 파싱하는 메소드
     * 휴가자 리스트
     * @param
     * @return
     */
    private String[][] jsonParserList1(String pRecvServerPage){
        try {
            JSONObject json = new JSONObject(pRecvServerPage);
            JSONArray jArr = json.getJSONArray("bbs");

            // 받아온 pRecvServerPage를 분석하는 부분
            String jsonName[];
            if(pRecvServerPage.lastIndexOf("RESULT") > 0) {
                jsonName = new String[]{"RESULT"};
            }else {
                jsonName = new String[]{"IDX", "SUBJECT", "REGNM", "REGDT" };
            }
            String[][] parseredData = new String[jArr.length()][jsonName.length];
            for (int i = 0; i < jArr.length(); i++) {
                json = jArr.getJSONObject(i);
                for(int j = 0; j < jsonName.length; j++) {
                    try{
                        if(parseredData[i][j] == null ){
                            parseredData[i][j] = json.getString(jsonName[j]);
                            Log.i("JSON을 분석한 데이터 " + i + " : ", parseredData[i][j] );
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            return parseredData;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 받은 JSON 객체를 파싱하는 메소드
     * 공지 리스트, 외근 리스트
     * @param
     * @return
     */
    private String[][] jsonParserList2(String pRecvServerPage){
        try {
            JSONObject json = new JSONObject(pRecvServerPage);
            JSONArray jArr = json.getJSONArray("bbs");

            // 받아온 pRecvServerPage를 분석하는 부분
            String jsonName[];
            if(pRecvServerPage.lastIndexOf("RESULT") > 0) {
                jsonName = new String[]{"RESULT"};
            }else {
                jsonName = new String[]{"IDX", "SUBJECT", "REGNM", "AP" };
            }
            String[][] parseredData = new String[jArr.length()][jsonName.length];
            for (int i = 0; i < jArr.length(); i++) {
                json = jArr.getJSONObject(i);
                for(int j = 0; j < jsonName.length; j++) {
                    try{
                        if(parseredData[i][j] == null ){
                            parseredData[i][j] = json.getString(jsonName[j]);
                            Log.i("JSON을 분석한 데이터 " + i + " : ", parseredData[i][j] );
                            //sIng = parseredData[i][3]+parseredData[i][4];
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            return parseredData;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 아이템 터치 이벤트
    private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // 이벤트 발생 시 해당 아이템 위치의 텍스트를 출력
        // 스레드 생성하고 시작
        new ThreadPolicy();
        if (NetworkUtil.isNetworkConnected(MainActivity.this)) {
            String sMidx = "";
            int iChoice = arg2;
            String result = SendByHttp(" ","2"); // 메시지를 서버에 보냄
            if (result != null) {
                String[][] parsedData = jsonParserList1(result); // JSON 데이터 파싱
                if(parsedData.length > 0){
                    for (int i = 0; i < parsedData.length; i++) {
                        sMidx = parsedData[iChoice][0];
                        Log.i("JSON을 분석한 데이터 " + i + " : ", parsedData[i][0] );
                    }
                }else {
                    Toast.makeText(MainActivity.this, R.string.network_error_retry,Toast.LENGTH_SHORT).show();
                }
                /*액티비티 호출 하여 새로운 화면 호출 하여 상세 내용 출력*/
                Intent intent = new Intent(MainActivity.this, NoticeDetails.class);//리스트에서 상세 화면으로
                intent.putExtra("_id",sMidx); //조회 키 값을 넘겨준다
                intent.putExtra("idx",psMidx); //조회 키 값을 넘겨준다
                intent.putExtra("id",psMid);
                intent.putExtra("name",psMname);
                intent.putExtra("path",psMpath);
                intent.putExtra("dept",psMdept);
                intent.putExtra("sTelephone",sTelephone);
                startActivityForResult(intent, 1); // Sub_Activity 호출
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                finish();
            }
        }else{
            Toast.makeText(MainActivity.this, R.string.network_error_chk,Toast.LENGTH_SHORT).show();
        }
        }

    };
    /* 프리퍼런스 가져오기*/
    private void loadScore() {
        SharedPreferences pref = getSharedPreferences("PrefName", Activity.MODE_PRIVATE);
        sToken = pref.getString("token", "");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    //길게 눌러 참고:http://gandus.tistory.com/476
    private  AdapterView.OnItemLongClickListener onClickListItem2 = new AdapterView.OnItemLongClickListener(){
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Snackbar.make(view, "외근 등록 하시겠습니까?(개발 중!!!)", Snackbar.LENGTH_LONG).setAction("등록", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }).show();
            return false;
        }
    };

    //공지 리스트 커스텀 처리 부분 시작
    private class ViewHolder {
        public ImageView mIcon;
        public TextView mText;
        public TextView mDate;
    }
    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<MainNoticeListData> mListData = new ArrayList<MainNoticeListData>();

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void addItem(  String mTitle ,String mDate,Drawable icon){
            MainNoticeListData addInfo = null;
            addInfo = new MainNoticeListData();
            addInfo.mIcon = icon;
            addInfo.noticeDate = mDate;
            addInfo.mTitle = mTitle;

            mListData.add(addInfo);
        }

        public void remove(int position){
            mListData.remove(position);
            dataChange();
        }

        public void sort(){
            Collections.sort(mListData, MainNoticeListData.ALPHA_COMPARATOR);
            dataChange();
        }

        public void dataChange(){
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.activity_main_notice_item, null);

                holder.mIcon = (ImageView) convertView.findViewById(R.id.mImage);
                holder.mText = (TextView) convertView.findViewById(R.id.mTitle);
                holder.mDate = (TextView) convertView.findViewById(R.id.noticeDate);

                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            MainNoticeListData mData = mListData.get(position);
            if (mData.mIcon != null) {
                holder.mIcon.setVisibility(View.VISIBLE);
                holder.mIcon.setImageDrawable(mData.mIcon);
            }else{
                holder.mIcon.setVisibility(View.GONE);
            }

            holder.mText.setText(mData.mTitle);
            holder.mDate.setText(mData.noticeDate);
            holder.mDate.setTextSize(19);
            String strColor = "#AAAAAA";
            holder.mDate.setTextColor(Color.parseColor(strColor));
            return convertView;
        }
    }

    //외근 리스트 커스텀 처리 부분 시작
    private class ViewHolderOutside {
        public TextView outsideTitle;
        public TextView outsideDate;
        public TextView outsideName;
    }
    private class ListViewAdapterOutside extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<MainOutsideListData> mListData = new ArrayList<MainOutsideListData>();

        public ListViewAdapterOutside(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void addItemOutside( String mDate, String mTitle, String mName ) {
            MainOutsideListData addInfo = null;
            addInfo = new MainOutsideListData();
            addInfo.mDate = mDate;
            addInfo.mName = mName;
            addInfo.mTitle = mTitle;

            mListData.add(addInfo);
        }

        public void remove(int position) {
            mListData.remove(position);
            dataChange();
        }

        public void sort() {
            Collections.sort(mListData, MainOutsideListData.ALPHA_COMPARATOR);
            dataChange();
        }

        public void dataChange() {
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolderOutside holder;
            if (convertView == null) {
                holder = new ViewHolderOutside();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.activity_main_outside_item, null);

                holder.outsideDate = (TextView) convertView.findViewById(R.id.outsideDate);
                holder.outsideTitle = (TextView) convertView.findViewById(R.id.outsideTitle);
                holder.outsideName = (TextView) convertView.findViewById(R.id.outsideName);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolderOutside) convertView.getTag();
            }
            MainOutsideListData mData = mListData.get(position);

            holder.outsideDate.setText(mData.mDate);
            holder.outsideTitle.setText(mData.mTitle);
            holder.outsideName.setText(mData.mName);
            holder.outsideDate.setTextSize(18);
            String strColor1 = "#4174D9";
            holder.outsideDate.setTextColor(Color.parseColor(strColor1));

            holder.outsideName.setTextSize(18);
            String strColor2 = "#AAAAAA";
            holder.outsideName.setTextColor(Color.parseColor(strColor2));
            return convertView;
        }
    }

}
