package com.eluo.project.intranet.member;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.eluo.project.intranet.MainActivity;
import com.eluo.project.intranet.R;
import com.eluo.project.intranet.leave.Leave;
import com.eluo.project.intranet.map.Map;
import com.eluo.project.intranet.meeting.Meeting;
import com.eluo.project.intranet.network.NetworkUtil;
import com.eluo.project.intranet.notice.Notice;
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

/**
 * Created by gogumario on 2017-02-02.
 * 직원관리 상세 정보(사진,이름,ID,부서,직위,전화,메일,기타 정보)
 */

public class StaffDetails extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener ,  View.OnClickListener{
    String Id = "";
    //private Drawable mDrawable;
    private TextView textView, textView2, textView3, textView4,textView5,textView6;
    private Bitmap bmp;
    private String telephone, eMail;
    private Button collPhone, collMail;

    private String psMid = null;
    private String psMidx = null;
    private String psMpath = null;
    private String psMdept = null;
    private String psMname = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(getIntent());

        requestWindowFeature(Window.FEATURE_NO_TITLE);  // 화면위 타이틀 없애기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);  // 전체화면 만들기
        setContentView(R.layout.activity_staff_view_all);   //작성 화면 구성 xml

        Id = intent.getStringExtra("_id");    // 키값(PRIMARY KEY)
        psMidx = intent.getStringExtra("idx");
        psMid = intent.getStringExtra("id");
        psMname = intent.getStringExtra("name");
        psMpath = intent.getStringExtra("path");
        psMdept =intent.getStringExtra("dept");

        // 스레드 생성하고 시작
        new ThreadPolicy();

        String result = SendByHttp(Id); // 메시지를 서버에 보냄
        String sUrl = "";
        String sNm ="";
        String sId = "";
        String sPh ="";
        String sRank = "";
        String sDept = "";
        String sMail = "";
        int ii = 10;
        if (result != null) {
            String[][] parsedData = jsonParserList(result); // JSON 데이터 파싱
            String sTmp1 = "";
            String sTmp2 = "";
            for (int i = 0; i < parsedData.length; i++) {
                for (int iCount = 0; iCount < ii; iCount++) {
                    sTmp1 = parsedData[i][8];
                    sTmp2 = parsedData[i][9];
                    sNm = " "+parsedData[i][3];
                    sId = "  "+parsedData[i][2];
                    sPh = " "+parsedData[i][4];
                    sRank =  "  "+parsedData[i][7];
                    sDept = "  "+parsedData[i][6];
                    sMail = " "+parsedData[i][5];
                    telephone = parsedData[i][4];
                    eMail = parsedData[i][5];
                }
            }
            sUrl = sTmp1+sTmp2;

            //사진 이미지 뷰에 생성
            ImageView image = (ImageView) this.findViewById(R.id.staff_image);
            bmp = getBitmapFromURL(sUrl);
            int width=(int)(getWindowManager().getDefaultDisplay().getWidth()); // 가로 사이즈 지정
            int height=(int)(getWindowManager().getDefaultDisplay().getHeight() * 0.7); // 세로 사이즈 지정
            Bitmap resizedbitmap=Bitmap.createScaledBitmap(bmp, width, height, true); // 이미지 사이즈 조정
            image.setImageBitmap(resizedbitmap); // 이미지뷰에 조정한 이미지 넣기

            //ID
            textView =(TextView)findViewById(R.id.textIdView);
            textView.setText(sId);

            //이름
            textView2 =(TextView)findViewById(R.id.textNameView);
            textView2.setText(sNm);

            //직급
            textView3 =(TextView)findViewById(R.id.textRankView);
            textView3.setText(sRank);

            //부서
            textView4 =(TextView)findViewById(R.id.textDeptView);
            textView4.setText(sDept);

            //전화
            textView5 =(TextView)findViewById(R.id.textPhoneView);
            textView5.setText(sPh);
            collPhone = (Button) findViewById(R.id.coll_ph);
            collPhone.setOnClickListener(this);

            //메일
            textView6 =(TextView)findViewById(R.id.textMailView);
            textView6.setText(sMail);
            collMail =(Button)findViewById(R.id.coll_mail);
            collMail.setOnClickListener(this);

//        textView.append(sRank);
//        //textView.append(sId);
//        textView.append(sDept);

//        textView2 = (TextView)findViewById(R.id.text2_view);
//        textView2.setText(sPh);
//        textView2.append(sMail);

            //보여질 내용일 많아질경우를 위해 스크롤뷰 생성
            //ScrollView sv = new ScrollView(this);
            //스크롤뷰에 텍스트뷰를 붙임
            //sv.addView(textView);
            //스크롤뷰를 액티비티에 붙임
            //setContentView(sv);

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

            String sTelephone = "";
            sTelephone = getPhoneNumber().substring(0, 3) + "-" + getPhoneNumber().substring(3, 7) + "-" + getPhoneNumber().substring(7, 11);
            if(sTelephone.equals("") || sTelephone == null) {
                AlertDialog.Builder alert = new AlertDialog.Builder(StaffDetails.this);
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish(); //종료
                    }
                });
                alert.setMessage("유심을 확인 해주세요");
                alert.show();
            }
            if (NetworkUtil.isNetworkConnected(this)) {
                //3G 또는 WiFi 에 연결되어 있을 경우
                View nev_header_view = navigationView.getHeaderView(0);
                //네비게이터 사진
                ImageView nav_header_image = (ImageView) nev_header_view.findViewById(R.id.imageView);
                bmp = getBitmapFromURL(psMpath);
//                int width=(int)(getWindowManager().getDefaultDisplay().getWidth()/6.6); // 가로 사이즈 지정
//                int height=(int)(getWindowManager().getDefaultDisplay().getHeight() * 0.11); // 세로 사이즈 지정
                Bitmap resizedbitmap1 = Bitmap.createScaledBitmap(bmp, 222, 222, true); // 이미지 사이즈 조정
                nav_header_image.setImageBitmap(resizedbitmap1);

                //네비게이터 이름
                TextView nav_header_nm_text = (TextView) nev_header_view.findViewById(R.id.loginName);
                nav_header_nm_text.setText(psMname);
                //네이게이터에 소속
                TextView nav_header_id_text = (TextView) nev_header_view.findViewById(R.id.textView);
                nav_header_id_text.setText(psMdept);
            }else {
                AlertDialog.Builder alert = new AlertDialog.Builder(StaffDetails.this);
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish(); //종료
                    }
                });
                alert.setMessage("네트워크를 연결 끊어 졌습니다\n확인 후 다시 시도 하세요");
                alert.show();
            }
        }
    }
    //디바이스 전화 번호 가져오기
    public String getPhoneNumber(){
        TelephonyManager mgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        return mgr.getLine1Number();
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
    //@Override
    public void onClick(View v) {
//        mNum = mEditNumber.getText().toString();
        String tel = "tel:" + telephone;
        switch (v.getId()){
            case R.id.coll_ph:
                startActivity(new Intent("android.intent.action.CALL", Uri.parse(tel)));
                break;
            case R.id.coll_mail:
//                startActivity(new Intent("android.intent.action.DIAL", Uri.parse(tel)));
                Intent it = new Intent(Intent.ACTION_SEND);
                it.putExtra(Intent.EXTRA_EMAIL, eMail);
                it.putExtra(Intent.EXTRA_TEXT, "intranet application 에서 메일 보냅니다.");
                it.setType("text/plain");
                startActivity(Intent.createChooser(it, "Choose Email Client"));
                break;
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
            Toast.makeText(this, "앱 정보 (Pre-Alpha 0.3)", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.action_exit) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);     // 여기서 this는 Activity의 this
            // 여기서 부터는 알림창의 속성 설정
            builder.setTitle("알림")        // 제목 설정
                    .setMessage("앱을 종료 하시 겠습니까?")        // 메세지 설정
                    .setCancelable(false)        // 뒤로 버튼 클릭시 취소 가능 설정
                    .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                        // 확인 버튼 클릭시 설정
                        public void onClick(DialogInterface dialog, int whichButton){
                            finish();
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener(){
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

        if (id == R.id.action_home) {         //메인 화면
            Intent intent = new Intent(StaffDetails.this, MainActivity.class);//엑티비티 생성 작성 화면
            startActivity(intent);  //액티비티 시작
            finish();//현재 실행중인 엑티비티 종료(엑티비티가 계속 쌓이게 되면 메모리 및 OS전체 부담을 줌)
        } else if (id == R.id.action_notice) {    //공지사항
            Intent intent = new Intent(StaffDetails.this, Notice.class);
            intent.putExtra("idx",psMidx); //조회 키 값을 넘겨준다
            intent.putExtra("id",psMid);
            intent.putExtra("name",psMname);
            intent.putExtra("path",psMpath);
            intent.putExtra("dept",psMdept);
            startActivityForResult(intent, 1); // Sub_Activity 호출
            finish();
        }else if (id == R.id.action_member) {   //직원 조회
            Intent intent = new Intent(StaffDetails.this, Staff.class);
            intent.putExtra("idx",psMidx);
            intent.putExtra("id",psMid);
            intent.putExtra("name",psMname);
            intent.putExtra("path",psMpath);
            intent.putExtra("dept",psMdept);
            startActivityForResult(intent, 1);
            finish();
        }else if(id == R.id.action_meeting){   //회의실 예약
            Intent intent = new Intent(StaffDetails.this, Meeting.class);
            intent.putExtra("idx",psMidx);
            intent.putExtra("id",psMid);
            intent.putExtra("name",psMname);
            intent.putExtra("path",psMpath);
            intent.putExtra("dept",psMdept);
            startActivityForResult(intent, 1); // Sub_Activity 호출
            finish();
        }else if(id == R.id.action_leave){   //휴가 조회
            Intent intent = new Intent(StaffDetails.this, Leave.class);
            intent.putExtra("idx",psMidx);
            intent.putExtra("id",psMid);
            intent.putExtra("name",psMname);
            intent.putExtra("path",psMpath);
            intent.putExtra("dept",psMdept);
            startActivityForResult(intent, 1);
            finish();
        }else if (id == R.id.action_map) {     //회사 위치
            Intent intent = new Intent(StaffDetails.this, Map.class);
            intent.putExtra("idx",psMidx);
            intent.putExtra("id",psMid);
            intent.putExtra("name",psMname);
            intent.putExtra("path",psMpath);
            intent.putExtra("dept",psMdept);
            startActivityForResult(intent, 1);
            finish();
        }else{
            Log.e("Error","예외 발생");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //스마트 폰에서 뒤로가기 버튼 선택시 처리 이벤트 (클릭시 종료 여부 확인 메시지 처리)
    public boolean onKeyDown( int KeyCode, KeyEvent event ){
        if( KeyCode == KeyEvent.KEYCODE_BACK ){
            Intent intent = new Intent(StaffDetails.this, Staff.class);//엑티비티 생성 작성 화면
            intent.putExtra("idx",psMidx);
            intent.putExtra("id",psMid);
            intent.putExtra("name",psMname);
            intent.putExtra("path",psMpath);
            intent.putExtra("dept",psMdept);
            startActivityForResult(intent, 1);
            finish();
        }
        return super.onKeyDown( KeyCode, event );
    }

    /**
     * 서버에 데이터를 보내는 메소드
     * @param msg
     * @return
     */
    private String SendByHttp(String msg) {
        if(msg == null)
            msg = "";
        String URL ="http://www.eluocnc.com/GW_V3/app/memberView.asp";

        DefaultHttpClient client = new DefaultHttpClient();
        try {
			/* 체크할 id와 pwd값 서버로 전송 */
            HttpPost post = new HttpPost(URL+"?midx="+msg);

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
                String[] jsonName1 = {"RESULT"};
                jsonName = jsonName1;
            }else{
                String[] jsonName1 = {"MIDX", "GUBUN", "USERID", "USERNM", "MOBILE", "EMAIL", "PART","JOB","USERPATH","USERIMG"};
                jsonName = jsonName1;
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
}
