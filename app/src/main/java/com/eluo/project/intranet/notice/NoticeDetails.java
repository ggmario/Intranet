package com.eluo.project.intranet.notice;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.eluo.project.intranet.MainActivity;
import com.eluo.project.intranet.R;
import com.eluo.project.intranet.leave.Leave;
import com.eluo.project.intranet.map.Map;
import com.eluo.project.intranet.meeting.Meeting;
import com.eluo.project.intranet.member.Staff;
import com.eluo.project.intranet.network.NetworkUtil;
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


/*
 * Project	    : Eluo Intranet
 * Program    : 표기 하지 않음
 * Description	: 엘루오 씨엔시 인트라넷 앱 공지 상세
 * Environment	:
 * Notes	    : Developed by
 *
 * @(#) NoticeDetails.java
 * @since 2017-02-19
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-02-19][ggmario@eluocnc.com][CREATE: STATEMENT]
 */


public class NoticeDetails extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener {
    String Id = "";
    private TextView textView, textView3, textView4;
    private Bitmap bmp;
    private String psMid, psMidx, psMpath, psMdept, psMname, sTelephone, sPast = null;
    private String sIDX, sSUBJECT, sCONTENT, sREGNM, sREGDT, sNoData = null;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(getIntent());

        requestWindowFeature(Window.FEATURE_NO_TITLE);  // 화면위 타이틀 없애기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);  // 전체화면 만들기
        setContentView(R.layout.activity_notice_view_all);   //작성 화면 구성 xml

        if (NetworkUtil.isNetworkConnected(this)) {
            Id = intent.getStringExtra("_id");    // 키값(PRIMARY KEY)
            psMidx = intent.getStringExtra("idx");    // 키값(PRIMARY KEY)
            psMid = intent.getStringExtra("id");
            psMname = intent.getStringExtra("name");
            psMpath = intent.getStringExtra("path");
            psMdept =intent.getStringExtra("dept");
            sTelephone =intent.getStringExtra("sTelephone");
            sPast =intent.getStringExtra("sPast");

            new ThreadPolicy();;
            jsonParserList();
            String sContent = "";
            String sName = "";
            String sDate = "";
            String sTitle = "";
            if(sIDX != null){
                sTitle = sSUBJECT;
                sContent = sCONTENT;
                sContent = sContent.replaceAll("&amp;","&");
                sName = sREGNM;
                sDate = sREGDT;
            }
            if (sNoData!= null && sNoData.equals("NO DATA")) {
                textView =(TextView)findViewById(R.id.textWordView);
                textView.setText("");
                //제목
                textView3 =(TextView)findViewById(R.id.textTitleView);
                textView3.setText("데이터 가져오기 실패");
                //내용
                textView4 =(TextView)findViewById(R.id.textContentView);
                textView4.setMovementMethod(new ScrollingMovementMethod());
                textView4.setText("지속적으로 발생시 앱 담당자 문의 해주세요");
            }else{
                //작성자
                textView =(TextView)findViewById(R.id.textWordView);
                textView.setText(sName+" / "+sDate);
                //제목
                textView3 =(TextView)findViewById(R.id.textTitleView);
                textView3.setText(sTitle);
                //내용
                textView4 =(TextView)findViewById(R.id.textContentView);
                textView4.setMovementMethod(new ScrollingMovementMethod());
                textView4.setText(sContent);
            }
            //옵션 메뉴
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            //3G 또는 WiFi 에 연결되어 있을 경우
            View nev_header_view = navigationView.getHeaderView(0);
            //네비게이터 사진
            ImageView nav_header_image = (ImageView) nev_header_view.findViewById(R.id.imageView);
            bmp = getBitmapFromURL(psMpath);
            int width=(int)(getWindowManager().getDefaultDisplay().getWidth()/6.6); // 가로 사이즈 지정
            int height=(int)(getWindowManager().getDefaultDisplay().getHeight() * 0.11); // 세로 사이즈 지정
            Bitmap resizedbitmap=Bitmap.createScaledBitmap(bmp, width, height, true); // 이미지 사이즈 조정
            nav_header_image.setImageBitmap(resizedbitmap);

            //네비게이터 이름
            TextView nav_header_nm_text = (TextView) nev_header_view.findViewById(R.id.loginName);
            nav_header_nm_text.setText(psMname);
            //네이게이터에 소속
            TextView nav_header_id_text = (TextView) nev_header_view.findViewById(R.id.textView);
            nav_header_id_text.setText(psMdept);
        }else {
            Log.i("network error" , "connect error");
            Toast.makeText(NoticeDetails.this, R.string.network_error_chk, Toast.LENGTH_SHORT ).show(); //토스트 알림 메시지 출력
        }
        if( sTelephone == null) {
            TelephonyManager mgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            sTelephone = mgr.getLine1Number();
            if(sTelephone==null){
                AlertDialog.Builder alert = new AlertDialog.Builder(NoticeDetails.this);
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
            if (id == R.id.program_info) {
                if (NetworkUtil.isNetworkConnected(this)) {
                Intent intent = new Intent(NoticeDetails.this, ProgramInformation.class);
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
                    .setPositiveButton(R.string.D_Approval, new DialogInterface.OnClickListener() {
                        // 확인 버튼 클릭시 설정
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.D_Canceled, new DialogInterface.OnClickListener() {
                        // 취소 버튼 클릭시 설정
                        public void onClick(DialogInterface dialog, int whichButton) {
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
        int id = item.getItemId();
        if (NetworkUtil.isNetworkConnected(this)) {
            if (id == R.id.action_home) {         //메인 화면
                Intent intent = new Intent(NoticeDetails.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                finish();
            } else if (id == R.id.action_notice) {    //공지사항
                Intent intent = new Intent(NoticeDetails.this, Notice.class);
                intent.putExtra("idx", psMidx);
                intent.putExtra("id", psMid);
                intent.putExtra("name", psMname);
                intent.putExtra("path", psMpath);
                intent.putExtra("dept", psMdept);
                intent.putExtra("sTelephone", sTelephone);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                finish();
            } else if (id == R.id.action_member) {   //직원 조회
                Intent intent = new Intent(NoticeDetails.this, Staff.class);
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
                Intent intent = new Intent(NoticeDetails.this, Meeting.class);
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
                Intent intent = new Intent(NoticeDetails.this, Leave.class);
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
                Intent intent = new Intent(NoticeDetails.this, Map.class);
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
            Toast.makeText(this, R.string.network_error_chk,Toast.LENGTH_SHORT).show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public boolean onKeyDown( int KeyCode, KeyEvent event ){
        if( KeyCode == KeyEvent.KEYCODE_BACK ){
            if (NetworkUtil.isNetworkConnected(this)) {
                Intent intent  = null;
                if(sPast != null){
                    intent = new Intent(NoticeDetails.this, MainActivity.class);//엑티비티 생성 작성 화면
                }else{
                    intent = new Intent(NoticeDetails.this, Notice.class);//엑티비티 생성 작성 화면
                }
                intent.putExtra("idx",psMidx); //조회 키 값을 넘겨준다
                intent.putExtra("id",psMid);
                intent.putExtra("name",psMname);
                intent.putExtra("path",psMpath);
                intent.putExtra("dept",psMdept);
                intent.putExtra("sTelephone",sTelephone);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                finish();
            }else{
                AlertDialog.Builder alert = new AlertDialog.Builder(NoticeDetails.this);
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

    private String[][] jsonParserList() {
        URL url = null;
        HttpURLConnection urlConnection = null;
        try{
            //웹서버 URL 지정
            url= new URL("http://www.eluocnc.com/GW_V3/app/bbsView.asp?gb=not&idx="+Id);
            HttpURLConnection urlc =(HttpURLConnection)url.openConnection();
            urlc.setConnectTimeout(3000);
            urlc.connect();
        }catch (Exception e){
            FirebaseCrash.report(new Exception("공지 상세 내용 : 서버 연결 실패"));
            return null;
        }
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader bufreader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));
            Log.d("line:",bufreader.toString());
            String line = null;
            String page = "";
            while((line = bufreader.readLine())!=null){
               // line = line.replace("\"엘루오시안\"","엘루오시안");
                Log.d("line:",line);
                page+=line;
            }
            JSONObject json = new JSONObject(page);
            JSONArray jArr = json.getJSONArray("bbs");
            json = jArr.getJSONObject(0);

            sIDX = json.getString("IDX");
            sSUBJECT = json.getString("SUBJECT");
            sCONTENT = json.getString("CONTENT");
            sREGNM = json.getString("REGNM");
            sREGDT= json.getString("REGDT");
            return null;
        } catch (Exception e) {
            sNoData = "NO DATA";
            Log.i("RESULT","데이터가 없음");
            return null;
        }finally{
            urlConnection.disconnect();
        }
    }
}
