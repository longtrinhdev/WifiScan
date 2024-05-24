package com.example.androiddevwifiscan.Object;

public class OutData {
    private String SSID;
    private String BSSID;
    private String Level;
    private float Gx;
    private float Gy;
    private float Gz;

    private float Ax;
    private float Ay;
    private float Az;

    private float Mx;
    private float My;
    private float Mz;

    public OutData(String SSID, String BSSID, String level, float gx, float gy, float gz, float ax, float ay, float az, float mx, float my, float mz) {
        this.SSID = SSID;
        this.BSSID = BSSID;
        Level = level;
        Gx = gx;
        Gy = gy;
        Gz = gz;
        Ax = ax;
        Ay = ay;
        Az = az;
        Mx = mx;
        My = my;
        Mz = mz;
    }
    // getter
    public String getSSID() {
        return SSID;
    }

    public String getBSSID() {
        return BSSID;
    }

    public String getLevel() {
        return Level;
    }

    public float getGx() {
        return Gx;
    }

    public float getGy() {
        return Gy;
    }

    public float getGz() {
        return Gz;
    }

    public float getAx() {
        return Ax;
    }

    public float getAy() {
        return Ay;
    }

    public float getAz() {
        return Az;
    }

    public float getMx() {
        return Mx;
    }

    public float getMy() {
        return My;
    }

    public float getMz() {
        return Mz;
    }
    // setter

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public void setLevel(String level) {
        Level = level;
    }

    public void setGx(float gx) {
        Gx = gx;
    }

    public void setGy(float gy) {
        Gy = gy;
    }

    public void setGz(float gz) {
        Gz = gz;
    }

    public void setAx(float ax) {
        Ax = ax;
    }

    public void setAy(float ay) {
        Ay = ay;
    }

    public void setAz(float az) {
        Az = az;
    }

    public void setMx(float mx) {
        Mx = mx;
    }

    public void setMy(float my) {
        My = my;
    }

    public void setMz(float mz) {
        Mz = mz;
    }


}
