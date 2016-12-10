package org.digitalpanda.iot.raspberrypi;

import org.digitalpanda.iot.raspberrypi.sensor.BME240;

public class Application {


    public static void main(String...args) throws Exception{
        BME240 sensorTPH = new BME240();
        System.out.println("Temperature[C],Pressure[hPa],Humidity[%]");
        while(true){
            sensorTPH.fetchAndComputeValues();
            System.out.printf("%.2f,%.2f,%.2f%n",
                    sensorTPH.getTemperatureInDegreeCelsius(),
                    sensorTPH.getPressureInhPa(),
                    sensorTPH.getHumidityInPercent());
            Thread.sleep(1000);
        }
    }
}
