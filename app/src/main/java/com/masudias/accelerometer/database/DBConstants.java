package com.masudias.accelerometer.database;

import android.net.Uri;

import com.masudias.accelerometer.util.Constants;

public class DBConstants {

    public static final String TAG = "DataBaseOpenHelper";
    public static final String DB_PATH = "/data/data/" + Constants.ApplicationPackage + "/databases/";
    public static final String DB_NAME = "sqlitedboperation";
    public static final String DB_TABLE_READINGS = "readings";
    public static final Uri DB_TABLE_USER_URI = Uri
            .parse("sqlite://" + Constants.ApplicationPackage + "/" + DB_TABLE_READINGS);

    // User table columns
    public static final String KEY_ID = "id";
    public static final String KEY_X = "x";
    public static final String KEY_Y = "y";
    public static final String KEY_Z = "z";
    public static final String KEY_CREATED_AT = "created_at";
}