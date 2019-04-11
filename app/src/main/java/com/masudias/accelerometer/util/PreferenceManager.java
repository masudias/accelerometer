package com.masudias.accelerometer.util;

import android.content.Context;

public class PreferenceManager {

    public static final String REFERENCE_TIME = "REFERENCE_TIME";

    public static void setReferenceTimestamp(Context context, Long timestamp) {
        context.getSharedPreferences(Constants.ApplicationTag, Context.MODE_PRIVATE)
                .edit().putLong(REFERENCE_TIME, timestamp).apply();
    }

    public static Long getReferenceTimestamp(Context context) {
        return context.getSharedPreferences(Constants.ApplicationTag, Context.MODE_PRIVATE)
                .getLong(REFERENCE_TIME, 0);
    }
}
