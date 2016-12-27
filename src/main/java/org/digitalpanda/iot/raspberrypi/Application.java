package org.digitalpanda.iot.raspberrypi;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.digitalpanda.backend.data.SensorMeasures;
import org.digitalpanda.iot.raspberrypi.sensor.SensorDataMapper;
import org.digitalpanda.iot.raspberrypi.sensor.bme240.BME240;
import org.digitalpanda.iot.raspberrypi.sensor.bme240.BME240Data;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Application {

    private String targetRestEndpoint;
    private final Gson gsonSerializer;
    private final HttpClient httpClient;
    private final BME240 sensorTPH;
    private String sensorLocation;

    public static void main(String...args) {
        (new Application()).start();
    }

    private Application(){
        this.targetRestEndpoint = System.getenv("TARGET_REST_ENDPOINT");
        if(targetRestEndpoint == null) targetRestEndpoint = "http://localhost:8080/sensor";
        this.sensorLocation = System.getenv("SENSOR_LOCATION");
        if(sensorLocation == null) sensorLocation = "indoor";
        this.gsonSerializer = (new GsonBuilder()).create();
        this.httpClient = new HttpClient();
        this.sensorTPH = new BME240();
    }

    private void start(){
        if(!this.init()) System.exit(1);
        while(true){
            try {
                transmitToRestEndpoint(fetchAndDisplayMeasuresFromSensor());
                Thread.sleep(5000);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private boolean init(){
        if (targetRestEndpoint == null || targetRestEndpoint.length() == 0) {
            System.err.println("environment variable TARGET_REST_ENDPOINT not set" );
            return false;
        }else{
            System.out.println(System.getenv("TARGET_REST_ENDPOINT = " + targetRestEndpoint));
        }
        if (sensorLocation == null || sensorLocation.length() == 0) {
            System.err.println("environment variable SENSOR_LOCATION not set" );
            return false;
        }else{
            System.out.println(System.getenv("SENSOR_LOCATION = " + targetRestEndpoint));
        }
        try {
            this.httpClient.start();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        System.out.println(">,Time[ms],Temperature[C],Pressure[hPa],Humidity[%]");
        //return sensorTPH.initialize();
        return true;
    }

    private List<SensorMeasures> fetchAndDisplayMeasuresFromSensor() throws IOException {
        BME240Data tphSensorData = new BME240Data(24.0,750.0,65.0);
        //BME240Data tphSensorData = this.sensorTPH.fetchAndComputeValues();
        System.out.printf(">,%d,%.2f,%.2f,%.2f%n",
                tphSensorData.getTimestamp_ms(),
                tphSensorData.getTemperature_c(),
                tphSensorData.getPressure_hpa(),
                tphSensorData.getHumidity_percent());
        return SensorDataMapper.create(tphSensorData, this.sensorLocation);
    }

    private void transmitToRestEndpoint(List<SensorMeasures> sensorData){
        System.out.println("send to " + targetRestEndpoint + " POST: \n" + sensorData.toString());
        try {
            ContentResponse response =
                    this.httpClient.POST(this.targetRestEndpoint)
                            .header(HttpHeader.CONTENT_TYPE, "application/json")
                            .content(new StringContentProvider(this.gsonSerializer.toJson(sensorData)))
                            .timeout(500L, TimeUnit.MILLISECONDS)
                            .send();
            System.out.println("response status code = " + response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
