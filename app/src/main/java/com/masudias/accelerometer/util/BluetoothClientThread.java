package com.masudias.accelerometer.util;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.UUID;


public class BluetoothClientThread extends Thread {
    private static final int MAX_SAMPLES = 20;

    BluetoothSocket socket;
    BluetoothDevice device;

    private long timeOffset = 0;

    public BluetoothClientThread(BluetoothDevice device, UUID uuid) {
        this.device = device;
        try {
            //socket = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
            socket = device.createRfcommSocketToServiceRecord(uuid);
        }
        catch (IOException ex) {
            Log.e("BLUETOOTH_CLIENT", ex.getMessage());
        }

    }

    @Override
    public void run() {
        super.run();
        timeOffset = 0;
        try {
            Log.d("BLUETOOTH_CLIENT", "Connecting...");
            socket.connect();
            Log.d("BLUETOOTH_CLIENT", "Socket connected");
            do {
                long overheadSum = 0;
                for (int i = 0; i < MAX_SAMPLES; i++) {
                    long initialMillis = System.currentTimeMillis();
                    socket.getOutputStream().write("CURRENT_MILLIS".getBytes(Charset.forName("UTF-8")));
                    socket.getOutputStream().flush();
                    Log.d("BLUETOOTH_CLIENT", "Message sent");
                    DataInputStream dis = new DataInputStream(socket.getInputStream());
                    long remoteTimestamp = dis.readLong();
                    Log.d("BLUETOOTH_CLIENT", "Initial millis: " + initialMillis);
                    Log.d("BLUETOOTH_CLIENT", "Data read, remotTimestamp: " + remoteTimestamp);
                    long finalMillis = System.currentTimeMillis();
                    long bluetoothOverhead = (finalMillis - initialMillis) / 2;
                    byte[] buffer = new byte[48];
                    //int numBytes = socket.getInputStream().read(buffer);
                    //long remoteTimestamp = ByteBuffer.wrap(buffer, 0, numBytes).getLong();
                    long offset = remoteTimestamp - initialMillis;
                    overheadSum += offset - bluetoothOverhead;
                    Thread.sleep(40);
                }
                timeOffset = overheadSum / MAX_SAMPLES;
                Log.d("BLUETOOTH_CLIENT", "Time Offset: " + timeOffset);
                Thread.sleep(300000);
            } while(true);
        }
        catch (IOException ex) {
            Log.e("BLUETOOTH_CLIENT", ex.getMessage());
        }
        catch (InterruptedException ex) {
            Log.e("BLUETOOTH_CLIENT", ex.getMessage() != null ? ex.getMessage() : "null");
            ex.printStackTrace();
        }
    }

    public long getTimeOffset() {
        return timeOffset;
    }

    @Override
    protected void finalize() throws Throwable {
        socket.close();
        super.finalize();
    }
}
