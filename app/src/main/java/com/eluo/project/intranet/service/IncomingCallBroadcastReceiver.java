/*
 * Project	    : Eluo Intranet
 * Program    : 표기 하지 않음
 * Description	: 엘루오 씨엔시 리시버(전화 수신 리시버)
 * Environment	:
 * Notes	    : Developed by
 *
 * @(#) IncomingCallBroadcastReceiver.java
 * @since 2017-03-14
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-03-14][ggmario@eluocnc.com][CREATE: STATEMENT]
 */
package com.eluo.project.intranet.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by gogumario on 2017-03-14.
 */
public class IncomingCallBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG = "PHONE STATE";
    private static String mLastState;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG,"onReceive()");
        /** * http://mmarvick.github.io/blog/blog/lollipop-multiple-broadcastreceiver-call-state/ * 2번 호출되는 문제 해결 */
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (state.equals(mLastState)) {
            return;
        } else {
            mLastState = state;
        }
//            TelephonyManager.EXTRA_STATE_IDLE: 통화종료 혹은 통화벨 종료
//            TelephonyManager.EXTRA_STATE_RINGING: 통화벨 울리는중
//            TelephonyManager.EXTRA_STATE_OFFHOOK: 통화중

        //통화벨 울리면 실행
        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            final String phone_number = PhoneNumberUtils.formatNumber(incomingNumber);
            Intent serviceIntent = new Intent(context, CallingService.class);
            serviceIntent.putExtra(CallingService.EXTRA_CALL_NUMBER, phone_number);
            context.startService(serviceIntent);
        }
        //통화종료 혹은 통화벨 종료
        if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            Intent serviceIntent = new Intent(context, CallingService.class);
            serviceIntent.putExtra(CallingService.EXTRA_CALL_NUMBER, "A");
            context.startService(serviceIntent);
        }
        //통화중
        if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
            Intent serviceIntent = new Intent(context, CallingService.class);
            serviceIntent.putExtra(CallingService.EXTRA_CALL_NUMBER, "");
            context.startService(serviceIntent);
        }
    }
}

