package com.eluo.project.intranet;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
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
    private TextView TextView,TextView1;
    private ArrayAdapter<String> m_Adapter;
    private String psMid, psMidx, psMpath, psMdept, psMname, sTelephone,sToken = null;
    private ListView m_ListView, mListView, mListViewOutside = null;
    private ListViewAdapter mAdapter = null;
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
            //공지사항 더보기 터치시 공지사항 리스트 이동
            TextView1 = (TextView) findViewById(R.id.view_more);
            TextView1.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
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
                    return false;
                }
            });

            m_Adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.main_item_type01);   // Android에서 제공하는 string 문자열 하나를 출력 가능한 layout으로 어댑터 생성
            m_ListView = (ListView) findViewById(R.id.listNotice);  // Xml에서 추가한 ListView 연결
            m_ListView.setAdapter(m_Adapter);    // ListView에 어댑터 연결
            m_ListView.setOnItemClickListener(onClickListItem); // ListView 아이템 터치 시 이벤트

            //커스텀 처리 부분
            mListView = (ListView) findViewById(R.id.listNotice);
            mAdapter = new ListViewAdapter(this);
            mListView.setAdapter(mAdapter);

            //공지 사항 리스트
            String[][] parsedData = jsonParserList("2"); // JSON 데이터 파싱
            if(parsedData != null && parsedData.length > 0) {
                if (parsedData[0][0] == "NO DATA") {
                    Calendar cal = Calendar.getInstance();
                    mAdapter.addItem("데이터 가져오기 실패",+cal.get(Calendar.YEAR)+"-"+cal.get(Calendar.MARCH)+"-"+cal.get(Calendar.DATE),getResources().getDrawable(R.mipmap.ic_new));
                    Toast.makeText(MainActivity.this, "지속적으로 발생시 앱 담당자 문의",Toast.LENGTH_LONG).show();
                }else {
                    String sTitle, sDate, sDateInt = "";
                    //현재 날짜 구함
                    long now = System.currentTimeMillis();
                    Date date = new Date(now);
                    SimpleDateFormat CurYearFormat = new SimpleDateFormat("yyyy");
                    SimpleDateFormat CurMonthFormat = new SimpleDateFormat("MM");
                    SimpleDateFormat CurDayFormat = new SimpleDateFormat("dd");

                    String strCurYear = CurYearFormat.format(date);
                    String strCurMonth = CurMonthFormat.format(date);
                    String strCurDay = CurDayFormat.format(date);

                    String sIDateTmp = "";
                    sIDateTmp = strCurYear + strCurMonth + strCurDay;

                    DateFormat sdFormat = new SimpleDateFormat("yyyyMMdd");
                    Date tempDate = null;
                    Date tempDate2 = null;

                    for (int i = 0; i < parsedData.length; i++) {
                        if (parsedData[i][1].length() > 26) {
                            sTitle = parsedData[i][1].substring(0, 23) + "...";
                        } else {
                            sTitle = parsedData[i][1];
                        }
                        sDate = parsedData[i][3].substring(0, 4) + "." + parsedData[i][3].substring(5, 7) + "." + parsedData[i][3].substring(8, 10);
                        sDateInt = parsedData[i][3].substring(0, 4) + parsedData[i][3].substring(5, 7) + parsedData[i][3].substring(8, 10);
                        try {
                            tempDate = sdFormat.parse(sDateInt);
                            tempDate2 = sdFormat.parse(sIDateTmp);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        long diff = tempDate2.getTime() - tempDate.getTime();
                        long diffDays = diff / (24 * 60 * 60 * 1000);

                        if ((diffDays) <= 7) {
                            mAdapter.addItem(sTitle, sDate, getResources().getDrawable(R.mipmap.ic_new));
                        } else {
                            mAdapter.addItem(sTitle, sDate, null);
                        }
                    }
                }
            }else{
                Calendar cal = Calendar.getInstance();
                mAdapter.addItem("데이터 가져오기 실패",+cal.get(Calendar.YEAR)+"-"+cal.get(Calendar.MARCH)+"-"+cal.get(Calendar.DATE),getResources().getDrawable(R.mipmap.ic_new));
                Toast.makeText(getApplicationContext(), R.string.network_error_retry, Toast.LENGTH_LONG).show();
            }

            //휴가  리스트
            // Xml에서 추가한 ListView 연결
            TextView = (TextView) findViewById(R.id.listLeave);
            TextView.setMovementMethod(new ScrollingMovementMethod());  //스크롤

            // ListView에 어댑터 연결
            String[][] parsedData2 = jsonParserList("3"); // JSON 데이터 파싱
            if(parsedData2 != null && parsedData2.length > 0) {
                String tag = "";
                int iTag = 2;
                if (parsedData2[0][0] == "NO DATA") {
                    TextView.setText("등록된 휴가 없습니다");
                }else{
                    for (int i = 0; i < parsedData2.length; i++) {
                        if (iTag == i) {
                            tag = "\n";
                            iTag = iTag + 3;
                        }
                        TextView.append(" [" + parsedData2[i][1] + parsedData2[i][3] + "]" + " " + parsedData2[i][2] + "  " + tag);
                        tag = "";
                    }
                }
            } else {
                TextView.setText("등록된 휴가 없습니다");
                Toast.makeText(getApplicationContext(), R.string.network_error_retry, Toast.LENGTH_SHORT).show();
            }
            //외근  리스트
            // Android에서 제공하는 string 문자열 하나를 출력 가능한 layout으로 어댑터 생성
            m_Adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.main_item_type02);

            // Xml에서 추가한 ListView 연결
            m_ListView = (ListView) findViewById(R.id.listGoOut);

            // ListView에 어댑터 연결
            m_ListView.setAdapter(m_Adapter);

            mListViewOutside = (ListView) findViewById(R.id.listGoOut);
            mAdapterOutside = new ListViewAdapterOutside(this);
            mListViewOutside.setAdapter(mAdapterOutside);

            String[][] parsedData3 = jsonParserList("4"); // JSON 데이터 파싱
            if(parsedData3 != null && parsedData3.length > 0) {
                if (parsedData3[0][0] == "NO DATA") {
                    mAdapterOutside.addItemOutside("", "등록된 외근 없습니다","");
                }else{
                    for (int i = 0; i < parsedData3.length; i++) {
                        mAdapterOutside.addItemOutside(parsedData3[i][3], parsedData3[i][1], parsedData3[i][2]);
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.network_error_retry, Toast.LENGTH_SHORT).show();
                mAdapterOutside.addItemOutside("", "등록된 외근 없습니다","");
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
    @SuppressLint("MissingPermission")
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

    //메뉴 생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
                overridePendingTransition(R.anim.anim_slide_in_top, R.anim.anim_slide_out_bottom);
                finish();//현재 실행중인 엑티비티 종료(엑티비티가 계속 쌓이게 되면 메모리 및 OS전체 부담을 줌)
            }else{
                Toast.makeText(this, R.string.network_error_chk,Toast.LENGTH_SHORT).show();
            }
        }
        if(id == R.id.settings){
            if (NetworkUtil.isNetworkConnected(this)) {
                Intent intent = new Intent(MainActivity.this, com.eluo.project.intranet.settings.Settings.class);//엑티비티 생성 작성 화면
                intent.putExtra("idx", psMidx); //조회 키 값을 넘겨준다
                intent.putExtra("id", psMid);
                intent.putExtra("name", psMname);
                intent.putExtra("path", psMpath);
                intent.putExtra("dept", psMdept);
                intent.putExtra("sTelephone", sTelephone);
                startActivityForResult(intent, 1); // Sub_Activity 호출
                overridePendingTransition(R.anim.anim_slide_in_top, R.anim.anim_slide_out_bottom);
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

        if (Build.VERSION.SDK_INT == 25) {
            if (isInMultiWindowMode() == true) {
                Toast.makeText(this, R.string.T_multi_windowMode, Toast.LENGTH_SHORT).show();
            }
        }
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT){
            // 유심이 없는 경우
            Toast.makeText(this, R.string.D_aspiration_chip,Toast.LENGTH_LONG).show();
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
                        Toast.makeText(this, R.string.T_app_permissions,Toast.LENGTH_LONG).show();
                    }else{
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},REQUEST_CODE_LOCATION );
                    }
                }else {
                    Log.i("디바이스 전화번호111 : ", getPhoneNumber());
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
                            String[][] parsedData = jsonParserList("1"); // JSON 데이터 파싱
                            if(parsedData != null && parsedData.length > 0) {
                                if(parsedData[0][0].length() > 0) {
                                    if (parsedData[0][0].toString().equals("NO DATA")) {
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
                                Log.e("JSON 데이터 파싱 에러", "");
                            }
                        }
                        if(!sMidx.equals("ERR")){
                            new ThreadPolicy();
                            loadScore();
                            Log.d("로그인  되었습니다.",sTelephone);
                            String[][] parsedData = jsonParserList("1"); // JSON 데이터 파싱
                            if (parsedData!= null && parsedData.length  > 0) {
                                if (parsedData[0][0].toString().equals("NO DATA")) {
                                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                    alert.setPositiveButton(R.string.D_Approval, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish(); //종료
                                        }
                                    });
                                    alert.setMessage(R.string.D_account);
                                    alert.show();
                                } else {
                                    for (int i = 0; i < parsedData.length; i++) {
                                        psMidx = parsedData[i][0];
                                        psMpath = parsedData[i][3] + parsedData[i][4];
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
                                    int width = (int) (getWindowManager().getDefaultDisplay().getWidth() / 6.6); // 가로 사이즈 지정
                                    int height = (int) (getWindowManager().getDefaultDisplay().getHeight() * 0.11); // 세로 사이즈 지정
                                    Bitmap resizedbitmap = Bitmap.createScaledBitmap(bmp, width, height, true); // 이미지 사이즈 조정
                                    nav_header_image.setImageBitmap(resizedbitmap);

                                    //네비게이터 이름
                                    TextView nav_header_nm_text = (TextView) nev_header_view.findViewById(R.id.loginName);
                                    nav_header_nm_text.setText(psMname);
                                    //네이게이터에 소속
                                    TextView nav_header_id_text = (TextView) nev_header_view.findViewById(R.id.textView);
                                    nav_header_id_text.setText(psMdept);
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
                            }
                        }else{
                            Log.v("전화번호가 일치 하지 않습니다.", "");
                            Log.i("디바이스 전화번호222 : ", getPhoneNumber());
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

        //전화 번호, 현재의 셀룰러 네트워크 정보, 진행중인 통화의 상태, 그리고 어떤이의 목록을 포함하여 전화 상태에 대한 액세스 권한 여부 확인
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if(permissionCheck== PackageManager.PERMISSION_DENIED){
            // 권한 없음
            System.out.println("권한이 없음~~");
        }else{
            // 권한 있음
            //getHistory();
        }
    }
    //json으로 서버에서 메인에 보여줄 리스트 가져옴
    private String[][] jsonParserList(String type) {
        URL url = null;
        HttpURLConnection urlConnection = null;
        try{
            //웹서버 URL 지정
            if(type.equals("1")){
                url= new URL("http://www.eluocnc.com/GW_V3/app/loginProc.asp?hp="+sTelephone+"&uugb=2&uuid="+sToken);  // 1번
            }else{
                if(type.equals("2")){
                    url= new URL("http://www.eluocnc.com/GW_V3/app/bbsList.asp?gb=not&page=1&pageUnit=5");
                }else if(type.equals("3")){
                    url= new URL("http://www.eluocnc.com/GW_V3/app/bbsList.asp?gb=vac&page=1&pageUnit=5");
                }else if(type.equals("4")) {
                    url = new URL("http://www.eluocnc.com/GW_V3/app/bbsList.asp?gb=out&page=1&pageUnit=5");
                }else{

                }
            }
            HttpURLConnection urlc =(HttpURLConnection)url.openConnection();
            urlc.setConnectTimeout(3000);
            urlc.connect();
        }catch (Exception e){
            FirebaseCrash.report(new Exception("메인  리스트 : 서버 연결 실패"));
            return null;
        }

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader bufreader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));
            Log.d("line:",bufreader.toString());
            String line = null;
            String page = "";
            while((line = bufreader.readLine())!=null){
                Log.d("line:",line);
//                line = line.replace("\"엘루오시안\"","엘루오시안");
//                line = line.replace("?","");
                page+=line;
                Log.d("line:",line);
            }
            JSONArray jArr = null;
            String[][] parseredData = null;
            JSONObject json = new JSONObject(page);
            if(type.equals("1")) {
                jArr = json.getJSONArray("member");
                parseredData = new String[jArr.length()][6];
            }else{
                jArr = json.getJSONArray("bbs");
                parseredData = new String[jArr.length()][4];
            }

            if(type.equals("1")){
                for (int i=0; i<jArr.length(); i++){
                    json = jArr.getJSONObject(i);
                    parseredData[i][0] = json.getString("MIDX");
                    parseredData[i][1] = json.getString("USERID");
                    parseredData[i][2] = json.getString("USERNM");
                    parseredData[i][3] = json.getString("USERPATH");
                    parseredData[i][4] = json.getString("USERIMG");
                    parseredData[i][5] = json.getString("DEPTNM");
                }
                return parseredData;
            }else{
                if(type.equals("2")||type.equals("4")){
                    for (int i=0; i<jArr.length(); i++){
                        json = jArr.getJSONObject(i);
                        parseredData[i][0] = json.getString("IDX");
                        parseredData[i][1] = json.getString("SUBJECT");
                        parseredData[i][2] = json.getString("REGNM");
                        parseredData[i][3] = json.getString("REGDT");
                    }
                    return parseredData;
                }else{
                    for (int i=0; i<jArr.length(); i++){
                        json = jArr.getJSONObject(i);
                        parseredData[i][0] = json.getString("IDX");
                        parseredData[i][1] = json.getString("SUBJECT");
                        parseredData[i][2] = json.getString("REGNM");
                        parseredData[i][3] = json.getString("AP");
                    }
                    return parseredData;
                }
            }
        } catch (Exception e) {
            String[][] parseredData = new String[1][1];
            parseredData[0][0] = "NO DATA";
            Log.i("RESULT","데이터가 없음");
            return parseredData;
        }finally{
            urlConnection.disconnect();
        }
    }
    // 아이템 터치 이벤트
    private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // 이벤트 발생 시 해당 아이템 위치의 텍스트를 출력
        new ThreadPolicy();
        if (NetworkUtil.isNetworkConnected(MainActivity.this)) {
            String sMidx = "";
            int iChoice = arg2;
            String[][] parsedData = jsonParserList("2"); // JSON 데이터 파싱
            if(parsedData != null && parsedData.length > 0) {
                if (parsedData[0][0] != "NO DATA") {
                    for (int i = 0; i < parsedData.length; i++) {
                        sMidx = parsedData[iChoice][0];
                        Log.i("JSON을 분석한 데이터 " + i + " : ", parsedData[i][0] );
                    }
                    Intent intent = new Intent(MainActivity.this, NoticeDetails.class);//리스트에서 상세 화면으로
                    intent.putExtra("_id",sMidx); //조회 키 값을 넘겨준다
                    intent.putExtra("idx",psMidx); //조회 키 값을 넘겨준다
                    intent.putExtra("id",psMid);
                    intent.putExtra("name",psMname);
                    intent.putExtra("path",psMpath);
                    intent.putExtra("dept",psMdept);
                    intent.putExtra("sTelephone",sTelephone);
                    intent.putExtra("sPast","M");
                    startActivityForResult(intent, 1); // Sub_Activity 호출
                    overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                    finish();
                }else{
                    Toast.makeText(MainActivity.this, R.string.T_no_data,Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(MainActivity.this, R.string.network_error_retry,Toast.LENGTH_SHORT).show();
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
