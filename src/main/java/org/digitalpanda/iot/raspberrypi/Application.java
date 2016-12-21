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
import java.util.concurrent.TimeUnit;

public class Application {

    private final String targetRestEndpoint;
    private final Gson gsonSerializer;
    private final HttpClient httpClient;
    private final BME240 sensorTPH;
    private final String sensorLocation;

    public static void main(String...args) {
        (new Application()).start();
    }

    private Application(){
        this.targetRestEndpoint = System.getenv("TARGET_REST_ENDPOINT");
        this.sensorLocation = System.getenv("SENSOR_LOCATION");
        this.gsonSerializer = (new GsonBuilder()).create();
        this.httpClient = new HttpClient();
        this.sensorTPH = new BME240();
    }

    private void start(){
        if(!this.init()) System.exit(1);
        while(true){
            try {
                transmitToRestEndpoint(fetchAndDisplayMeasuresFromSensor());
                Thread.sleep(1000);
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
        try {
            this.httpClient.start();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        System.out.println(">,Time[ms],Temperature[C],Pressure[hPa],Humidity[%]");
        return sensorTPH.initialize();
    }

    private SensorMeasures fetchAndDisplayMeasuresFromSensor() throws IOException {
        BME240Data tphSensorData = this.sensorTPH.fetchAndComputeValues();
        System.out.printf(">,%d,%.2f,%.2f,%.2f%n",
                tphSensorData.getTimestamp_ms(),
                tphSensorData.getTemperature_c(),
                tphSensorData.getPressure_hpa(),
                tphSensorData.getHumidity_percent());
        return SensorDataMapper.create(tphSensorData, this.sensorLocation);
    }

    private void transmitToRestEndpoint(SensorMeasures sensorData){
        try {
            ContentResponse response =
                    this.httpClient.POST(this.targetRestEndpoint)
                            .header(HttpHeader.CONTENT_TYPE, "application/json")
                            .content(new StringContentProvider(this.gsonSerializer.toJson(sensorData),
                                    "utf-8"))
                            .timeout(500L, TimeUnit.MILLISECONDS)
                            .send();
            response.getStatus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
