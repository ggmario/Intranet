/*
 * Project	    : Eluo Intranet
 * Program    : 표기 하지 않음
 * Description	: 엘루오 씨엔시 서비스(전화 수신 서비스)
 * Environment	:
 * Notes	    : Developed by
 *
 * @(#) CallingService.java
 * @since 2017-03-14
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-03-14][ggmario@eluocnc.com][CREATE: STATEMENT]
 */
package com.eluo.project.intranet.service;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
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
import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
    String call_midx = "";
    String sReception = "";
    String sMissedType = "";
    String sExtraCallNumber="";

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
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        if (rootView != null) windowManager.updateViewLayout(rootView, params);
                        return true;
                }
                return false;
            }
        });
    }
//부재
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sExtraCallNumber = intent.getStringExtra(EXTRA_CALL_NUMBER);
        if (!sExtraCallNumber.equals("") && !sExtraCallNumber.equals("A")) {    //전화벨 울리는 중...
            sExtraCallNumber = "";
            windowManager.addView(rootView, params);
            loadScore();
            setExtra(intent);
            if (!TextUtils.isEmpty(call_number)) {
                if (!call_nm.equals("") && (sOverlay.equals("true") || sOverlay.equals(""))) {
                    tv_call_number.setText(call_nm + call_job + "\n" + call_part);
                } else {
                    if (sOverlay.equals("false") || call_nm.equals("")) {
                        call_tb = "Y";
                        removePopup();
                    }
                }
            }
        } else {
            //removePopup();
            if(sExtraCallNumber.equals("A") ){ //전화 벨이 종료 통화 종료시..
                sExtraCallNumber ="";
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run(){
                        getHistory();
                        String[][] parsedData = jsonParserList(); // JSON 데이터 파싱
                        if (parsedData != null && parsedData.length > 0) {
                            call_nm = "";
                            call_part = "";
                            call_job = "";
                            if (parsedData[0][0] == "NO DATA") {
                                if(call_tb.equals("Y")) {
                                    removePopup();
                                    call_tb = "";
                                }
                            } else {
                                call_nm = parsedData[0][3];
                                call_part = parsedData[0][6];
                                call_job = parsedData[0][7];
                                call_midx = parsedData[0][0];

                                if(sReception.equals("1")) {
                                    call_tb = "Y";
                                    NotificationSomethings(call_midx);
                                }
                                if(call_tb.equals("Y")) {
                                    removePopup();
                                    call_tb = "";
                                }
                            }
                        }
                    }
                }, 1000);
            }
        }
        return START_REDELIVER_INTENT;
    }

    private void setExtra(Intent intent) {
        if (intent == null) {
//            call_tb = "Y";
            removePopup();
            return;
        } else {
            call_number = intent.getStringExtra(EXTRA_CALL_NUMBER);
        }
        // 스레드 생성하고 시작
        new ThreadPolicy();
        if (!call_number.equals("")) {
            loadScore();
            if (sOverlay.equals("true") || sOverlay.equals("")) {   //오버레이 표기 여부
                String[][] parsedData = jsonParserList(); // JSON 데이터 파싱
                if (parsedData != null && parsedData.length > 0) {
                    call_nm = "";
                    call_part = "";
                    call_job = "";
                    if (parsedData[0][0] == "NO DATA") {
                        if (call_tb.equals("Y")) {
                            removePopup();
                            call_tb = "";
                        }
                    } else {
                        call_nm = parsedData[0][3];
                        call_part = parsedData[0][6];
                        call_job = parsedData[0][7];
                        call_tb = "Y";
                    }
                }
            } else {
                if (call_tb.equals("Y")) {
                    removePopup();
                    call_tb = "";
                }
            }
        } else {
            if (intent.getStringExtra(EXTRA_CALL_NUMBER).equals("")) {
                if (call_tb.equals("Y")) {
                    removePopup();
                    call_tb = "";
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
        if (rootView != null && windowManager != null && call_tb.equals("Y"))
            windowManager.removeView(rootView);
        call_tb = "";  //iCount = 0;
        stopSelf();
    }


    private String[][] jsonParserList() {
        URL url = null;
        HttpURLConnection urlConnection = null;
        try {
            //웹서버 URL 지정
            Log.i("call_number:", call_number);
            url = new URL("http://www.eluocnc.com/GW_V3/app/memberList.asp?searchValue=" + call_number + "&pageUnit=100");
//            url = new URL("http://www.eluocnc.com/GW_V3/app/memberList.asp?searchValue=010-6248-3985&pageUnit=100");
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setConnectTimeout(3000);
            urlc.connect();
        } catch (Exception e) {
            FirebaseCrash.report(new Exception("수신전화 정보조회 : 서버 연결 실패:"+call_number));
            return null;
        }

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader bufreader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            Log.d("line:", bufreader.toString());
            String line = null;
            String page = "";
            while ((line = bufreader.readLine()) != null) {
                Log.d("line:", line);
                page += line;
            }
            JSONObject json = new JSONObject(page);
            JSONArray jArr = json.getJSONArray("member");
            String[][] parseredData = new String[jArr.length()][8];
            for (int i = 0; i < jArr.length(); i++) {
                json = jArr.getJSONObject(i);
                parseredData[i][0] = json.getString("MIDX");
                parseredData[i][1] = json.getString("GUBUN");
                parseredData[i][2] = json.getString("USERID");
                parseredData[i][3] = json.getString("USERNM");
                parseredData[i][4] = json.getString("MOBILE");
                parseredData[i][5] = json.getString("EMAIL");
                parseredData[i][6] = json.getString("PART");
                parseredData[i][7] = json.getString("JOB");
            }
            return parseredData;
        } catch (Exception e) {
            String[][] parseredData = new String[1][1];
            parseredData[0][0] = "NO DATA";
            Log.i("RESULT", "데이터가 없음");
            return parseredData;
        } finally {
            urlConnection.disconnect();
        }
    }

    /* 프리퍼런스 가져오기*/
    private void loadScore() {
        SharedPreferences pref = getSharedPreferences("Overlay", Activity.MODE_PRIVATE);
        sOverlay = pref.getString("switch", "");
    }

    private void getHistory() {
        String[] projection = {CallLog.Calls.CONTENT_TYPE, CallLog.Calls.NUMBER, CallLog.Calls.DURATION, CallLog.Calls.DATE};
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Cursor cur = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, CallLog.Calls.TYPE + "= ?", new String[]{String.valueOf(CallLog.Calls.MISSED_TYPE)}, CallLog.Calls.DEFAULT_SORT_ORDER);
        Log.d("db count=", String.valueOf(cur.getCount()));
        Log.d("db count=", CallLog.Calls.CONTENT_ITEM_TYPE);
        Log.d("db count=", CallLog.Calls.CONTENT_TYPE);
        Log.d("db count=", CallLog.Calls.DATE);
        int ii = 0;
        if(cur.moveToFirst() && cur.getCount() > 0) {
            while(cur.isAfterLast() == false) {
                StringBuffer sb = new StringBuffer();
            if(ii == 0){
                String sTemp = String.valueOf(sb.append("").append(cur.getString(cur.getColumnIndex(CallLog.Calls.NUMBER))));
                if(sTemp.length()  == 11){
                    call_number = sTemp.substring(0,3)+"-"+sTemp.substring(3,7)+"-"+sTemp.substring(7,11);
                }else if(sTemp.length() == 10){
                    call_number = sTemp.substring(0,3)+"-"+sTemp.substring(3,6)+"-"+sTemp.substring(6,10);
                }else{
                }
                sReception = cur.getString(cur.getColumnIndex(CallLog.Calls.NEW));
                sMissedType = cur.getString(cur.getColumnIndex(CallLog.Calls.TYPE));
            }
                sb.append("call type=").append(cur.getString(cur.getColumnIndex(CallLog.Calls.TYPE)));
                sb.append(", cashed name=").append(cur.getString(cur.getColumnIndex(CallLog.Calls.CACHED_NAME)));
                sb.append(", content number=").append(cur.getString(cur.getColumnIndex(CallLog.Calls.NUMBER)));
                sb.append(", duration=").append(cur.getString(cur.getColumnIndex(CallLog.Calls.DURATION)));
                sb.append(", new=").append(cur.getString(cur.getColumnIndex(CallLog.Calls.NEW)));
//                sb.append(", date=").append(timeToString(cur.getLong(cur.getColumnIndex(CallLog.Calls.DATE)))).append("]");
                cur.moveToNext();
                ii++;
            }
        }
    }

    public void NotificationSomethings(String call_midx) {
        Resources res = getResources();
        Intent notificationIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(call_number));

        notificationIntent.putExtra("notificationId", 9999); //전달할 값
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("Eluo 부재중 전화")
                .setContentText(call_nm+" "+call_job+" "+call_part)
                .setTicker("애타게 전화를 기다리고 있을 꺼에요~")
                .setSmallIcon(R.mipmap.eluo_icon)   //아이콘
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.eluo_icon))
                .setContentIntent(contentIntent)//터치시 이동 할 앱 메뉴
                .setAutoCancel(true)    //터치시 사라짐
                .setWhen(System.currentTimeMillis())    //시간

                .setDefaults(Notification.DEFAULT_ALL);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }

//        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        nm.notify(1234, builder.build());
        int iId = Integer.parseInt(call_midx);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(iId /* ID of notification */, builder.build());
    }


    //서비스 실행 여부 확인
    public  boolean isServiceRunningCheck() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.eluo.project.intranet.service.CallingService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}

