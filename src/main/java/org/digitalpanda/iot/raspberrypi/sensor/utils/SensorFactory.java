package org.digitalpanda.iot.raspberrypi.sensor.utils;

import org.digitalpanda.iot.raspberrypi.sensor.Sensor;
import org.digitalpanda.iot.raspberrypi.sensor.SensorModel;
import org.digitalpanda.iot.raspberrypi.sensor.bme280.BME280;
import org.digitalpanda.iot.raspberrypi.sensor.bmp180.BMP180;

public class SensorFactory {

    public static Sensor getSensor(SensorModel sensorModel){
        switch (sensorModel) {
            case BME280:
                return new BME280();
            case BMP180:
                return new BMP180();
            default:
                return null;
        }
    }
}
