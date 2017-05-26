package com.eluo.project.intranet.leave;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.NavigationView;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.eluo.project.intranet.MainActivity;
import com.eluo.project.intranet.R;
import com.eluo.project.intranet.map.Map;
import com.eluo.project.intranet.meeting.Meeting;
import com.eluo.project.intranet.member.Staff;
import com.eluo.project.intranet.network.NetworkUtil;
import com.eluo.project.intranet.notice.Notice;
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

/*
 * Project	    : Eluo Intranet
 * Program    : 표기 하지 않음
 * Description	: 엘루오 씨엔시 인트라넷 앱 휴가 정보
 * Environment	:
 * Notes	    : Developed by
 *
 * @(#) Leave.java
 * @since 2017-03-08
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-03-08][ggmario@eluocnc.com][CREATE: STATEMENT]
 */
public class Leave extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ArrayAdapter<String> m_Adapter;
    private ListView m_ListView;
    private LeaveCustomDialog mCustomDialog;
    private String psMid = null;
    private String psMidx = null;
    private String psMpath = null;
    private String psMdept = null;
    private String psMname = null;
    private String sTelephone = null;
    private static final long MIN_CLICK_INTERVAL=600;
    private long mLastClickTime;

    //private Drawable mDrawable;
    private Bitmap bmp;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(getIntent());

        requestWindowFeature(Window.FEATURE_NO_TITLE);  // 화면위 타이틀 없애기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);  // 전체화면 만들기
        setContentView(R.layout.activity_leave_view_all);   //작성 화면 구성 xml

        // wifi 또는 모바일 네트워크 어느 하나라도 연결이 되어있다면,
        if (NetworkUtil.isNetworkConnected(this)) {
            psMidx = intent.getStringExtra("idx");    // 키값(PRIMARY KEY)
            psMid = intent.getStringExtra("id");
            psMname = intent.getStringExtra("name");
            psMpath = intent.getStringExtra("path");
            psMdept =intent.getStringExtra("dept");
            sTelephone =intent.getStringExtra("sTelephone");

            // 스레드 생성하고 시작
            new ThreadPolicy();
            String result = SendByHttp(psMidx); // 메시지를 서버에 보냄
            // Android에서 제공하는 string 문자열 하나를 출력 가능한 layout으로 어댑터 생성
            m_Adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.leave_item);

            // Xml에서 추가한 ListView 연결
            m_ListView = (ListView) findViewById(R.id.leave_list_view);

            // ListView에 어댑터 연결
            m_ListView.setAdapter(m_Adapter);

            // ListView 아이템 터치 시 이벤트
            m_ListView.setOnItemClickListener(onClickListItem);

            if (result != null) {
                if (result.lastIndexOf("RESULT") > 0) {
                    m_Adapter.add("휴가 내역이 없습니다");
                } else {
                    String[][] parsedData = jsonParserList(result); // JSON 데이터 파싱
                    if(parsedData.length > 0){
                        for (int i = 0; i < parsedData.length; i++) {
                            m_Adapter.add("[신청] " + parsedData[i][4] + "\n[구분] " + parsedData[i][3] + " / " + parsedData[i][6] + " " + parsedData[i][9]);
                        }
                    }
                }
            }else{
                Toast.makeText(getApplicationContext(), R.string.network_error_retry, Toast.LENGTH_SHORT).show();
            }

            //옵션 메뉴
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            if(sTelephone == null) {
                TelephonyManager mgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                sTelephone = mgr.getLine1Number();
                if(sTelephone==null ){
                    AlertDialog.Builder alert = new AlertDialog.Builder(Leave.this);
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
        }else{
            Log.i("연결 안 됨" , "연결이 다시 한번 확인해주세요");
            Toast.makeText(Leave.this, R.string.network_error_chk, Toast.LENGTH_SHORT ).show(); //토스트 알림 메시지 출력
        }
    }

    // 아이템 터치 이벤트
    private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {
        @Override
        public void  onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            int iChoice = arg2;
            long currentClickTime= SystemClock.uptimeMillis();
            long elapsedTime=currentClickTime-mLastClickTime;
            mLastClickTime=currentClickTime;
            // 중복 클릭인 경우
            if(elapsedTime<=MIN_CLICK_INTERVAL){
                return;
            }
            if (NetworkUtil.isNetworkConnected(Leave.this)) {
                String result = SendByHttp(psMidx); // 메시지를 서버에 보냄
                if (result != null) {
                    String[][] parsedData = jsonParserList(result); // JSON 데이터 파싱
                    mCustomDialog = new LeaveCustomDialog(Leave.this,"휴가 정보","[구분]  "+parsedData[iChoice][3]+"\n[시작날짜]  "+parsedData[iChoice][7]+"\n[종료날짜]  "+parsedData[iChoice][8]+"\n[신청]  "+parsedData[iChoice][4]+"\n[일수]  "+parsedData[iChoice][2]+"일\n[승인여부] "+parsedData[iChoice][6]+" "+parsedData[iChoice][9],leftListener,rightListener);
                    mCustomDialog.show();
                }
            }else{
                Toast.makeText(Leave.this, R.string.network_error_chk,Toast.LENGTH_SHORT).show();
            }
        }
        private View.OnClickListener leftListener = new View.OnClickListener() {
            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "왼쪽버튼 클릭",
//                        Toast.LENGTH_SHORT).show();
                mCustomDialog.dismiss();
            }
        };
        private View.OnClickListener rightListener = new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "오른쪽버튼 클릭",
                        Toast.LENGTH_SHORT).show();
            }
        };
    };

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
    /**
     * 서버에 데이터를 보내는 메소드
     * @param msg
     * @return
     */
    private String SendByHttp(String msg) {
        if(msg == null)
            msg = "";
        String URL ="http://www.eluocnc.com/GW_V3/app/myVacList.asp";

        DefaultHttpClient client = new DefaultHttpClient();
        try {
			/* 체크할 id와 pwd값 서버로 전송 */
            HttpPost post = new HttpPost(URL+"?seq="+msg);

			/* 지연시간 최대 5초 */
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
            JSONArray jArr = json.getJSONArray("vacation");

            // 받아온 pRecvServerPage를 분석하는 부분
            String jsonName[];
            if(pRecvServerPage.lastIndexOf("RESULT") > 0) {
                String[] jsonName1 = {"RESULT"};
                jsonName = jsonName1;
            }else{
                String[] jsonName1 = {"VIDX", "DT", "VACCNT", "REASON", "REGDT", "AUTH", "AUTH", "DTS", "DTE","STATE" };
                jsonName = jsonName1;
            }

            String[][] parseredData = new String[jArr.length()][jsonName.length];
            if(parseredData.length > 0){
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
            }
            return parseredData;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
            //noinspection SimplifiableIfStatement
        if (id == R.id.program_info) {
            if (NetworkUtil.isNetworkConnected(this)) {
            Intent intent = new Intent(Leave.this, ProgramInformation.class);//엑티비티 생성 작성 화면
            intent.putExtra("idx", psMidx); //조회 키 값을 넘겨준다
            intent.putExtra("id", psMid);
            intent.putExtra("name", psMname);
            intent.putExtra("path", psMpath);
            intent.putExtra("dept", psMdept);
            intent.putExtra("sTelephone", sTelephone);
            startActivityForResult(intent, 1); // Sub_Activity 호출
            finish();
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
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (NetworkUtil.isNetworkConnected(Leave.this)) {
            if (id == R.id.action_home) {         //메인 화면
                Intent intent = new Intent(Leave.this, MainActivity.class);//엑티비티 생성 작성 화면
                startActivity(intent);  //액티비티 시작
                finish();//현재 실행중인 엑티비티 종료(엑티비티가 계속 쌓이게 되면 메모리 및 OS전체 부담을 줌)
            } else if (id == R.id.action_notice) {    //공지사항
                Intent intent = new Intent(Leave.this, Notice.class);
                intent.putExtra("idx",psMidx); //조회 키 값을 넘겨준다
                intent.putExtra("id",psMid);
                intent.putExtra("name",psMname);
                intent.putExtra("path",psMpath);
                intent.putExtra("dept",psMdept);
                intent.putExtra("sTelephone",sTelephone);
                startActivityForResult(intent, 1); // Sub_Activity 호출
                finish();
            }else if (id == R.id.action_member) {   //직원 조회
                Intent intent = new Intent(Leave.this, Staff.class);
                intent.putExtra("idx",psMidx);
                intent.putExtra("id",psMid);
                intent.putExtra("name",psMname);
                intent.putExtra("path",psMpath);
                intent.putExtra("dept",psMdept);
                intent.putExtra("sTelephone",sTelephone);
                startActivityForResult(intent, 1);
                finish();
            }else if(id == R.id.action_meeting){   //회의실 예약
                Intent intent = new Intent(Leave.this, Meeting.class);
                intent.putExtra("idx",psMidx);
                intent.putExtra("id",psMid);
                intent.putExtra("name",psMname);
                intent.putExtra("path",psMpath);
                intent.putExtra("dept",psMdept);
                intent.putExtra("sTelephone",sTelephone);
                startActivityForResult(intent, 1); // Sub_Activity 호출
                finish();
            }else if(id == R.id.action_leave){   //휴가 조회
                Intent intent = new Intent(Leave.this, Leave.class);
                intent.putExtra("idx",psMidx);
                intent.putExtra("id",psMid);
                intent.putExtra("name",psMname);
                intent.putExtra("path",psMpath);
                intent.putExtra("dept",psMdept);
                intent.putExtra("sTelephone",sTelephone);
                startActivityForResult(intent, 1);
                finish();
            }else if (id == R.id.action_map) {     //회사 위치
                Intent intent = new Intent(Leave.this, Map.class);
                intent.putExtra("idx",psMidx);
                intent.putExtra("id",psMid);
                intent.putExtra("name",psMname);
                intent.putExtra("path",psMpath);
                intent.putExtra("dept",psMdept);
                intent.putExtra("sTelephone",sTelephone);
                startActivityForResult(intent, 1);
                finish();
            }else{
                Log.e("Error","예외 발생");
            }
        }else {
            Toast.makeText(Leave.this, R.string.network_error_chk,Toast.LENGTH_SHORT).show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //스마트 폰에서 뒤로가기 버튼 선택시 처리 이벤트 (클릭시 종료 여부 확인 메시지 처리)
    public boolean onKeyDown( int KeyCode, KeyEvent event ){
        if( KeyCode == KeyEvent.KEYCODE_BACK ){
            ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifi.isConnected() || mobile.isConnected()) {
                Intent intent = new Intent(Leave.this, MainActivity.class);//엑티비티 생성 작성 화면
                startActivity(intent); //엑티비티 시작
                finish();
            }else{
                AlertDialog.Builder alert = new AlertDialog.Builder(Leave.this);
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
