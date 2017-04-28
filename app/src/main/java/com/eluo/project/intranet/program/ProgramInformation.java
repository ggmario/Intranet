package com.eluo.project.intranet.program;

import android.app.AlertDialog;
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
import com.eluo.project.intranet.notice.Notice;
import com.eluo.project.intranet.utils.ThreadPolicy;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by gogumario on 2017-03-13.
 */
/*
 * Project	    : Eluo Intranet
 * Program    : 표기 하지 않음
 * Description	: 엘루오 씨엔시 인트라넷 앱 프로그램 정보
 * Environment	:
 * Notes	    : Developed by
 *
 * @(#) ProgramInformation.java
 * @since 2017-03-13
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-03-13][ggmario@eluocnc.com][CREATE: STATEMENT]
 */
public class ProgramInformation  extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private Bitmap bmp;
    private TextView textView;
    private String psMid = null;
    private String psMidx = null;
    private String psMpath = null;
    private String psMdept = null;
    private String psMname = null;
    private String sTelephone = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(getIntent());

        requestWindowFeature(Window.FEATURE_NO_TITLE);  // 화면위 타이틀 없애기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);  // 전체화면 만들기
        setContentView(R.layout.program_information_all);   //작성 화면 구성 xml

        psMidx = intent.getStringExtra("idx");    // 키값(PRIMARY KEY)
        psMid = intent.getStringExtra("id");
        psMname = intent.getStringExtra("name");
        psMpath = intent.getStringExtra("path");
        psMdept =intent.getStringExtra("dept");
        sTelephone =intent.getStringExtra("sTelephone");

        // 스레드 생성하고 시작
        new ThreadPolicy();

        textView =(TextView)findViewById(R.id.program_infomation);
        textView.setText(R.string.prog_info);
        textView.setMovementMethod(new ScrollingMovementMethod());

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
        View nev_header_view = navigationView.getHeaderView(0);

        ImageView nav_header_image = (ImageView) nev_header_view.findViewById(R.id.imageView);

        bmp = getBitmapFromURL(psMpath);
        int width=(int)(getWindowManager().getDefaultDisplay().getWidth()/6.6); // 가로 사이즈 지정
        int height=(int)(getWindowManager().getDefaultDisplay().getHeight() * 0.11); // 세로 사이즈 지정
        Bitmap resizedbitmap=Bitmap.createScaledBitmap(bmp, width, height, true); // 이미지 사이즈 조정
        nav_header_image.setImageBitmap(resizedbitmap);

        TextView nav_header_nm_text = (TextView)nev_header_view.findViewById(R.id.loginName);
        nav_header_nm_text.setText(psMname);

        TextView nav_header_id_text = (TextView)nev_header_view.findViewById(R.id.textView);
        nav_header_id_text.setText(psMdept);
    }

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

    //메뉴 생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    //옵션 메뉴
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.program_info) {
            Intent intent = new Intent(ProgramInformation.this, ProgramInformation.class);
            intent.putExtra("idx",psMidx);
            intent.putExtra("id",psMid);
            intent.putExtra("name",psMname);
            intent.putExtra("path",psMpath);
            intent.putExtra("dept",psMdept);
            intent.putExtra("sTelephone",sTelephone);
            startActivityForResult(intent, 1); // Sub_Activity 호출
            finish();
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
        int id = item.getItemId();
        if (id == R.id.action_home) {         //메인 화면
            Intent intent = new Intent(ProgramInformation.this, MainActivity.class);//엑티비티 생성 작성 화면
            startActivity(intent);  //액티비티 시작
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
            finish();
        } else if (id == R.id.action_notice) {    //공지사항
            Intent intent = new Intent(ProgramInformation.this, Notice.class);
            intent.putExtra("idx",psMidx); //조회 키 값을 넘겨준다
            intent.putExtra("id",psMid);
            intent.putExtra("name",psMname);
            intent.putExtra("path",psMpath);
            intent.putExtra("dept",psMdept);
            intent.putExtra("sTelephone",sTelephone);
            startActivityForResult(intent, 1); // Sub_Activity 호출
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
            finish();
        }else if (id == R.id.action_member) {   //직원 조회
            Intent intent = new Intent(ProgramInformation.this, Staff.class);
            intent.putExtra("idx",psMidx);
            intent.putExtra("id",psMid);
            intent.putExtra("name",psMname);
            intent.putExtra("path",psMpath);
            intent.putExtra("dept",psMdept);
            intent.putExtra("sTelephone",sTelephone);
            startActivityForResult(intent, 1);
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
            finish();
        }else if(id == R.id.action_meeting){   //회의실 예약
            Intent intent = new Intent(ProgramInformation.this, Meeting.class);
            intent.putExtra("idx",psMidx);
            intent.putExtra("id",psMid);
            intent.putExtra("name",psMname);
            intent.putExtra("path",psMpath);
            intent.putExtra("dept",psMdept);
            intent.putExtra("sTelephone",sTelephone);
            startActivityForResult(intent, 1);
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
            finish();
        }else if(id == R.id.action_leave){   //휴가 조회
            Intent intent = new Intent(ProgramInformation.this, Leave.class);
            intent.putExtra("idx",psMidx);
            intent.putExtra("id",psMid);
            intent.putExtra("name",psMname);
            intent.putExtra("path",psMpath);
            intent.putExtra("dept",psMdept);
            intent.putExtra("sTelephone",sTelephone);
            startActivityForResult(intent, 1);
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
            finish();
        }else if (id == R.id.action_map) {     //회사 위치
            Intent intent = new Intent(ProgramInformation.this, Map.class);
            intent.putExtra("idx",psMidx);
            intent.putExtra("id",psMid);
            intent.putExtra("name",psMname);
            intent.putExtra("path",psMpath);
            intent.putExtra("dept",psMdept);
            intent.putExtra("sTelephone",sTelephone);
            startActivityForResult(intent, 1);
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
            finish();
        }else{
            Log.e("Error","예외 발생");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public boolean onKeyDown( int KeyCode, KeyEvent event ){
        if( KeyCode == KeyEvent.KEYCODE_BACK ){
            if (NetworkUtil.isNetworkConnected(this)) {
                Intent intent = new Intent(ProgramInformation.this, MainActivity.class);//엑티비티 생성 작성 화면
                startActivity(intent); //엑티비티 시작
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                finish();
            }else{
                AlertDialog.Builder alert = new AlertDialog.Builder(ProgramInformation.this);
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
}
