package com.eluo.project.intranet.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.StrictMode;

/**
 * Created by gogumario on 2017-03-27.
 */

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class ThreadPolicy {
    public ThreadPolicy() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.
                Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }
}
