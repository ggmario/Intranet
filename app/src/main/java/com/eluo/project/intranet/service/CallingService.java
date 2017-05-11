package com.eluo.project.intranet.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.eluo.project.intranet.R;
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
import java.io.InputStreamReader;

import butterknife.OnClick;

/**
 * Created by gogumario on 2017-03-14.
 */

public class CallingService extends Service {

    public static String EXTRA_CALL_NUMBER = "call_number";
    protected View rootView;
    private String sOverlay = null;

    TextView tv_call_number = null;
    String call_number = "";    //전화번호
    String call_job = "";   //직급
    String call_part = "";  //부서
    String call_nm = "";    //이름
    String call_tb = "";


    WindowManager.LayoutParams params;
    private WindowManager windowManager = null;

    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        int width = (int) (display.getWidth() * 0.9);
        //Display 사이즈의 90%
        params = new WindowManager.LayoutParams(
                width,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON, PixelFormat.TRANSLUCENT);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        rootView = layoutInflater.inflate(R.layout.phone_info, null);
//        ButterKnife.inject(this, rootView);
        tv_call_number = (TextView) rootView.findViewById(R.id.tv_call_info);
        ImageButton b1 = (ImageButton) rootView.findViewById(R.id.btn_close);
        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                removePopup();
            }
        });
        setDraggable();
    }
    //서비스 실행 여부 확인
    public boolean isServiceRunningCheck() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.eluo.project.intranet.service.CallingService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //수신 팝업 드레그
    private void setDraggable() {
        rootView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP: return true;
                    case MotionEvent.ACTION_MOVE: params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        if (rootView != null) windowManager.updateViewLayout(rootView, params);
                        return true;
                } return false;
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!intent.getStringExtra(EXTRA_CALL_NUMBER).equals("") ){
            windowManager.addView(rootView, params);
            loadScore();
            setExtra(intent);
            if (!TextUtils.isEmpty(call_number)) {
//                System.out.println("-------------------------->>"+call_number);
//                System.out.println("-------------------------->>"+call_nm);
//                System.out.println("-------------------------->>"+call_job);
                System.out.println("-------------------------->>"+sOverlay);

                if(!call_nm.equals("") && sOverlay.equals("true")){
                    tv_call_number.setText(call_nm+call_job+"\n"+call_part);
                }else{
                    if(!sOverlay.equals("true") ||  call_nm.equals("")) {
                        call_tb = "Y";
                        removePopup();
                    }
                }
            }
        }else{
            removePopup();
        }
        return START_REDELIVER_INTENT;
    }
    private void setExtra(Intent intent) {
        if (intent == null) {
//            call_tb = "Y";
            removePopup();
            return;
        }else {
            call_number = intent.getStringExtra(EXTRA_CALL_NUMBER);
        }
        // 스레드 생성하고 시작
        new ThreadPolicy();
        if(!call_number.equals("")) {
            loadScore();
            if(sOverlay.equals("true")){
                String result = SendByHttp(call_number); // 메시지를 서버에 보냄
                String[][] parsedData = jsonParserList(result); // JSON 데이터 파싱
                if(parsedData != null){
                    if (parsedData.length > 0) {
                        call_nm = "";
                        call_part = "";
                        call_job = "";
                        call_tb = "Y";
                        if (parsedData[0][0].equals("N")) {
                            removePopup();
                        } else {
                            call_nm = parsedData[0][3];
                            call_part = parsedData[0][6];
                            call_job = parsedData[0][7];
                        }
                    }
                }
            }else{
                removePopup();
            }
        }else{
            if(intent.getStringExtra(EXTRA_CALL_NUMBER).equals("")){
                if(call_tb.equals("Y")){
                    removePopup();
                }
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        removePopup();
    }

    @OnClick(R.id.btn_close)
    public void removePopup() {
        if (rootView != null && windowManager != null && call_tb.equals("Y")) windowManager.removeView(rootView); call_tb="";  //iCount = 0;
    }

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
            for (int i = 0; i < jArr.length(); i++) {
                json = jArr.getJSONObject(i);
                for(int j = 0; j < jsonName.length; j++) {
                    try{
                        if(parseredData[i][j] == null ){
                            parseredData[i][j] = json.getString(jsonName[j]);
                            Log.i("JSON을 분석한 데이터!!!!!!!!!!!! " + i + " : ", parseredData[i][j] );
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

    /* 프리퍼런스 가져오기*/
    private void loadScore() {
        SharedPreferences pref = getSharedPreferences("Overlay", Activity.MODE_PRIVATE);
        sOverlay = pref.getString("switch", "");
    }

}

