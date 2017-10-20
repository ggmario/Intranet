/*
 * Project	    : Eluo Intranet
 * Program    : 표기 하지 않음
 * Description	: 엘루오 씨엔시 유틸
 * Environment	:
 * Notes	    : Developed by
 *
 * @(#) Util.java
 * @since 2017-01-14
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-01-14][ggmario@eluocnc.com][CREATE: STATEMENT]
 */
package com.eluo.project.intranet.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/**
 * Created by gogumario on 2017-01-14.
 * 유틸
 * @author gogumario
 * @version 1.0
 * @since  2017-01-14
 */

public class Util  extends Activity {

    public static final String CONNECTION_CONFIRM_CLIENT_URL = "http://clients3.google.com/generate_204";



    private static class CheckConnect extends Thread{
        private boolean success;
        private String host;

        public CheckConnect(String host){
            this.host = host;
        }

        @Override
        public void run() {

            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection)new URL(host).openConnection();
                conn.setRequestProperty("User-Agent","Android");
                conn.setConnectTimeout(1000);
                conn.connect();
                int responseCode = conn.getResponseCode();
                if(responseCode == 204) success = true;
                else success = false;
            }
            catch (Exception e) {
                e.printStackTrace();
                success = false;
            }
            if(conn != null){
                conn.disconnect();
            }
        }

        public boolean isSuccess(){
            return success;
        }

    }


    public static boolean isOnline() {
        CheckConnect cc = new CheckConnect(CONNECTION_CONFIRM_CLIENT_URL);
        cc.start();
        try{
            cc.join();
            return cc.isSuccess();
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    @SuppressLint("NewApi")
    public static String GetDevicesUUID(Context mContext) {
        final TelephonyManager tm = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = ""
                + android.provider.Settings.Secure.getString(
                mContext.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(),
                ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        return deviceId;
    }
}
