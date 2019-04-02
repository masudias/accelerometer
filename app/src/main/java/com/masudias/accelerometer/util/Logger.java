package com.masudias.accelerometer.util;

import android.util.Log;

public class Logger {
    public static void debug(String subject, String text) {
        if (Constants.DEBUG) Log.d(subject, text);
    }

    public static void error(String subject, String text) {
        if (Constants.DEBUG) Log.e(subject, text);
    }
}