package org.digitalpanda.iot.raspberrypi.sensor;

import org.digitalpanda.backend.data.SensorMeasure;
import org.digitalpanda.backend.data.SensorMeasureEnum;
import org.digitalpanda.backend.data.SensorMeasureMetaData;
import org.digitalpanda.backend.data.SensorMeasures;
import org.digitalpanda.iot.raspberrypi.sensor.bme240.BME240Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensorDataMapper {

    public static SensorMeasures create(BME240Data bme240Data, String location){
        if (bme240Data == null || location == null) throw new RuntimeException("Some of the input data were null");
        final Map<SensorMeasureMetaData, List<SensorMeasure>> measuresMap = new HashMap<>();
        SensorMeasures sensorMeasures = new SensorMeasures(measuresMap);
        return append(sensorMeasures,bme240Data,location);
    }

    public static SensorMeasures append(SensorMeasures sensorMeasures, BME240Data bme240Data, String location){
        if (sensorMeasures == null || bme240Data == null || location == null) throw new RuntimeException("Some of the input data were null");
        setValue(sensorMeasures, location, bme240Data.getTimestamp_ms(), SensorMeasureEnum.HUMIDITY, bme240Data.getHumidity_percent());
        setValue(sensorMeasures, location, bme240Data.getTimestamp_ms(), SensorMeasureEnum.TEMPERATURE, bme240Data.getPressure_hpa());
        setValue(sensorMeasures, location, bme240Data.getTimestamp_ms(), SensorMeasureEnum.PRESSURE, bme240Data.getPressure_hpa());
        return  sensorMeasures;
    }

    private static void setValue(SensorMeasures sensorMeasures,
                                 String location,
                                 long timestamp,
                                 SensorMeasureEnum type,
                                 double value){
        final Map<SensorMeasureMetaData, List<SensorMeasure>> measuresMap = sensorMeasures.getMeasures();
        SensorMeasureMetaData sensorMeasureMetaData = new SensorMeasureMetaData(location, type);
        List<SensorMeasure> measures = measuresMap.get(sensorMeasureMetaData);
        if(measures == null){
            measures = new ArrayList<>();
            measuresMap.put(sensorMeasureMetaData, measures);
        }
        measures.add(new SensorMeasure(timestamp, value));
    }
}
