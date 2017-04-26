package com.eluo.project.intranet.meeting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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

import com.eluo.project.intranet.R;
import com.eluo.project.intranet.network.NetworkUtil;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by gogumario on 2017-04-13.
 */

public class MeetingBookingList  extends AppCompatActivity {
    private ArrayAdapter<String> m_Adapter;
    private ListView m_ListView;
    private Bitmap bmp;
    private String psMid = null;
    private String psMidx = null;
    private String psMpath = null;
    private String psMdept = null;
    private String psMname = null;
    private String sTelephone = null;
    private String sMeetingTitle = null;
    private String sDate = null;
    private ListView mListView = null;
    private ListViewAdapter mAdapter = null;
    private String sNotice = null;

    // 중복 클릭 방지 시간 설정
    private static final long MIN_CLICK_INTERVAL=600;
    private long mLastClickTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(getIntent());

        requestWindowFeature(Window.FEATURE_NO_TITLE);  // 화면위 타이틀 없애기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  // 전체화면 만들기
        setContentView(R.layout.activity_meeting_booking_list_all);   //작성 화면 구성 xml

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (NetworkUtil.isNetworkConnected(this)) {
            psMidx = intent.getStringExtra("idx");    // 키값(PRIMARY KEY)
            psMid = intent.getStringExtra("id");
            psMname = intent.getStringExtra("name");
            psMpath = intent.getStringExtra("path");
            psMdept = intent.getStringExtra("dept");
            sTelephone = intent.getStringExtra("sTelephone");
            sDate = intent.getStringExtra("sDate");
            System.out.println(">>>DATE::::"+sDate);

            //현재 날짜 구함
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat CurYearFormat = new SimpleDateFormat("yyyy");
            SimpleDateFormat CurMonthFormat = new SimpleDateFormat("MM");
            SimpleDateFormat CurDayFormat = new SimpleDateFormat("dd");
            SimpleDateFormat CurHourFormat = new SimpleDateFormat("HH");
            SimpleDateFormat CurMinuteFormat = new SimpleDateFormat("mm");

            String strCurYear = CurYearFormat.format(date);
            String strCurMonth = CurMonthFormat.format(date);
            String strCurDay = CurDayFormat.format(date);
            String strCurHour = CurHourFormat.format(date);
            String strCurMinute = CurMinuteFormat.format(date);

            int iCiDat = Integer.parseInt(sDate);   //선택한 날짜
            int iToDate  =  Integer.parseInt(strCurYear+strCurMonth+strCurDay);//오늘 날짜
            int iToTime = Integer.parseInt(strCurHour);
            int iToMinute = Integer.parseInt(strCurMinute);
            if(iToDate > iCiDat){
                sNotice = "ORD";
            }else if(iToDate == iCiDat){
                sNotice = "TOD";
            }else{
                sNotice = "FUT";
            }

            mListView = (ListView) findViewById(R.id.meeting_mList);
            mAdapter = new ListViewAdapter(this);
            mListView.setAdapter(mAdapter);

            String result = SendByHttp(sTelephone); // 메시지를 서버에 보냄
            String[][] parsedData = jsonParserList(result); // JSON 데이터 파싱

            if (result.lastIndexOf("RESULT") > 0) {
                mAdapter.addItem(getResources().getDrawable(R.drawable.common_full_open_on_phone),
                        "예약된 회의실 없음 ",
                        "2014-02-18");
            }else {
                if(parsedData != null) {
                    if (parsedData.length > 0) {
                        Resources res = getResources();
                        String[] arrString = res.getStringArray(R.array.meeting_time);
                        List<String>  mArrayList = new ArrayList<String>();

                        int iCk= 0;
                        for(String s:arrString){
                            for (int i = 0; i < parsedData.length; i++) {
                                if(s.equals(parsedData[i][0])){
                                    mAdapter.addItem(getResources().getDrawable(R.mipmap.icon_meeting), parsedData[i][0], parsedData[i][1]);
                                    iCk = 1;
                                }
                            }
                            if(iCk == 0){
                                if(sNotice.equals("ORD")){  //과거
                                    mAdapter.addItem(getResources().getDrawable(R.mipmap.icon_meeting), s, getString(R.string.not_available));
                                }else if(sNotice.equals("TOD")){ //현제
                                    String sTmp = s.substring(0,2);
                                    String sTmp1 = s.substring(3,5);
                                    int iTime = Integer.parseInt(sTmp);
                                    int iMinute = Integer.parseInt(sTmp1);
                                    if(iToTime > iTime) {
                                        mAdapter.addItem(getResources().getDrawable(R.mipmap.icon_meeting), s, getString(R.string.not_available));
                                    }else if(iToTime == iTime){
                                        if(iToMinute <= iMinute){
                                            mAdapter.addItem(getResources().getDrawable(R.mipmap.icon_my_eluo), s, getString(R.string.available));
                                        }else{
                                            mAdapter.addItem(getResources().getDrawable(R.mipmap.icon_meeting), s, getString(R.string.not_available));
                                        }
                                    }else{
                                        mAdapter.addItem(getResources().getDrawable(R.mipmap.icon_my_eluo), s, getString(R.string.available));
                                    }
                                }else{  //미래
                                    mAdapter.addItem(getResources().getDrawable(R.mipmap.icon_my_eluo), s, getString(R.string.available));
                                }
                            }
                            mArrayList.add(s);
                            iCk = 0;
                        }
                    }else{
                        Toast.makeText(this, R.string.network_error_retry,Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(this, R.string.network_error_retry,Toast.LENGTH_SHORT).show();
                }
            }
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id){
                    MeetingListData mData = mAdapter.mListData.get(position);

//                    System.out.println("VVVVVVVVVVVVVVVVVVVVVVVVVVV"+position);
                    if(sNotice.equals("ORD")) {  //과거
                        Toast.makeText(MeetingBookingList.this, R.string.D_not_available, Toast.LENGTH_SHORT).show();
                    }else if(sNotice.equals("TOD")) {//현제
//                        Toast.makeText(MeetingBookingList.this, mData.mTitle, Toast.LENGTH_SHORT).show();
                        if(mData.mTitle.equals("예약 가능")){
                            Toast.makeText(MeetingBookingList.this, mData.mTitle, Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MeetingBookingList.this, "불가", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(MeetingBookingList.this, mData.mTitle, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    private class ViewHolder {
        public ImageView mIcon;
        public TextView mText;
        public TextView mDate;
    }

    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<MeetingListData> mListData = new ArrayList<MeetingListData>();

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

        public void addItem(Drawable icon, String mDate, String mTitle ){
            MeetingListData addInfo = null;
            addInfo = new MeetingListData();
            addInfo.mIcon = icon;
            addInfo.mDate = mDate;
            addInfo.mTitle = mTitle;

            mListData.add(addInfo);
        }

        public void remove(int position){
            mListData.remove(position);
            dataChange();
        }

        public void sort(){
            Collections.sort(mListData, MeetingListData.ALPHA_COMPARATOR);
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
                convertView = inflater.inflate(R.layout.activity_listview_item, null);

                holder.mIcon = (ImageView) convertView.findViewById(R.id.mImage);
                holder.mText = (TextView) convertView.findViewById(R.id.mText);
                holder.mDate = (TextView) convertView.findViewById(R.id.mDate);

                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

            MeetingListData mData = mListData.get(position);


            if (mData.mIcon != null) {
                holder.mIcon.setVisibility(View.VISIBLE);
                holder.mIcon.setImageDrawable(mData.mIcon);
            }else{
                holder.mIcon.setVisibility(View.GONE);
            }

            holder.mText.setText(mData.mTitle);
            holder.mDate.setText(mData.mDate);

            return convertView;
        }
    }

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
            if (NetworkUtil.isNetworkConnected(MeetingBookingList.this)) {
            }else{
                Toast.makeText(MeetingBookingList.this, R.string.network_error_chk, Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * 서버에 데이터를 보내는 메소드 (회의실 리스트)
     * @param msg
     * @return
     */
    private String SendByHttp(String msg) {
        if(msg == null)
            msg = "";
        String URL ="http://www.eluocnc.com/GW_V3/app/meetList.asp";

        DefaultHttpClient client = new DefaultHttpClient();
        try {
			/* 체크할 id와 pwd값 서버로 전송 */
            HttpPost post = new HttpPost(URL);

			/* 지연시간 최대 3초 */
            HttpParams params = client.getParams();
            HttpConnectionParams.setConnectionTimeout(params, 3000);
            HttpConnectionParams.setSoTimeout(params, 300);

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
     * 받은 JSON 객체를 파싱하는 메소드(회의실 리스트)
     * @param
     * @return
     */
    private String[][] jsonParserList(String pRecvServerPage) {
        Log.i("서버에서 받은 전체 내용 : ", pRecvServerPage);
        try {
            JSONObject json = new JSONObject(pRecvServerPage);
            JSONArray jArr = json.getJSONArray("meeting");

            String jsonName[];
            if(pRecvServerPage.lastIndexOf("RESULT") > 0) {
                String[] jsonName1 = {"RESULT"};
                jsonName = jsonName1;
            }else{
                String[] jsonName1 = {"MTIME", "M7F", "M8F"};
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

    //스마트 폰에서 뒤로가기 버튼 선택시 처리 이벤트 (클릭시 종료 여부 확인 메시지 처리)
    public boolean onKeyDown( int KeyCode, KeyEvent event ){
        if( KeyCode == KeyEvent.KEYCODE_BACK ){
            if (NetworkUtil.isNetworkConnected(this)) {
                Intent intent = new Intent(MeetingBookingList.this, Meeting.class);//엑티비티 생성 작성 화면
                intent.putExtra("idx",psMidx); //조회 키 값을 넘겨준다
                intent.putExtra("id",psMid);
                intent.putExtra("name",psMname);
                intent.putExtra("path",psMpath);
                intent.putExtra("dept",psMdept);
                intent.putExtra("sTelephone",sTelephone);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.anim_slide_in_top, R.anim.anim_slide_out_bottom);
                finish();
            }else{
                AlertDialog.Builder alert = new AlertDialog.Builder(MeetingBookingList.this);
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
