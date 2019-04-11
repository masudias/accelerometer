package com.masudias.accelerometer.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.masudias.accelerometer.R;
import com.masudias.accelerometer.service.BluetoothService;
import com.masudias.accelerometer.util.Constants;
import com.masudias.accelerometer.util.Logger;
import com.masudias.accelerometer.util.PreferenceManager;

public class SyncActivity extends AppCompatActivity {

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private long syncStartTimeInMillis;
    private long syncEndTimeInMillis;

    private Button findOtherDeviceBtn;
    private Button startSyncBtn;
    private TextView statusTextView;

    private String mConnectedDeviceName = null;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothService mChatService = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setupUIElements();
        setupBluetooth();
        setButtonActions();
        ensureDiscoverable();
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupBTService();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void setupBTService() {
        Logger.debug("SyncActivity", "setupChat()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothService(SyncActivity.this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    private void setupUIElements() {
        findOtherDeviceBtn = findViewById(R.id.btn_find_other_device);
        startSyncBtn = findViewById(R.id.btn_send_sync_message);
        statusTextView = findViewById(R.id.status_text);
    }

    private void setupBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(SyncActivity.this, getString(R.string.bluetooth_not_available), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setButtonActions() {

        findOtherDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serverIntent = new Intent(SyncActivity.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
            }
        });

        startSyncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncStartTimeInMillis = System.currentTimeMillis();
                sendMessage(Constants.HOST);
                PreferenceManager.setTimeOffset(SyncActivity.this, Constants.HOST_OFFSET);
            }
        });
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(SyncActivity.this, "Not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    private void setStatus(String status) {
        statusTextView.setText(status);
    }

    private void processMessageFromHost(String msg) {
        // There is nothing to process now, just stop the BTService and move to the next activity
        sendMessage(Constants.CLIENT + System.currentTimeMillis());
        PreferenceManager.setTimeOffset(SyncActivity.this, 0L);
        startMainActivity();
    }

    private void processMessageFromRemote(String msg) {
        syncEndTimeInMillis = System.currentTimeMillis();
        Long btOverHead = (syncEndTimeInMillis - syncStartTimeInMillis) / 2;

        String[] msgArr = msg.split(":");
        Long remoteTimestamp = Long.parseLong(msgArr[1]);

        Long systemTimestamp = System.currentTimeMillis();
        Long timeStampInRemoteDevice = remoteTimestamp + btOverHead;

        PreferenceManager.setTimeOffset(SyncActivity.this, timeStampInRemoteDevice - systemTimestamp);

        // There is nothing to process now, just stop the BTService and move to the next activity
        startMainActivity();
    }

    private void startMainActivity() {
        Intent intent = new Intent(SyncActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus("Connected to " + mConnectedDeviceName);
                            findOtherDeviceBtn.setVisibility(View.GONE);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus("Connecting...");
                            findOtherDeviceBtn.setEnabled(false);
                            break;
                        case BluetoothService.STATE_NONE:
                            setStatus("Not Connected");
                            findOtherDeviceBtn.setVisibility(View.VISIBLE);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Logger.debug(writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if (readMessage.contains(Constants.HOST)) processMessageFromHost(readMessage);
                    else if (readMessage.contains(Constants.CLIENT))
                        processMessageFromRemote(readMessage);
                    Logger.debug(readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(SyncActivity.this, "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(SyncActivity.this, msg.getData().getString(Constants.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupBTService();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Logger.debug("SyncActivity", "BT not enabled");
                    Toast.makeText(SyncActivity.this, "Bluetooth not enabled... Exiting...",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }
}