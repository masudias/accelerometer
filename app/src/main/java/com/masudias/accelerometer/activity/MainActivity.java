package com.masudias.accelerometer.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.masudias.accelerometer.R;
import com.masudias.accelerometer.domain.AccelerometerReading;
import com.masudias.accelerometer.util.BluetoothClientThread;
import com.masudias.accelerometer.util.BluetoothServerThread;
import com.masudias.accelerometer.util.ExportHelper;
import com.masudias.accelerometer.util.InsertReadingAsyncTask;
import com.masudias.accelerometer.util.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

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
    private Switch mainDeviceBtn;

    private long startTime;
    private long endTime;

    private boolean modeServer = false;
    private BluetoothClientThread clientThread;
    private BluetoothServerThread serverThread;
    BluetoothAdapter bluetoothAdapter;
    private boolean receiverRegistered = false;

    ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();

    private String DEFAULT_UUID = "0000110-0000-1000-8000-00805F9B34FB";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setupUIElements();
        requestPermissionFromUser();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mainDeviceBtn.setChecked(false);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("MAIN", "Receiver onReceive action: " + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d("MAIN", "Device found");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                device.fetchUuidsWithSdp();
                Log.d("MAIN", "Device discovered: " + device.getName());
//                if (device.getUuids() != null) {
//                    Log.d("MAIN", "UUID: " + device.getUuids()[0].toString());
//                } else {
//                    Log.d("MAIN", "UUID is null");
//                }
//                if (device.getUuids() != null && device.getUuids()[0].equals(DEFAULT_UUID)) {
//                    Log.d("MAIN", "Device UUIDs: " + device.getUuids()[0].toString());
//                    clientThread = new BluetoothClientThread(device, UUID.fromString(DEFAULT_UUID));
//                    clientThread.start();
//                }
                deviceList.add(device);
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d("MAIN", "Discovery finished");
                for (BluetoothDevice device : deviceList) {
                    device.fetchUuidsWithSdp();
                }
                deviceList.clear();
            }
            else if (BluetoothDevice.ACTION_UUID.equals(action)) {
                Log.d("MAIN", "UUID fetch");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                Log.d("MAIN", "Device name: " + device.getName());
                if (uuidExtra != null) {
                    Log.d("MAIN", "UUID: " + uuidExtra[0].toString());
                    Log.d("MAIN", "DEFA: " + UUID.fromString(DEFAULT_UUID).toString());
                    //if (uuidExtra[0].toString().equals(UUID.fromString(DEFAULT_UUID).toString())) {
                    if (device.getName().startsWith("SAMSUNG-SGH")) {
                        Log.d("MAIN", "Connecting to " + device.getName());
                        clientThread = new BluetoothClientThread(device, UUID.fromString(DEFAULT_UUID));
                        clientThread.start();
                    }
                }
                else {
                    Log.d("MAIN", "UUID is null");
                }
            }
        }
    };

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
        mainDeviceBtn = findViewById(R.id.main_deivce);
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

        mainDeviceBtn.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton b, boolean checked) {
                Log.d("MAIN", "Main device button pressed");
                modeServer = checked;
                if (checked) {
                    if (clientThread != null) {
                        clientThread.interrupt();
                    }
                    if (receiverRegistered) {
                        bluetoothAdapter.cancelDiscovery();
                        unregisterReceiver(receiver);
                        receiverRegistered = false;
                    }
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivity(discoverableIntent);
                    serverThread = new BluetoothServerThread(bluetoothAdapter, UUID.fromString(DEFAULT_UUID));
                    serverThread.start();
                }
                else {
                    if (serverThread != null) {
                        serverThread.interrupt();
                    }
                    //Look for paired devices first
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                    Log.d("MAIN", "Paired devices found: " + pairedDevices.size());
                    Iterator<BluetoothDevice> it = pairedDevices.iterator();
                    boolean found = false;
                    BluetoothDevice device = null;
                    Log.d("MAIN", "Adapter address: " + bluetoothAdapter.getAddress());
                    while (it.hasNext() && !found) {
                        device = it.next();
                        Log.d("MAIN", "Paired device name: " + device.getName());
                        Log.d("MAIN", "Paired device UUID: " + device.getUuids()[0].toString());
                        Log.d("MAIN", "Paired device address: " + device.getAddress());
                        if (device.getUuids() != null
                                //&& device.getUuids()[0].toString().endsWith(DEFAULT_UUID.toString().substring(8))
                                && device.getUuids()[0].toString().equals(UUID.fromString(DEFAULT_UUID).toString())
                                && !device.getAddress().equals(bluetoothAdapter.getAddress())) {
                            found = true;
                        }
                    }
                    if (found) {
                        Log.d("MAIN", "Connecting to paired device " + device.getName());
                        clientThread = new BluetoothClientThread(device, UUID.fromString(DEFAULT_UUID));
                        clientThread.start();
                    }
                    else {
                        //If not found any, discover new devices
                        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                        filter.addAction(BluetoothDevice.ACTION_UUID);
                        Log.d("MAIN", "Registering receiver...");
                        registerReceiver(receiver, filter);
                        bluetoothAdapter.startDiscovery();
                        receiverRegistered = true;
                    }
                }
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
        if (clientThread != null) {
            clientThread.interrupt();
        }
        if (serverThread != null) {
            serverThread.interrupt();
        }
        if (receiverRegistered) {
            unregisterReceiver(receiver);
        }
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
            long currentTimestamp = System.currentTimeMillis() + getTimeOffset();
            AccelerometerReading reading = new AccelerometerReading(ax, ay, az, currentTimestamp);
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

    public long getTimeOffset() {
        if (modeServer) {
            return 0;
        }
        else {
            return clientThread.getTimeOffset();
        }
    }
}