package com.eluo.project.intranet.member;

import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.eluo.project.intranet.MainActivity;
import com.eluo.project.intranet.R;
import com.eluo.project.intranet.leave.Leave;
import com.eluo.project.intranet.map.Map;
import com.eluo.project.intranet.meeting.Meeting;
import com.eluo.project.intranet.network.NetworkUtil;
import com.eluo.project.intranet.notice.Notice;
import com.eluo.project.intranet.program.ProgramInformation;
import com.eluo.project.intranet.utils.ThreadPolicy;
import com.eluo.project.intranet.utils.VoiceRecognition;

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
 * Description	: 엘루오 씨엔시 인트라넷 앱 지원 검색 리스트 화면
 * Environment	:
 * Notes	    : Developed by
 *
 * @(#) Staff.java
 * @since 2017-01-21
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-01-21][ggmario@eluocnc.com][CREATE: STATEMENT]
 */
public class Staff  extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener ,TextView.OnEditorActionListener {
    private EditText etMessage;
    private Button btnSend, btnSendMic;
    private Bitmap bmp;

    private String psMid = null;
    private String psMidx = null;
    private String psMpath = null;
    private String psMdept = null;
    private String psMname = null;
    private String sTelephone = null;
    private String psPhone = "";
    private String psViewsConditions = "N";  //조회 상태

    private ListView m_ListView;
    private ArrayAdapter<String> m_Adapter;

    private VoiceRecognition voiceRecognition;

    // 중복 클릭 방지 시간 설정
    private static final long MIN_CLICK_INTERVAL=600;
    private long mLastClickTime;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(getIntent());

        requestWindowFeature(Window.FEATURE_NO_TITLE);  // 화면위 타이틀 없애기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);  // 전체화면 만들기
        setContentView(R.layout.activity_staff_list_all);   //작성 화면 구성 xml

        //옵션 메뉴
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        psMidx = intent.getStringExtra("idx");    // 키값(PRIMARY KEY)
        psMid = intent.getStringExtra("id");
        psMname = intent.getStringExtra("name");
        psMpath = intent.getStringExtra("path");
        psMdept =intent.getStringExtra("dept");
        sTelephone =intent.getStringExtra("sTelephone");

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(sTelephone == null) {
            TelephonyManager mgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            sTelephone = mgr.getLine1Number();
            if(sTelephone==null ){
                AlertDialog.Builder alert = new AlertDialog.Builder(Staff.this);
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
            AlertDialog.Builder alert = new AlertDialog.Builder(Staff.this);
            alert.setPositiveButton(R.string.D_Approval, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish(); //종료
                }
            });
            alert.setMessage(R.string.network_error_msg);
            alert.show();
        }

        //버튼
        etMessage = (EditText) findViewById(R.id.et_message);
        etMessage.setOnEditorActionListener(this); //키패드에 독보기 클릭시 조회 되게 하는...
        btnSend = (Button) findViewById(R.id.btn_sendData);
        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(etMessage.length() == 0){
                    Toast.makeText(Staff.this, R.string.T_search_no, Toast.LENGTH_SHORT ).show(); //토스트 알림 메시지 출력
                }else {
                    if (NetworkUtil.isNetworkConnected(Staff.this)) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);

                        String sMessage = etMessage.getText().toString(); // 보내는 메시지를 받아옴
                        sMessage = sMessage.replaceAll(" ","");/*공백 제거*/
                        String result = SendByHttp(sMessage); // 메시지를 서버에 보냄
                        String[][] parsedData = jsonParserList(result); // JSON 데이터 파싱

                        // Android에서 제공하는 string 문자열 하나를 출력 가능한 layout으로 어댑터 생성
                        m_Adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.staff_item);
                        m_ListView = (ListView) findViewById(R.id.listview);
                        m_ListView.setAdapter(m_Adapter);
                        m_ListView.setOnItemClickListener(onClickListItem);
                        m_ListView.setOnItemLongClickListener(onClickListItem1);

                        if (result.lastIndexOf("RESULT") > 0) {
                            m_Adapter.add("조회 내용이 없습니다");
                            psViewsConditions = "N";
                        } else {
                            if (parsedData.length > 0) {
                                psViewsConditions = "Y";
                                for (int i = 0; i < parsedData.length; i++) {
                                    m_Adapter.add(parsedData[i][3] + " ( " + parsedData[i][7] + " ) / " + parsedData[i][4]);
                                }
                            }else{
                                Toast.makeText(getApplicationContext(), R.string.network_error_retry, Toast.LENGTH_SHORT).show();
                                psViewsConditions = "N";
                            }
                        }
                    } else {
                        Log.i("연결 안 됨" , "연결이 다시 한번 확인해주세요");
                        Toast.makeText(Staff.this, R.string.network_error_chk, Toast.LENGTH_SHORT ).show(); //토스트 알림 메시지 출력
                        psViewsConditions = "N";
                    }
                }
            }
        });

        //음성인식 버튼
        btnSendMic = (Button) findViewById(R.id.btn_sendMic);
        btnSendMic.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) {
                  startVoiceRecognition();
              }
        });
    }

    //키패드에 독보기 클릭시 조회
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // TODO Auto-generated method stub
        //오버라이드한 onEditorAction() 메소드

        if(etMessage.length() == 0){
            Toast.makeText(Staff.this, "검색내용 입력 해주세요", Toast.LENGTH_SHORT ).show(); //토스트 알림 메시지 출력
        }else {
            if (NetworkUtil.isNetworkConnected(this)) {
                //키패드 숨기기
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);

                String sMessage = etMessage.getText().toString(); // 보내는 메시지를 받아옴
                sMessage = sMessage.replaceAll(" ","");/*공백 제거*/
                String result = SendByHttp(sMessage); // 메시지를 서버에 보냄
                String[][] parsedData = jsonParserList(result); // JSON 데이터 파싱

                // Android에서 제공하는 string 문자열 하나를 출력 가능한 layout으로 어댑터 생성
                m_Adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.staff_item);
                m_ListView = (ListView) findViewById(R.id.listview);
                m_ListView.setAdapter(m_Adapter);
                m_ListView.setOnItemClickListener(onClickListItem);
                m_ListView.setOnItemLongClickListener(onClickListItem1);

                if (result.lastIndexOf("RESULT") > 0) {
                    m_Adapter.add("조회 내용이 없습니다");
                    psViewsConditions = "N";
                } else {
                    if (parsedData.length > 0) {
                        psViewsConditions = "Y";
                        for (int i = 0; i < parsedData.length; i++) {
                            m_Adapter.add(parsedData[i][3] + " ( " + parsedData[i][7] + " ) / " + parsedData[i][4]);
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), R.string.network_error_retry, Toast.LENGTH_SHORT).show();
                        psViewsConditions = "N";
                    }
                }
            }else{
                Log.i("연결 안 됨" , "연결이 다시 한번 확인해주세요");
                Toast.makeText(Staff.this, R.string.network_error_chk, Toast.LENGTH_SHORT ).show(); //토스트 알림 메시지 출력
                psViewsConditions = "N";
            }
        }
        return false;
    }

    //길게 눌러 참고:http://gandus.tistory.com/476
    private  AdapterView.OnItemLongClickListener onClickListItem1 = new AdapterView.OnItemLongClickListener(){
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View arg1, int arg2, long arg3) {
        String sPhones ="";
        new ThreadPolicy();             // 스레드 생성하고 시작
        String sMessage = etMessage.getText().toString();
        if(psViewsConditions.equals("Y")){
            int iChoice = arg2;
            String result = SendByHttp(sMessage); // 메시지를 서버에 보냄
            if (result != null) {
                String[][] parsedData = jsonParserList(result); // JSON 데이터 파싱
                if(parsedData.length > 0){
                    for (int i = 0; i < parsedData.length; i++) {
                        sPhones = parsedData[iChoice][4];
                    }
                }
                final  CharSequence info[] = new CharSequence[] {sPhones};
                Intent intent;
                intent = new Intent("android.intent.action.CALL", Uri.parse("tel:"+info[0].subSequence(0,info[0].length())));
                startActivity(intent);
            }
        }
        return true;
        }
    };

    // 아이템 터치 이벤트
    private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            //아이템 중복 실행 방지
            long currentClickTime= SystemClock.uptimeMillis();
            long elapsedTime=currentClickTime-mLastClickTime;
            mLastClickTime=currentClickTime;
            // 중복 클릭인 경우
            if(elapsedTime<=MIN_CLICK_INTERVAL){
                return;
            }
            // 스레드 생성하고 시작
            new ThreadPolicy();
            if (NetworkUtil.isNetworkConnected(Staff.this)) {
                if (psViewsConditions.equals("Y")) {
                    String sMidx = "";
                    String sDept = "";
                    String sEmail = "";
                    String sPhone = "";
                    String sNm = "";
                    String sPosition = "";
                    String sMessage = etMessage.getText().toString();
                    int iChoice = arg2;
                    String result = SendByHttp(sMessage); // 메시지를 서버에 보냄
                    if (result != null) {
                        String[][] parsedData = jsonParserList(result); // JSON 데이터 파싱
                        sMidx = parsedData[iChoice][0];
                        sNm = parsedData[iChoice][3];
                        sPhone = parsedData[iChoice][4];
                        sEmail = parsedData[iChoice][5];
                        sDept = parsedData[iChoice][6];
                        psPhone = parsedData[iChoice][4];
                        sPosition = parsedData[iChoice][7];
                        /*액티비티 호출 하여 새로운 화면 호출 하여 상세 내용 출력*/
                        //                Intent intent = new Intent(Staff.this, StaffDetails.class);//리스트에서 상세 화면으로
                        //                intent.putExtra("_id",sMidx); //조회 키 값을 넘겨준다
                        //                intent.putExtra("idx",psMidx);
                        //                intent.putExtra("id",psMid);
                        //                intent.putExtra("name",psMname);
                        //                intent.putExtra("path",psMpath);
                        //                intent.putExtra("dept",psMdept);
                        //                startActivityForResult(intent, 1); // Sub_Activity 호출
                        //                finish();

                        final CharSequence[] items = {sNm + " " + sPosition + "", sDept, sEmail, sPhone};

                        /*AlertDialog.Builder 를 생성할 때 context 만 넘겨주면 activity 의 theme 를 따르게 된다*/
                        AlertDialog.Builder bld = new AlertDialog.Builder(Staff.this);
                        /* AlertDialog.Builder 를 생성할 때 theme 값을 넣어주면 해당 theme 를 따르게 된다. ICS 기본 테마를 따르게 했다. */
                        //AlertDialog.Builder bld = new AlertDialog.Builder(Staff.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                        final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);  //클립보드 선언
                        bld.setTitle(R.string.employee_information);
                        bld.setItems(items, new DialogInterface.OnClickListener() {    // 목록 클릭시 설정
                            public void onClick(DialogInterface dialog, int index) {
                                clipboardManager.setText(items[index]);//클립보드 담기
                                Toast.makeText(Staff.this, "클립보드 복사", Toast.LENGTH_SHORT).show(); //토스트 알림 메시지 출력
                            }
                        });

                        bld.setPositiveButton(R.string.message, new DialogInterface.OnClickListener() { //문자
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.putExtra("address", items[3]);
                                intent.putExtra("sms_body", "안녕하세요.\n" + psMdept + "부서에 " + psMname + "입니다.");
                                intent.setType("vnd.android-dir/mms-sms");
                                startActivity(intent);
                            }
                        });

                        bld.setNegativeButton(R.string.telephone_connection, new DialogInterface.OnClickListener() {  //통화
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent;
                                intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + psPhone));
                                startActivity(intent);
                            }
                        });
                        bld.show();
                    } else {
                        Toast.makeText(Staff.this, R.string.D_View_again, Toast.LENGTH_SHORT).show(); //토스트 알림 메시지 출력
                    }
                }
            }else{
                Toast.makeText(Staff.this, R.string.network_error_chk, Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * 서버에 데이터를 보내는 메소드
     * @param msg
     * @return
     */
    private String SendByHttp(String msg) {
        if(msg == null)
            msg = "";
        String URL ="http://www.eluocnc.com/GW_V3/app/memberList.asp";

        DefaultHttpClient client = new DefaultHttpClient();
        try {
			/* 체크할 id와 pwd값 서버로 전송 */
            HttpPost post = new HttpPost(URL+"?searchValue="+msg+"&pageUnit=100");
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
                String[] jsonName1 = {"MIDX", "GUBUN", "USERID", "USERNM", "MOBILE", "EMAIL", "PART","JOB"};
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
            }else{
                Toast.makeText(getApplicationContext(), R.string.network_error_retry, Toast.LENGTH_SHORT).show();
            }
            return parseredData;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
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
            if (NetworkUtil.isNetworkConnected(this)) {
                Intent intent = new Intent(Staff.this, MainActivity.class);//엑티비티 생성 작성 화면
                startActivity(intent); //엑티비티 시작
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                finish();
            }else{
                AlertDialog.Builder alert = new AlertDialog.Builder(Staff.this);
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
            if (NetworkUtil.isNetworkConnected(this)) {
                Intent intent = new Intent(Staff.this, ProgramInformation.class);//엑티비티 생성 작성 화면
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
        if (NetworkUtil.isNetworkConnected(Staff.this)) {
            if (id == R.id.action_home) {
                Intent intent = new Intent(Staff.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                finish();
            } else if (id == R.id.action_notice) {    //공지사항
                Intent intent = new Intent(Staff.this, Notice.class);
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
                Intent intent = new Intent(Staff.this, Staff.class);
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
                Intent intent = new Intent(Staff.this, Meeting.class);
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
                Intent intent = new Intent(Staff.this, Leave.class);
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
                Intent intent = new Intent(Staff.this, Map.class);
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
            Toast.makeText(Staff.this, R.string.network_error_chk,Toast.LENGTH_SHORT).show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    // 음성 인식 시작
    private void startVoiceRecognition() {
        voiceRecognition = new VoiceRecognition(Staff.this);
        if (voiceRecognition.recognitionAvailable()) {
            Intent intent = voiceRecognition.getVoiceRecognitionIntent("ELUO 음성 검색");
            startActivityForResult(intent,voiceRecognition.VOICE_RECOGNITION_REQUEST_CODE);
        } else {
            Toast toast = Toast.makeText(Staff.this,"Voice recognition is not available.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    // 음성 인식 결과
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == voiceRecognition.VOICE_RECOGNITION_REQUEST_CODE && resultCode == -1) {
            String result = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS).get(0);
            // 이 부분에서 result 를 가지고 검색을 하거나, 명령을 실행 하면 됨
//            Toast toast = Toast.makeText(Staff.this, result,Toast.LENGTH_SHORT);
            etMessage.setText(result);
            btnSend.callOnClick();
//            toast.show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
