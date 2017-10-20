package com.eluo.project.intranet.intro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.eluo.project.intranet.MainActivity;
import com.eluo.project.intranet.R;
import com.eluo.project.intranet.network.NetworkUtil;
import com.eluo.project.intranet.utils.ThreadPolicy;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * Project	    : Eluo Intranet
 * Program    : 표기 하지 않음
 * Description	: 엘루오 씨엔시 인트라넷 앱 인트로
 * Environment	:
 * Notes	    : Developed by
 *
 * @(#) IntroActivity.java
 * @since 2017-01-19
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-01-19][ggmario@eluocnc.com][CREATE: STATEMENT]
 */
public class IntroActivity extends Activity {
    Handler h;//핸들러 선언
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  // 화면위 타이틀 없애기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  // 전체화면 만들기

        //앱 설치시 home 화면에 바로가기 앱 아이콘 생성 기능
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        pref.getString("check", "");
        if(pref.getString("check", "").isEmpty()){
            Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
            shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            shortcutIntent.setClassName(this, getClass().getName());
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            Intent intent = new Intent();

            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResources().getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(this, R.mipmap.eluo_icon));
            intent.putExtra("duplicate", false);
            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

            sendBroadcast(intent);

        }
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("check", "exist");
        editor.commit();

        if (NetworkUtil.isNetworkConnected(this)) {
            String sVersionName = "1.0.0";
            try {
                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT){
                    // 유심이 없는 경우
                    Log.e("USIM :", "통신사가 없습니다");
                    AlertDialog.Builder alert = new AlertDialog.Builder(IntroActivity.this);
                    alert.setPositiveButton(R.string.D_Approval, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    alert.setMessage(R.string.D_aspiration_chip);
                    alert.show();
                } else {
                    // 유심이 존재하는 경우
                    sVersionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                    new ThreadPolicy();
                    String sVer = "";
                    String[][] parsedData = jsonParserList();

                    if (parsedData != null && parsedData.length > 0) {
                        sVer = parsedData[0][0];
                    } else {
                        sVer = sVersionName;
                        Log.i("버전 체크:", "앱 버전 체크 실패 하였습니다");
                        //FirebaseCrash.report(new Exception("앱 버전 체크 실패 하였습니다"));
                    }
                    if (!sVersionName.equals(sVer)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        // 여기서 부터는 알림창의 속성 설정
                        builder.setTitle(R.string.D_TitleName)
                                .setMessage(R.string.D_Update)
                                .setCancelable(false)
                                .setPositiveButton(R.string.D_Approval, new DialogInterface.OnClickListener() {
                                    // 확인 버튼 클릭시 설정
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        finish();
                                        callBrowser("http://fs.eluocnc.com:8282/download.jsp");
                                    }
                                })
                                .setNegativeButton(R.string.D_Canceled, new DialogInterface.OnClickListener() {
                                    // 취소 버튼 클릭시 설정
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        finish();
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else {
                        requestWindowFeature(Window.FEATURE_NO_TITLE); //인트로화면이므로 타이틀바를 없앤다
                        setContentView(R.layout.activity_intro);
                        h = new Handler(); //딜래이를 주기 위해 핸들러 생성
                        h.postDelayed(mrun, 2000); // 딜레이 ( 런어블 객체는 mrun, 시간 2초)
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                AlertDialog.Builder alert = new AlertDialog.Builder(IntroActivity.this);
                alert.setPositiveButton(R.string.D_Approval, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                alert.setMessage(R.string.network_error_msg);
                alert.show();
            }
        }else{
            AlertDialog.Builder alert = new AlertDialog.Builder(IntroActivity.this);
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

    Runnable mrun = new Runnable(){
        @Override
        public void run(){
            Intent i = new Intent(IntroActivity.this, MainActivity.class); //인텐트 생성(현 액티비티, 새로 실행할 액티비티)
            startActivity(i);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            //overridePendingTransition 이란 함수를 이용하여 fade in,out 효과를줌. 순서가 중요
        }
    };
    //인트로 중에 뒤로가기를 누를 경우 핸들러를 끊어버려 아무일 없게 만드는 부분
    //미 설정시 인트로 중 뒤로가기를 누르면 인트로 후에 홈화면이 나옴.
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        h.removeCallbacks(mrun);
    }

    //외부 브라우저 호출 (인트로 화면에서만 사용함)
    public void callBrowser(String url) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[][] jsonParserList() {
        URL url = null;
        HttpURLConnection urlConnection = null;
        try{
            //웹서버 URL 지정
            url= new URL("http://fs.eluocnc.com:8282/versionName.jsp");
            HttpURLConnection urlc =(HttpURLConnection)url.openConnection();
            urlc.setConnectTimeout(500);
            urlc.connect();
        }catch (Exception e){
            return null;
        }
        try {
            //URL 접속
            urlConnection = (HttpURLConnection) url.openConnection();
            //[웹문서 소스를 버퍼에 저장]
            //데이터를 버퍼에 기록
            BufferedReader bufreader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));
            Log.d("line:",bufreader.toString());
            String line = null;
            String page = "";

            //버퍼의 웹문서 소스를 줄단위로 읽어(line), Page에 저장함
            while((line = bufreader.readLine())!=null){
                Log.d("line:",line);
                page+=line;
            }
            //읽어들인 JSON포맷의 데이터를 JSON객체로 변환
            JSONObject json = new JSONObject(page);

            //ksk_list 에 해당하는 배열을 할당
            JSONArray jArr = json.getJSONArray("vers");
            String[][] parseredData = new String[jArr.length()][jArr.length()];

            for (int i=0; i<jArr.length(); i++){
                json = jArr.getJSONObject(i);
                parseredData[0][i] = json.getString("VER");
            }
            return parseredData;
        } catch (Exception e) {
            Log.e("err_v","앱버전 정보 가져오기 실패");
            //FirebaseCrash.report(new Exception("앱 버전 정보 가져오기 실패"));
            return null;
        }finally{
            urlConnection.disconnect();      //URL 연결 해제
        }
    }
}
