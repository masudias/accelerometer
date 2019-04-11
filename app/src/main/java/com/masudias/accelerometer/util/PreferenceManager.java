package com.masudias.accelerometer.util;

import android.content.Context;

public class PreferenceManager {

    public static final String TIME_OFFSET = "TIME_OFFSET";

    public static void setTimeOffset(Context context, Long timestamp) {
        context.getSharedPreferences(Constants.ApplicationTag, Context.MODE_PRIVATE).edit()
                .putLong(TIME_OFFSET, timestamp).apply();
    }

    public static Long getTimeOffset(Context context) {
        return context.getSharedPreferences(Constants.ApplicationTag, Context.MODE_PRIVATE)
                .getLong(TIME_OFFSET, 0);
    }
}
