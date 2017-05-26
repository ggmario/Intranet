package com.eluo.project.intranet.meeting;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.eluo.project.intranet.MainActivity;
import com.eluo.project.intranet.R;
import com.eluo.project.intranet.leave.Leave;
import com.eluo.project.intranet.map.Map;
import com.eluo.project.intranet.member.Staff;
import com.eluo.project.intranet.network.NetworkUtil;
import com.eluo.project.intranet.notice.Notice;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/*
 * Project	    : Eluo Intranet
 * Program    : 표기 하지 않음
 * Description	: 엘루오 씨엔시 인트라넷 앱 회의실 예약 정보
 * Environment	:
 * Notes	    : Developed by
 *
 * @(#) Meeting.java
 * @since 2017-03-08
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-03-08][ggmario@eluocnc.com][CREATE: STATEMENT]
 */
public class Meeting  extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ArrayAdapter<String> m_Adapter;
    private ListView m_ListView;
    private Bitmap bmp;

    private String psMid, psMidx, psMpath, psMdept, psMname, sTelephone = null;
    private String sFloor = "7";

    /** Called when the activity is first created. */

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //현재 날짜 구함
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat CurHourFormat = new SimpleDateFormat("HH");
        SimpleDateFormat CurMinuteFormat = new SimpleDateFormat("mm");

        String strCurHour = CurHourFormat.format(date);
        String strCurMinute = CurMinuteFormat.format(date);

        int iToTime = Integer.parseInt(strCurHour);
        int iToMinute = Integer.parseInt(strCurMinute);
        switch (item.getItemId()) {
            case R.id.navigation_home:
                sFloor = "7";
                String[][] parsedData = jsonParserList();
                if(parsedData != null && parsedData.length > 0) {
                    m_Adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.notice_item);
                    m_ListView = (ListView) findViewById(R.id.meeting_list_view);
                    ArrayAdapter adapter7 = (ArrayAdapter) m_ListView.getAdapter();
                    adapter7.notifyDataSetChanged();
                    m_ListView.setAdapter(m_Adapter);

                    // ListView 아이템 터치 시 이벤트
                    m_ListView.setOnItemLongClickListener(onClickListItem1);

                    Resources res = getResources();
                    String[] arrString = res.getStringArray(R.array.meeting_time);
                    int iCk= 0;
                    for(String s:arrString) {
                        for (int i = 0; i < parsedData.length; i++) {
                            if (s.equals(parsedData[i][0])) {
                                if (!parsedData[i][1].equals("")) {
                                    m_Adapter.add(s + "\n" + parsedData[i][1]);
                                    iCk = 1;
                                }
                            }
                        }
                        if(iCk == 0){
                            String sTmp = s.substring(0,2);
                            String sTmp1 = s.substring(3,5);
                            int iTime = Integer.parseInt(sTmp);
                            int iMinute = Integer.parseInt(sTmp1);
                            if(iToTime > iTime) {
                                m_Adapter.add(s+"\n 예약 불가");
                            }else if(iToTime == iTime){
                                if(iToMinute <= iMinute){
                                    m_Adapter.add(s+"\n 예약 가능");
                                }else{
                                    m_Adapter.add(s+"\n 예약 불가");
                                }
                            }else{
                                m_Adapter.add(s+"\n 예약 가능");
                            }
                        }
                        iCk = 0;
                    }
                }else{
                    Log.e("jsonParserList 7:" , "null");
                    Toast.makeText(Meeting.this, R.string.network_error_chk, Toast.LENGTH_SHORT ).show(); //토스트 알림 메시지 출력
                }
                return true;
            case R.id.navigation_dashboard:
                sFloor = "8";
                String[][] parsedData8 = jsonParserList();
                if(parsedData8 != null && parsedData8.length > 0) {
                    m_Adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.notice_item);
                    m_ListView = (ListView) findViewById(R.id.meeting_list_view);
                    ArrayAdapter adapter8 = (ArrayAdapter) m_ListView.getAdapter();
                    adapter8.notifyDataSetChanged();
                    m_ListView.setAdapter(m_Adapter);
                    int icount = 0;
                    for (int i = 0; i < parsedData8.length; i++) {
                        if (!parsedData8[i][2].equals("")) {
                            icount++;
                            Resources res = getResources();
                            String[] arrString = res.getStringArray(R.array.meeting_time);
                            int iCk = 0;
                            for (String s : arrString) {
                                for (int is = 0; is < parsedData8.length; is++) {
                                    if (s.equals(parsedData8[is][0])) {
                                        if (!parsedData8[is][1].equals("")) {
                                            m_Adapter.add(s + "\n" + parsedData8[is][2]);
                                            iCk = 1;
                                        }
                                    }
                                }
                                if (iCk == 0) {
                                    String sTmp = s.substring(0, 2);
                                    String sTmp1 = s.substring(3, 5);
                                    int iTime = Integer.parseInt(sTmp);
                                    int iMinute = Integer.parseInt(sTmp1);
                                    if (iToTime > iTime) {
                                        m_Adapter.add(s + "\n 예약 불가");
                                    } else if (iToTime == iTime) {
                                        if (iToMinute <= iMinute) {
                                            m_Adapter.add(s + "\n 예약 가능");
                                        } else {
                                            m_Adapter.add(s + "\n 예약 불가");
                                        }
                                    } else {
                                        m_Adapter.add(s + "\n 예약 가능");
                                    }
                                }
                                iCk = 0;
                            }
                        } else {
                            if (icount == 0) {
                                m_Adapter.add("예약된 회의실이 없습니다.");
                                icount++;
                            }
                        }
                    }
                    return true;
                }else{
                    Log.e("jsonParserList 8:" , "null");
                    Toast.makeText(getApplicationContext(), R.string.network_error_retry, Toast.LENGTH_SHORT).show();
                }
//                case R.id.navigation_notifications:
//                    Toast.makeText(Meeting.this, "준비 중...", Toast.LENGTH_SHORT ).show(); //토스트 알림 메시지 출력
//                    sFloor = "4";
//                    String result4 = SendByHttp("1");
//                    String[][] parsedData4 = jsonParserList(result4);
//                    m_Adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.notice_item);
//
//                    m_ListView = (ListView) findViewById(R.id.meeting_list_view);
//
//                    ArrayAdapter adapter4 = (ArrayAdapter) m_ListView.getAdapter();
//                    adapter4.notifyDataSetChanged();
//                    m_ListView.setAdapter(m_Adapter);
//                    m_Adapter.add("예약된 회의실이 없습니다.");
//                    return true;
        }
        return false;
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(getIntent());

        requestWindowFeature(Window.FEATURE_NO_TITLE);  // 화면위 타이틀 없애기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);  // 전체화면 만들기
        setContentView(R.layout.activity_meeting_list_all);   //작성 화면 구성 xml

//        Toast.makeText(Meeting.this, mTextMessage, Toast.LENGTH_SHORT ).show(); //토스트 알림 메시지 출력
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //현재 날짜 구함
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat CurHourFormat = new SimpleDateFormat("HH");
        SimpleDateFormat CurMinuteFormat = new SimpleDateFormat("mm");

        String strCurHour = CurHourFormat.format(date);
        String strCurMinute = CurMinuteFormat.format(date);

        int iToTime = Integer.parseInt(strCurHour);
        int iToMinute = Integer.parseInt(strCurMinute);

        //옵션 메뉴
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        if (NetworkUtil.isNetworkConnected(this)) {
            //Floating Action Button(떠다니는 버튼)
//            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.meeting_fab);
//            fab.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    DialogDatePicker();
//                }
//            });

            //엑티비티로 전달 받은 데이터 세팅
            psMidx = intent.getStringExtra("idx");    // 키값(PRIMARY KEY)
            psMid = intent.getStringExtra("id");
            psMname = intent.getStringExtra("name");
            psMpath = intent.getStringExtra("path");
            psMdept =intent.getStringExtra("dept");
            sTelephone =intent.getStringExtra("sTelephone");

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            View nev_header_view = navigationView.getHeaderView(0);

            ImageView nav_header_image = (ImageView) nev_header_view.findViewById(R.id.imageView);
            bmp = getBitmapFromURL(psMpath);
            int width=(int)(getWindowManager().getDefaultDisplay().getWidth()/6.6); // 가로 사이즈 지정
            int height=(int)(getWindowManager().getDefaultDisplay().getHeight() * 0.11); // 세로 사이즈 지정
            Bitmap resizedbitmap=Bitmap.createScaledBitmap(bmp, width, height, true); // 이미지 사이즈 조정
            nav_header_image.setImageBitmap(resizedbitmap);

            TextView nav_header_nm_text = (TextView) nev_header_view.findViewById(R.id.loginName);
            nav_header_nm_text.setText(psMname);

            TextView nav_header_id_text = (TextView) nev_header_view.findViewById(R.id.textView);
            nav_header_id_text.setText(psMdept);

            m_Adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.notice_item);
            m_ListView = (ListView) findViewById(R.id.meeting_list_view);
            m_ListView.setAdapter(m_Adapter);

            m_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ListView listView = (ListView) parent;
                    // TODO 아이템 클릭시에 구현할 내용은 여기에.
                    String item = (String) listView.getItemAtPosition(position);
                    Snackbar.make(view, "["+sFloor+"층 회의실]  "+item,  Snackbar.LENGTH_LONG).setAction("닫기", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            Toast.makeText(Meeting.this, "7층 회의실 예약", Toast.LENGTH_SHORT ).show(); //토스트 알림 메시지 출력
                        }
                    }).show();
                }
            });

            String[][] parsedData = jsonParserList();
            if(parsedData != null && parsedData.length > 0) {
                // ListView 아이템 터치 시 이벤트
                m_ListView.setOnItemLongClickListener(onClickListItem1);
                    if(parsedData != null){
                        if (parsedData.length > 0) {
                            Resources res = getResources();
                            String[] arrString = res.getStringArray(R.array.meeting_time);
                            int iCk= 0;
                            for(String s:arrString) {
                                for (int i = 0; i < parsedData.length; i++) {
                                    if (s.equals(parsedData[i][0])) {
                                        if (!parsedData[i][1].equals("")) {
                                            m_Adapter.add(s + "\n" + parsedData[i][1]);
                                            iCk = 1;
                                        }
                                    }
                                }
                                if(iCk == 0){
                                    String sTmp = s.substring(0,2);
                                    String sTmp1 = s.substring(3,5);
                                    int iTime = Integer.parseInt(sTmp);
                                    int iMinute = Integer.parseInt(sTmp1);
                                    if(iToTime > iTime) {
                                        m_Adapter.add(s+"\n 예약 불가");
                                    }else if(iToTime == iTime){
                                        if(iToMinute <= iMinute){
                                            m_Adapter.add(s+"\n 예약 가능");
                                        }else{
                                            m_Adapter.add(s+"\n 예약 불가");
                                        }
                                    }else{
                                        m_Adapter.add(s+"\n 예약 가능");
                                    }
                                }
                                iCk = 0;
                            }
                        }
                    }else{
                        Log.e("jsonParserList 7:" , "null");
                        Toast.makeText(getApplicationContext(), R.string.network_error_retry, Toast.LENGTH_SHORT).show();
                    }
//                }
            }else{
                Log.i("연결 안 됨" , "연결이 다시 한번 확인해주세요");
                Toast.makeText(Meeting.this, R.string.network_error_chk, Toast.LENGTH_SHORT ).show(); //토스트 알림 메시지 출력
            }
        }else {
            Log.i("연결 안 됨" , "연결이 다시 한번 확인해주세요");
            Toast.makeText(Meeting.this, R.string.network_error_chk, Toast.LENGTH_SHORT ).show(); //토스트 알림 메시지 출력
        }

        if( sTelephone == null) {
            TelephonyManager mgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            sTelephone = mgr.getLine1Number();
            if(sTelephone==null ){
                AlertDialog.Builder alert = new AlertDialog.Builder(Meeting.this);
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
    }

    // 아이템 터치 이벤트
    private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Toast.makeText(Meeting.this, R.string.D_meeting, Toast.LENGTH_SHORT ).show(); //토스트 알림 메시지 출력
        }
    };

    private String[][] jsonParserList() {
        URL url = null;
        HttpURLConnection urlConnection = null;
        try{
            //웹서버 URL 지정
            url= new URL("http://www.eluocnc.com/GW_V3/app/meetList.asp");
            HttpURLConnection urlc =(HttpURLConnection)url.openConnection();
            urlc.setConnectTimeout(3000);
            urlc.connect();
        }catch (Exception e){
            FirebaseCrash.report(new Exception("회의실 예약정보 : 서버 연결 실패"));
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
                page+=line;
            }
            JSONObject json = new JSONObject(page);
            JSONArray jArr = json.getJSONArray("meeting");
            String[][] parseredData = new String[jArr.length()][jArr.length()];
            for (int i=0; i<jArr.length(); i++){
                json = jArr.getJSONObject(i);
                parseredData[i][0] = json.getString("MTIME");
                parseredData[i][1] = json.getString("M7F");
                parseredData[i][2] = json.getString("M8F");
            }
            return parseredData;
        } catch (Exception e) {
            Log.i("RESULT","데이터가 없음");
            return null;
        }finally{
            urlConnection.disconnect();
        }
    }
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
        if (id == R.id.program_info) {
            if (NetworkUtil.isNetworkConnected(this)) {
                Intent intent = new Intent(Meeting.this, ProgramInformation.class);//엑티비티 생성 작성 화면
                intent.putExtra("idx", psMidx); //조회 키 값을 넘겨준다
                intent.putExtra("id", psMid);
                intent.putExtra("name", psMname);
                intent.putExtra("path", psMpath);
                intent.putExtra("dept", psMdept);
                intent.putExtra("sTelephone", sTelephone);
                startActivityForResult(intent, 1); // Sub_Activity 호출
                overridePendingTransition(R.anim.anim_slide_in_top, R.anim.anim_slide_out_bottom);
                finish();
            }else{
                Toast.makeText(this, R.string.network_error_chk,Toast.LENGTH_SHORT).show();
            }
        }
        if(id == R.id.settings){
            if (NetworkUtil.isNetworkConnected(this)) {
                Intent intent = new Intent(this, com.eluo.project.intranet.settings.Settings.class);//엑티비티 생성 작성 화면
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
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        if (NetworkUtil.isNetworkConnected(Meeting.this)) {
            if (id == R.id.action_home) {         //메인 화면
                Intent intent = new Intent(Meeting.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                finish();
            } else if (id == R.id.action_notice) {    //공지사항
                Intent intent = new Intent(Meeting.this, Notice.class);
                intent.putExtra("idx", psMidx); //조회 키 값을 넘겨준다
                intent.putExtra("id", psMid);
                intent.putExtra("name", psMname);
                intent.putExtra("path", psMpath);
                intent.putExtra("dept", psMdept);
                startActivityForResult(intent, 1); // Sub_Activity 호출
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                finish();
            } else if (id == R.id.action_member) {   //직원 조회
                Intent intent = new Intent(Meeting.this, Staff.class);
                intent.putExtra("idx", psMidx);
                intent.putExtra("id", psMid);
                intent.putExtra("name", psMname);
                intent.putExtra("path", psMpath);
                intent.putExtra("dept", psMdept);
                intent.putExtra("sTelephone", sTelephone);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                finish();
            } else if (id == R.id.action_meeting) {   //회의실 예약
                Intent intent = new Intent(Meeting.this, Meeting.class);
                intent.putExtra("idx", psMidx);
                intent.putExtra("id", psMid);
                intent.putExtra("name", psMname);
                intent.putExtra("path", psMpath);
                intent.putExtra("dept", psMdept);
                intent.putExtra("sTelephone", sTelephone);
                startActivityForResult(intent, 1); // Sub_Activity 호출
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                finish();
            } else if (id == R.id.action_leave) {   //휴가 조회
                Intent intent = new Intent(Meeting.this, Leave.class);
                intent.putExtra("idx", psMidx);
                intent.putExtra("id", psMid);
                intent.putExtra("name", psMname);
                intent.putExtra("path", psMpath);
                intent.putExtra("dept", psMdept);
                intent.putExtra("sTelephone", sTelephone);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                finish();
            } else if (id == R.id.action_map) {     //회사 위치
                Intent intent = new Intent(Meeting.this, Map.class);
                intent.putExtra("idx", psMidx);
                intent.putExtra("id", psMid);
                intent.putExtra("name", psMname);
                intent.putExtra("path", psMpath);
                intent.putExtra("dept", psMdept);
                intent.putExtra("sTelephone", sTelephone);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                finish();
            } else {
                Log.e("Error", "예외 발생");
            }
        }else{
            Toast.makeText(Meeting.this, R.string.network_error_chk,Toast.LENGTH_SHORT).show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
    //스마트 폰에서 뒤로가기 버튼 선택시 처리 이벤트 (클릭시 종료 여부 확인 메시지 처리)
    public boolean onKeyDown( int KeyCode, KeyEvent event ){
        if( KeyCode == KeyEvent.KEYCODE_BACK ){
            ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifi.isConnected() || mobile.isConnected()) {
                Intent intent = new Intent(Meeting.this, MainActivity.class);//엑티비티 생성 작성 화면
                startActivity(intent); //엑티비티 시작
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                finish();
            }else{
                AlertDialog.Builder alert = new AlertDialog.Builder(Meeting.this);
                alert.setPositiveButton(R.string.D_Approval, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish(); //종료
                    }
                });
                alert.setMessage(R.string.network_error_msg);
                alert.show();
            }
        }
        return super.onKeyDown( KeyCode, event );
    }

    //길게 눌러 참고:http://gandus.tistory.com/476
    private  AdapterView.OnItemLongClickListener onClickListItem1 = new AdapterView.OnItemLongClickListener(){
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View arg1, int arg2, long arg3) {
            new ThreadPolicy();

            String sPhones ="";
            int iChoice = arg2;

            registerForContextMenu(m_ListView);
//            String result = SendByHttp("1"); // 메시지를 서버에 보냄
//            String[][] parsedData = jsonParserList(result); // JSON 데이터 파싱
//            if (parsedData.length > 0) {
//                for (int i = 0; i < parsedData.length; i++) {
//                    m_Adapter.add(parsedData[i][0] + "\n" + parsedData[i][1]);
//                }
//            }

//            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(Meeting.this);
//            alert_confirm.setMessage("삭제 하시겠습니까?").setCancelable(false).setPositiveButton("삭제",
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            // 'YES'
//                            //DialogTimePicker();
//                        }
//                    }).setNegativeButton("닫기",
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            // 'No'
//                            return;
//                        }
//                    });
//            AlertDialog alert = alert_confirm.create();
//            alert.show();
            return true;
        }
    };

    //달력 호출
    private void DialogDatePicker(){
        Calendar c = Calendar.getInstance();
        int cyear = c.get(Calendar.YEAR);
        int cmonth = c.get(Calendar.MONTH);
        int cday = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            // onDateSet method
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                //선택한 년월일 넘겨주기 위해 가공
                String sMM = null;
                String sDD = null;
                if(String.valueOf(monthOfYear+1).length() == 1){
                    sMM = 0+String.valueOf(monthOfYear+1);
                }else{
                    sMM = String.valueOf(monthOfYear+1);
                }
                if(+String.valueOf(dayOfMonth).length() == 1){
                    sDD = 0+String.valueOf(dayOfMonth);
                }else{
                    sDD =String.valueOf(dayOfMonth);
                }
                String date_selected = String.valueOf(year)+sMM+sDD;
                    Intent intent = new Intent(Meeting.this, MeetingBookingList.class);
                    intent.putExtra("idx",psMidx);
                    intent.putExtra("id",psMid);
                    intent.putExtra("name",psMname);
                    intent.putExtra("path",psMpath);
                    intent.putExtra("dept",psMdept);
                    intent.putExtra("sTelephone",sTelephone);
                    intent.putExtra("sDate",date_selected);
                    startActivityForResult(intent, 1);
                    overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_slide_out_top);
                    finish();
            }
        };
        DatePickerDialog alert = new DatePickerDialog(this,  mDateSetListener, cyear, cmonth, cday);
        alert.show();
    }

    //시간 호출
    private void DialogTimePicker(){
        TimePickerDialog.OnTimeSetListener mTimeSetListener =
                new TimePickerDialog.OnTimeSetListener() {
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Toast.makeText(Meeting.this,
                                "Time is=" + hourOfDay + ":" + minute, Toast.LENGTH_SHORT)
                                .show();
                    }
                };
        TimePickerDialog alert = new TimePickerDialog(this,
                mTimeSetListener, 0, 0, false);
        alert.show();
    }
}
