package com.example.smarthomepvl;

import java.util.Date;

public class ChartEntry {
    private Date dateTime;
    private String status;
    private String timeOn;   // Định dạng HH:mm:ss
    private String timeOff;
    private float current;
    private float voltage;
    private float temperature;

    public ChartEntry(Date dateTime, String timeOn,String timeOff,String status, float current, float voltage, float temperature) {
        this.dateTime = dateTime;
        this.timeOn = timeOn;
        this.timeOff = timeOff;
        this.status = status;
        this.current = current;
        this.voltage = voltage;
        this.temperature = temperature;
    }

    public Date getDateTime() { return dateTime; }
    public String gettimeOn() { return timeOn; }
    public String gettimeOff() { return timeOff; }
    public String getStatus() { return status; }
    public float getCurrent() { return current; }
    public float getVoltage() { return voltage; }
    public float getTemperature() { return temperature; }
}

