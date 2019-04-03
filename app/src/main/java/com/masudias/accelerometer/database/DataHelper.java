package com.masudias.accelerometer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.masudias.accelerometer.domain.AccelerometerReading;
import com.masudias.accelerometer.util.Constants;
import com.masudias.accelerometer.util.Logger;

public class DataHelper {

    private static final int DATABASE_VERSION = 1;

    private final Context context;
    private static DataHelper instance = null;
    private static DataBaseOpenHelper dOpenHelper;

    private DataHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized DataHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DataHelper(context);
            dOpenHelper = new DataBaseOpenHelper(context, DBConstants.DB_NAME,
                    DATABASE_VERSION);
        }
        return instance;
    }

    public void closeDbOpenHelper() {
        if (dOpenHelper != null) dOpenHelper.close();
        instance = null;
    }

    public long insertReading(AccelerometerReading reading) {

        long rowIdOfSavedReading = -1;

        if (reading != null) {
            SQLiteDatabase db = dOpenHelper.getWritableDatabase();
            db.beginTransaction();

            try {
                ContentValues values = new ContentValues();
                values.put(DBConstants.KEY_X, reading.x);
                values.put(DBConstants.KEY_Y, reading.y);
                values.put(DBConstants.KEY_Z, reading.z);
                values.put(DBConstants.KEY_CREATED_AT, reading.createdAt);
                rowIdOfSavedReading = db.insertWithOnConflict(DBConstants.DB_TABLE_READINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            } catch (Exception e) {
                e.printStackTrace();
            }

            db.setTransactionSuccessful();
            db.endTransaction();

            context.getContentResolver().notifyChange(DBConstants.DB_TABLE_USER_URI, null);

            Log.d(Constants.ApplicationTag, "Inserted reading into the table.");
        }

        return rowIdOfSavedReading;
    }

    public Cursor getAllReadings() {
        Cursor cursor = null;
        SQLiteDatabase db = dOpenHelper.getReadableDatabase();

        try {
            String queryString = "SELECT * FROM " + DBConstants.DB_TABLE_READINGS;
            cursor = db.rawQuery(queryString, null);
            if (cursor != null) cursor.moveToFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cursor;
    }

    public Cursor getReadingsInTimeRange(long startTime, long endTime) {
        Cursor cursor = null;
        SQLiteDatabase db = dOpenHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            String queryString = "SELECT * FROM " + DBConstants.DB_TABLE_READINGS
                    + " WHERE " + DBConstants.KEY_CREATED_AT + " > " + startTime
                    + " AND " + DBConstants.KEY_CREATED_AT + " < " + endTime;

            cursor = db.rawQuery(queryString, null);
            if (cursor != null) cursor.moveToFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cursor;
    }

    public void deleteReading(int id) {
        Cursor cursor = null;
        SQLiteDatabase db = dOpenHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            String queryString = "DELETE FROM " + DBConstants.DB_TABLE_READINGS
                    + " WHERE " + DBConstants.KEY_ID + " = " + id;
            cursor = db.rawQuery(queryString, null);
            if (cursor != null) cursor.moveToFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.setTransactionSuccessful();
        db.endTransaction();

        context.getContentResolver().notifyChange(DBConstants.DB_TABLE_USER_URI, null);

        Logger.debug(Constants.ApplicationTag, "Deleted reading from the table with id: " + id);
    }
}