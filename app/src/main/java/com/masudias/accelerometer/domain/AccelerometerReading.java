package com.masudias.accelerometer.domain;

public class AccelerometerReading {
    public Double x;
    public Double y;
    public Double z;
    public Long createdAt;

    public AccelerometerReading(Double x, Double y, Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.createdAt = System.currentTimeMillis();
    }
}