package com.masudias.accelerometer.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Thread;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothServerThread extends Thread {

    BluetoothAdapter adapter;
    BluetoothServerSocket serverSocket;
    BluetoothSocket socket;

    public BluetoothServerThread(BluetoothAdapter adapter, UUID uuid) {
        this.adapter = adapter;
        try {
            serverSocket = adapter.listenUsingRfcommWithServiceRecord("VEHICLE_DETECTOR", uuid);
        }
        catch (IOException ex) {
            Log.d("SERVER_BLUETOOTH", ex.getMessage());
        }

    }

    @Override
    public void run() {
        super.run();
        byte[] buffer = new byte[256];
        ByteBuffer longBuffer = ByteBuffer.allocate(Long.BYTES);
        do {
            try {
                Log.d("SERVER_BLUETOOTH", "Accepting connections...");
                BluetoothSocket socket = serverSocket.accept();
                Log.d("SERVER_BLUETOOTH", "Connection established with " + socket.getRemoteDevice().getName());
                while (socket.isConnected()) {
                    int numBytes = socket.getInputStream().read(buffer);
                    String message = new String(buffer, 0, numBytes, Charset.forName("UTF-8"));
                    Log.d("SERVER_BLUETOOTH", "Message received: " + message);
                    if (message.equals("CURRENT_MILLIS")) {
                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                        dos.writeLong(System.currentTimeMillis());
                        dos.flush();
                        socket.getOutputStream().flush();
                    }
                }
            }
            catch (IOException ex) {
                Log.d("SERVER_BLUETOOTH", ex.getMessage());
            }
        } while (true);

    }

    @Override
    protected void finalize() throws Throwable {
        socket.close();
        serverSocket.close();
        super.finalize();
    }
}
