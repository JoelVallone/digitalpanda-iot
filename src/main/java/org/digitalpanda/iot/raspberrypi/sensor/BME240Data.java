package org.digitalpanda.iot.raspberrypi.sensor;

public class BME240Data {

    private double temperature_c;
    private double pressure_hpa;
    private double humidity_percent;
    private long timestamp_ms;

    public BME240Data(double temperature_c, double pressure_hpa, double humidity_percent) {
        this.timestamp_ms = System.currentTimeMillis();
        this.temperature_c = temperature_c;
        this.pressure_hpa = pressure_hpa;
        this.humidity_percent = humidity_percent;
    }

    public double getTemperature_c() {
        return temperature_c;
    }

    public double getPressure_hpa() {
        return pressure_hpa;
    }

    public double getHumidity_percent() {
        return humidity_percent;
    }

    public long getTimestamp_ms() {
        return timestamp_ms;
    }
}
