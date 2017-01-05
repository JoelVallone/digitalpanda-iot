package org.digitalpanda.iot.raspberrypi;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.digitalpanda.backend.data.SensorMeasures;
import org.digitalpanda.iot.raspberrypi.sensor.Sensor;
import org.digitalpanda.iot.raspberrypi.sensor.SensorData;
import org.digitalpanda.iot.raspberrypi.sensor.utils.SensorDataMapper;
import org.digitalpanda.iot.raspberrypi.sensor.SensorModel;
import org.digitalpanda.iot.raspberrypi.sensor.utils.SensorFactory;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Application {

    private String targetRestEndpoint;
    private String sensorLocation;
    private SensorModel sensorModel;


    private final Gson gsonSerializer;
    private final HttpClient httpClient;
    private Sensor sensorTPH;

    public static void main(String...args) {
        (new Application()).start();
    }

    private Application(){
        this.targetRestEndpoint = System.getenv("TARGET_REST_ENDPOINT");
        if(targetRestEndpoint == null) targetRestEndpoint = "http://localhost:8080/sensor";
        this.sensorLocation = System.getenv("SENSOR_LOCATION");
        if(sensorLocation == null) sensorLocation = "indoor";
        this.sensorModel = SensorModel.valueOf(System.getenv("SENSOR_MODEL"));
        this.gsonSerializer = (new GsonBuilder()).create();
        this.httpClient = new HttpClient();
    }

    private void start(){
        if(!this.init()) System.exit(1);
        while(true){
            try {
                long startTime = System.currentTimeMillis();
                transmitToRestEndpoint(fetchAndDisplayMeasuresFromSensor());
                long sleepTime = 1000L - (System.currentTimeMillis() - startTime);
                Thread.sleep(sleepTime > 0L ? sleepTime : 1L);
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
            System.out.println(System.getenv("SENSOR_LOCATION = " + sensorLocation));
        }
        if (sensorModel == null ) {
            System.err.println("environment variable SENSOR_MODEL not set" );
            return false;
        }else{
            System.out.println(System.getenv("SENSOR_MODEL = " + sensorModel));
        }
        try {
            this.httpClient.start();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        System.out.println(">," + (new SensorData(sensorModel)).csvHeader());
        this.sensorTPH = SensorFactory.getSensor(sensorModel);
        return sensorTPH.initialize();
    }

    private List<SensorMeasures> fetchAndDisplayMeasuresFromSensor() throws IOException {
        SensorData tphSensorData = this.sensorTPH.fetchAndComputeValues();
        System.out.printf(">," + tphSensorData.csvData());
        return SensorDataMapper.create(tphSensorData, this.sensorLocation);
    }

    private void transmitToRestEndpoint(List<SensorMeasures> sensorData){
        try {
            String sensorDataJson = this.gsonSerializer.toJson(sensorData);
            System.out.println("send to " + targetRestEndpoint + " POST: \n" + sensorDataJson);
            ContentResponse response =
                    this.httpClient.POST(this.targetRestEndpoint)
                            .header(HttpHeader.CONTENT_TYPE, "application/json")
                            .content(new StringContentProvider(sensorDataJson))
                            .timeout(1000L, TimeUnit.MILLISECONDS)
                            .send();
            if(response.getStatus() != HttpStatus.Code.OK.getCode() ){
                System.out.println("response status code = " + response.getStatus());
                System.out.println("message = " + response.getContentAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
