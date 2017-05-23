/*
 * Project	    : Eluo Intranet
 * Program    : 표기 하지 않음
 * Description	: 엘루오 씨엔시 쓰레드 처리
 * Environment	:
 * Notes	    : Developed by
 *
 * @(#) TheadPolicy.java
 * @since 2017-03-27
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-03-27][ggmario@eluocnc.com][CREATE: STATEMENT]
 */
package com.eluo.project.intranet.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.StrictMode;

/**
 * Created by gogumario on 2017-03-27.
 * 스레드
 * @author gogumario
 * @version 1.0
 * @since  2017-03-27
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class ThreadPolicy {
    public ThreadPolicy() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.
                Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }
}
