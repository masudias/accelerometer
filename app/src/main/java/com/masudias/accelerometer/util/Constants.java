package com.masudias.accelerometer.util;

public class Constants {
    public static final String ApplicationPackage = "com.masudias.accelerometer";
    public static final String ApplicationTag = "accelerometer_reading";
    public static final boolean DEBUG = false;

    // Message types sent from the BluetoothService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final String HOST = "HOST:";
    public static final String CLIENT = "CLIENT:";
    public static final Long HOST_OFFSET = 0L;
}