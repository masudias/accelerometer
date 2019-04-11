package com.masudias.accelerometer.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.masudias.accelerometer.R;
import com.masudias.accelerometer.domain.AccelerometerReading;
import com.masudias.accelerometer.util.ExportHelper;
import com.masudias.accelerometer.util.InsertReadingAsyncTask;
import com.masudias.accelerometer.util.Logger;
import com.masudias.accelerometer.util.PreferenceManager;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private final int PERMISSION_REQUEST_CODE = 1144;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private double ax, ay, az;

    private TextView axTextView;
    private TextView ayTextView;
    private TextView azTextView;
    private Button exportBtn;
    private Button exportAllBtn;
    private Button startBtn;
    private Button endBtn;

    private long startTime;
    private long endTime;
    private long timeOffset;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setupUIElements();
        requestPermissionFromUser();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        timeOffset = PreferenceManager.getTimeOffset(MainActivity.this);
        Logger.debug("Time NOW: " + (System.currentTimeMillis() + timeOffset));
    }

    private void requestPermissionFromUser() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WAKE_LOCK
                    }, PERMISSION_REQUEST_CODE);
        }
    }

    private void setupUIElements() {
        axTextView = findViewById(R.id.ax);
        ayTextView = findViewById(R.id.ay);
        azTextView = findViewById(R.id.az);
        exportBtn = findViewById(R.id.export);
        exportAllBtn = findViewById(R.id.export_all);
        startBtn = findViewById(R.id.start);
        endBtn = findViewById(R.id.end);
        setupClickListenersForButtons();
    }

    private void setupClickListenersForButtons() {
        exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogForFileNameAndExportDataInTimeRange(startTime, endTime);
                startTime = endTime = 0; // clearing the values
            }
        });

        exportAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogForFileNameAndExportAllData();
            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTime = System.currentTimeMillis();
                endTime = Long.MAX_VALUE;

                startBtn.setEnabled(false);
                endBtn.setEnabled(true);
                sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
            }
        });

        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTime = System.currentTimeMillis();

                startBtn.setEnabled(true);
                endBtn.setEnabled(false);
                sensorManager.unregisterListener(MainActivity.this);
            }
        });
    }

    public void setValuesInUI(AccelerometerReading reading) {
        axTextView.setText(reading.x + "");
        ayTextView.setText(reading.y + "");
        azTextView.setText(reading.z + "");
    }

    private void showDialogForFileNameAndExportAllData() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("File Name");
        alertDialog.setMessage("Please enter a file name");

        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String fileName = input.getText().toString();
                        if (!fileName.isEmpty()) {
                            ExportHelper.exportAllData(MainActivity.this, fileName);
                        } else {
                            Toast.makeText(MainActivity.this, "Please put a file name", Toast.LENGTH_LONG).show();
                        }
                    }
                });

        alertDialog.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    private void showDialogForFileNameAndExportDataInTimeRange(final Long startTime, final Long endTime) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("File Name");
        alertDialog.setMessage("Please enter a file name");

        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String fileName = input.getText().toString();
                        if (!fileName.isEmpty() && startTime != 0 && endTime != 0) {
                            ExportHelper.exportDataWithTimeRange(MainActivity.this, fileName, startTime, endTime);
                        } else {
                            if (fileName.isEmpty())
                                Toast.makeText(MainActivity.this, "Please put a file name", Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(MainActivity.this, "Please start recording", Toast.LENGTH_LONG).show();
                        }
                    }
                });

        alertDialog.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];
            long timeStamp = System.currentTimeMillis() + timeOffset;
            AccelerometerReading reading = new AccelerometerReading(ax, ay, az, timeStamp);
            Logger.debug("Readings", ax + "," + ay + "," + az);

            // Insert values in the database with timestamp
            new InsertReadingAsyncTask(MainActivity.this, reading).execute();
            setValuesInUI(reading);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this
                            , "This app requires this permission for exporting CSV files"
                            , Toast.LENGTH_SHORT).show();
                    requestPermissionFromUser(); // Re attempt requesting for permission
                } else if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this
                            , "This app requires this permission for exporting CSV files"
                            , Toast.LENGTH_SHORT).show();
                    requestPermissionFromUser(); // Re attempt requesting for permission
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}