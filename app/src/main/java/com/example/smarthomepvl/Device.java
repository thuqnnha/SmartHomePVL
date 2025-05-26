package com.example.smarthomepvl;

public class Device {
    private int id;
    private String diaChiMAC;
    private String tenThietBi;
    private int loaiThietBi;

    // Constructor
    public Device(int id, String diaChiMAC, String tenThietBi,int loaiThietBi) {
        this.id = id;
        this.diaChiMAC = diaChiMAC;
        this.tenThietBi = tenThietBi;
        this.loaiThietBi = loaiThietBi;
    }

    // Getter
    public int getId() {
        return id;
    }

    public String getDiaChiMAC() {
        return diaChiMAC;
    }

    public String getTenThietBi() {
        return tenThietBi;
    }
    public int getLoaiThietBi() {
        return loaiThietBi;
    }

    // Setter (tùy chọn nếu bạn cần chỉnh sửa)
    public void setId(int id) {
        this.id = id;
    }

    public void setDiaChiMAC(String diaChiMAC) {
        this.diaChiMAC = diaChiMAC;
    }

    public void setTenThietBi(String tenThietBi) {
        this.tenThietBi = tenThietBi;
    }
    public void setLoaiThietBi(int loaiThietBi) {
        this.loaiThietBi = loaiThietBi;
    }

}

