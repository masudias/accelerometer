package com.masudias.accelerometer.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.masudias.accelerometer.R;
import com.masudias.accelerometer.util.Constants;
import com.masudias.accelerometer.util.Logger;
import com.masudias.accelerometer.util.PreferenceManager;

public class ShakeAndSyncActivity extends AppCompatActivity implements SensorEventListener {

    private final int PERMISSION_REQUEST_CODE = 1144;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private double ax, ay, az;

    private Button startSyncBtn;
    private TextView syncStatusTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_with_shake);
        setupUIElements();
        requestPermissionFromUser();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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
        startSyncBtn = findViewById(R.id.btn_start_sync);
        syncStatusTextView = findViewById(R.id.sync_status_text);
        setupClickListenersForButtons();
    }

    private void setupClickListenersForButtons() {
        startSyncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSyncBtn.setEnabled(false);
                syncStatusTextView.setText("Start Shaking...");
                sensorManager.registerListener(ShakeAndSyncActivity.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
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

            if (passedShakeThreshold(ax, ay, az)) {
                recordReferenceTimestamp();
                playSoundOnCompletion();
                goBackToMainActivity();
            }
            Logger.debug("Readings", ax + "," + ay + "," + az);
        }
    }

    private void recordReferenceTimestamp() {
        sensorManager.unregisterListener(ShakeAndSyncActivity.this);
        PreferenceManager.setReferenceTimestamp(ShakeAndSyncActivity.this, System.currentTimeMillis());
        syncStatusTextView.setText("Time syncing complete!");
    }

    private boolean passedShakeThreshold(double ax, double ay, double az) {
        if (ax <= Constants.SHAKE_THRESHOLD_NEGATIVE || ax >= Constants.SHAKE_THRESHOLD_POSITIVE)
            return true;
        if (ay <= Constants.SHAKE_THRESHOLD_NEGATIVE || ay >= Constants.SHAKE_THRESHOLD_POSITIVE)
            return true;
        if (az <= Constants.SHAKE_THRESHOLD_NEGATIVE || az >= Constants.SHAKE_THRESHOLD_POSITIVE)
            return true;

        return false;
    }

    private void playSoundOnCompletion() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goBackToMainActivity() {
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(ShakeAndSyncActivity.this
                            , "This app requires this permission for exporting CSV files"
                            , Toast.LENGTH_SHORT).show();
                    requestPermissionFromUser(); // Re attempt requesting for permission
                } else if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(ShakeAndSyncActivity.this
                            , "This app requires this permission for exporting CSV files"
                            , Toast.LENGTH_SHORT).show();
                    requestPermissionFromUser(); // Re attempt requesting for permission
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        return; // Disable the back button press
    }
}