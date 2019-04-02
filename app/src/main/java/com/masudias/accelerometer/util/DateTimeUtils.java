package com.masudias.accelerometer.util;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DateTimeUtils {

    private static final SimpleDateFormat DATE_FORMAT_WITH_TIME = new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.US);

    public static String formatDateWithTime(long time) {
        return DATE_FORMAT_WITH_TIME.format(time);
    }
}