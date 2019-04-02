package com.masudias.accelerometer.util;

import android.content.Context;
import android.os.AsyncTask;

import com.masudias.accelerometer.database.DataHelper;
import com.masudias.accelerometer.domain.AccelerometerReading;

public class InsertReadingAsyncTask extends AsyncTask<Object, Object, Long> {

    private Context context;
    private AccelerometerReading reading;

    public InsertReadingAsyncTask(Context context, AccelerometerReading reading) {
        this.context = context;
        this.reading = reading;
    }

    @Override
    protected Long doInBackground(Object... params) {
        return DataHelper.getInstance(context).insertReading(reading);
    }

    @Override
    protected void onPostExecute(Long savedId) {
        // Do nothing for now
    }
}