package com.example.smarthomepvl;

public class Device {
    private int id;
    private String diaChiMAC;
    private String tenThietBi;

    public Device(int id, String diaChiMAC, String tenThietBi) {
        this.id = id;
        this.diaChiMAC = diaChiMAC;
        this.tenThietBi = tenThietBi;
    }

    public int getId() { return id; }
    public String getDiaChiMAC() { return diaChiMAC; }
    public String getTenThietBi() { return tenThietBi; }
}
