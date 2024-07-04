package com.platformatory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class SensorReading {
    private String Date;
    private String Time;
    private double Global_active_power;
    private double Global_reactive_power;
    private double Voltage;
    private double Global_intensity;
    private double Sub_metering_1;
    private double Sub_metering_2;
    private double Sub_metering_3;
    private long timestamp;

    // Default constructor for deserialization
    public SensorReading() {}

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
        if (Date!=null && Time!=null) {
            this.timestamp = convertToTimestamp(Date, Time);
        }
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
        if (Date!=null && Time!=null) {
            this.timestamp = convertToTimestamp(Date, Time);
        }
    }

    public double getGlobal_active_power() {
        return Global_active_power;
    }

    public void setGlobal_active_power(double global_active_power) {
        Global_active_power = global_active_power;
    }

    public double getGlobal_reactive_power() {
        return Global_reactive_power;
    }

    public void setGlobal_reactive_power(double global_reactive_power) {
        Global_reactive_power = global_reactive_power;
    }

    public double getVoltage() {
        return Voltage;
    }

    public void setVoltage(double voltage) {
        Voltage = voltage;
    }

    public double getGlobal_intensity() {
        return Global_intensity;
    }

    public void setGlobal_intensity(double global_intensity) {
        Global_intensity = global_intensity;
    }

    public double getSub_metering_1() {
        return Sub_metering_1;
    }

    public void setSub_metering_1(double sub_metering_1) {
        Sub_metering_1 = sub_metering_1;
    }

    public double getSub_metering_2() {
        return Sub_metering_2;
    }

    public void setSub_metering_2(double sub_metering_2) {
        Sub_metering_2 = sub_metering_2;
    }

    public double getSub_metering_3() {
        return Sub_metering_3;
    }

    public void setSub_metering_3(double sub_metering_3) {
        Sub_metering_3 = sub_metering_3;
    }

    public long getTimestamp() {
        return timestamp;
    }

    private long convertToTimestamp(String date, String time) {
        String dateTimeStr = date + " " + time;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy H:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
