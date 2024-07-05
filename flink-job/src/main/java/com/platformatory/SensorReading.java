package com.platformatory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class SensorReading {
    private String household;
    private String date;
    private String time;
    private double global_active_power;
    private double global_reactive_power;
    private double voltage;
    private double global_intensity;
    private double sub_metering_1;
    private double sub_metering_2;
    private double sub_metering_3;
    private long timestamp;

    // Default constructor for deserialization
    public SensorReading() {}

    public String getHousehold() {
        return household;
    }

    public void setHousehold(String household) {
        this.household = household;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
        if (date!=null && time!=null) {
            this.timestamp = convertToTimestamp(date, time);
        }
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
        if (date!=null && time!=null) {
            this.timestamp = convertToTimestamp(date, time);
        }
    }

    public double getGlobal_active_power() {
        return this.global_active_power;
    }

    public void setGlobal_active_power(double global_active_power) {
        this.global_active_power = global_active_power;
    }

    public double getGlobal_reactive_power() {
        return this.global_reactive_power;
    }

    public void setGlobal_reactive_power(double global_reactive_power) {
        this.global_reactive_power = global_reactive_power;
    }

    public double getVoltage() {
        return this.voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    public double getGlobal_intensity() {
        return this.global_intensity;
    }

    public void setGlobal_intensity(double global_intensity) {
        this.global_intensity = global_intensity;
    }

    public double getSub_metering_1() {
        return this.sub_metering_1;
    }

    public void setSub_metering_1(double sub_metering_1) {
        this.sub_metering_1 = sub_metering_1;
    }

    public double getSub_metering_2() {
        return this.sub_metering_2;
    }

    public void setSub_metering_2(double sub_metering_2) {
        this.sub_metering_2 = sub_metering_2;
    }

    public double getSub_metering_3() {
        return this.sub_metering_3;
    }

    public void setSub_metering_3(double sub_metering_3) {
        this.sub_metering_3 = sub_metering_3;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    private long convertToTimestamp(String date, String time) {
        String dateTimeStr = date + " " + time;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy H:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
