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

import static org.digitalpanda.iot.raspberrypi.Configuration.ConfigurationKey.BACKEND_URL;
import static org.digitalpanda.iot.raspberrypi.Configuration.ConfigurationKey.SENSOR_LOCATION;
import static org.digitalpanda.iot.raspberrypi.Configuration.ConfigurationKey.SENSOR_MODEL;

public class Application {

    private Configuration configuration;

    private final Gson gsonSerializer;
    private final HttpClient httpClient;
    private Sensor sensorTPH;

    public static void main(String...args) {
        (new Application()).start();
    }

    private Application(){
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
        this.configuration = Configuration.getInstance();
        System.out.println(configuration);
        try {
            this.httpClient.start();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        SensorModel sensorModel = SensorModel.valueOf(configuration.getValue(SENSOR_MODEL));
        System.out.println(">," + (new SensorData(sensorModel)).csvHeader());
        this.sensorTPH = SensorFactory.getSensor(sensorModel);
        return sensorTPH.initialize();
    }

    private List<SensorMeasures> fetchAndDisplayMeasuresFromSensor() throws IOException {
        SensorData tphSensorData = this.sensorTPH.fetchAndComputeValues();
        System.out.println(">," + tphSensorData.csvData());
        return SensorDataMapper.create(tphSensorData, configuration.getValue(SENSOR_LOCATION));
    }

    private void transmitToRestEndpoint(List<SensorMeasures> sensorData){
        try {
            String sensorDataJson = this.gsonSerializer.toJson(sensorData);
            String targetRestEndpoint = configuration.getValue(BACKEND_URL);
            ContentResponse response =
                    this.httpClient.POST(targetRestEndpoint)
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
