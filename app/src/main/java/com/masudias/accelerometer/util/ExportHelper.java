package com.masudias.accelerometer.util;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.widget.Toast;

import com.masudias.accelerometer.database.DBConstants;
import com.masudias.accelerometer.database.DataHelper;
import com.masudias.accelerometer.domain.AccelerometerReading;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class ExportHelper {

    public static void exportAllData(Context context, String fileName) {
        Cursor cursor = DataHelper.getInstance(context).getAllReadings();

        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(context, "No readings found!", Toast.LENGTH_LONG).show();
            return;
        }

        ArrayList<AccelerometerReading> readings = new ArrayList<AccelerometerReading>();

        cursor.moveToFirst();

        do {
            double ax = cursor.getDouble(cursor.getColumnIndex(DBConstants.KEY_X));
            double ay = cursor.getDouble(cursor.getColumnIndex(DBConstants.KEY_Y));
            double az = cursor.getDouble(cursor.getColumnIndex(DBConstants.KEY_Z));
            long createdAt = cursor.getLong(cursor.getColumnIndex(DBConstants.KEY_CREATED_AT));
            readings.add(new AccelerometerReading(ax, ay, az, createdAt));
        } while (cursor.moveToNext());

        createCSVFile(context, fileName, readings);
    }

    public static void exportDataWithTimeRange(Context context, String fileName, Long startTime, Long endTime) {
        Cursor cursor = DataHelper.getInstance(context).getReadingsInTimeRange(startTime, endTime);

        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(context, "No readings found!", Toast.LENGTH_LONG).show();
            return;
        }

        ArrayList<AccelerometerReading> readings = new ArrayList<AccelerometerReading>();

        cursor.moveToFirst();

        do {
            double ax = cursor.getDouble(cursor.getColumnIndex(DBConstants.KEY_X));
            double ay = cursor.getDouble(cursor.getColumnIndex(DBConstants.KEY_Y));
            double az = cursor.getDouble(cursor.getColumnIndex(DBConstants.KEY_Z));
            long createdAt = cursor.getLong(cursor.getColumnIndex(DBConstants.KEY_CREATED_AT));
            readings.add(new AccelerometerReading(ax, ay, az, createdAt));
        } while (cursor.moveToNext());

        createCSVFile(context, fileName, readings);
    }

    private static void createCSVFile(Context context, String fileName, ArrayList<AccelerometerReading> readings) {

        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "readings");
        if (!directory.exists()) directory.mkdirs();

        try {
            File csvFile = new File(directory + File.separator + fileName + ".csv");
            csvFile.createNewFile();

            StringBuilder sb = new StringBuilder();
            sb.append("timestamp,x,y,z");
            sb.append('\n');

            for (AccelerometerReading reading : readings) {
                sb.append(reading.createdAt);
                sb.append(',');
                sb.append(reading.x);
                sb.append(',');
                sb.append(reading.y);
                sb.append(',');
                sb.append(reading.z);
                sb.append('\n');
            }

            FileWriter out = new FileWriter(csvFile);
            out.write(sb.toString());
            out.close();
            Toast.makeText(context, "CSV File created successfully!", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "ERROR Creating CSV File!", Toast.LENGTH_LONG).show();
        }
    }
}