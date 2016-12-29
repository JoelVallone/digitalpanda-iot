package org.digitalpanda.iot.raspberrypi.sensor;

import org.digitalpanda.backend.data.SensorMeasure;
import org.digitalpanda.backend.data.SensorMeasureType;
import org.digitalpanda.backend.data.SensorMeasureMetaData;
import org.digitalpanda.backend.data.SensorMeasures;
import org.digitalpanda.iot.raspberrypi.sensor.bme240.BME240Data;

import java.util.ArrayList;
import java.util.List;

public class SensorDataMapper {

    public static List<SensorMeasures> create(BME240Data bme240Data, String location){
        if (bme240Data == null || location == null) throw new RuntimeException("Some of the input data were null");
        final List<SensorMeasures> measures = new ArrayList<>(3);
        return append(measures, bme240Data,location);
    }

    public static List<SensorMeasures> append(List<SensorMeasures> sensorMeasures, BME240Data bme240Data, String location){
        if (sensorMeasures == null || bme240Data == null || location == null) throw new RuntimeException("Some of the input data were null");
        setValue(sensorMeasures, location, bme240Data.getTimestamp_ms(), SensorMeasureType.HUMIDITY, bme240Data.getHumidity_percent());
        setValue(sensorMeasures, location, bme240Data.getTimestamp_ms(), SensorMeasureType.TEMPERATURE, bme240Data.getTemperature_c());
        setValue(sensorMeasures, location, bme240Data.getTimestamp_ms(), SensorMeasureType.PRESSURE, bme240Data.getPressure_hpa());
        return  sensorMeasures;
    }

    private static void setValue(List<SensorMeasures> sensorMeasuresList,
                                 String location,
                                 long timestamp,
                                 SensorMeasureType type,
                                 double value){
        SensorMeasures measures = sensorMeasuresList.stream()
                                    .filter((sensorMeasures) -> sensorMeasures.getSensorMeasureMetaData().getType().equals(type))
                                    .findFirst()
                                    .orElse(null);
        List<SensorMeasure> measureList;
        if (measures == null){
            measureList = new ArrayList<>();
            measures = new SensorMeasures(new SensorMeasureMetaData(location,type), measureList);
            sensorMeasuresList.add(measures);
        }else{
            measureList = measures.getMeasures();
        }
        measureList.add(new SensorMeasure(timestamp, value));
    }
}
