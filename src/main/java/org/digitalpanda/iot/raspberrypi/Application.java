package org.digitalpanda.iot.raspberrypi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Application {
    public static void main(String...args) throws Exception{
        SpringApplication.run(Application.class, args);
    /*
        BME240 sensorTPH = new BME240();
        System.out.println("Temperature[C],Pressure[hPa],Humidity[%]");
        while(true){
            sensorTPH.fetchAndComputeValues();
            System.out.printf("%.2f,%.2f,%.2f%n",
                    sensorTPH.getTemperatureInDegreeCelsius(),
                    sensorTPH.getPressureInhPa(),
                    sensorTPH.getHumidityInPercent());
            Thread.sleep(1000);
        }*/
    }
}
