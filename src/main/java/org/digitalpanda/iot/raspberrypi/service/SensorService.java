package org.digitalpanda.iot.raspberrypi.service;


import org.springframework.stereotype.Service;

@Service
public class SensorService {

    public SensorService(){}
    public String getInfo(){
        return "{ \"text\" : \"Got it!\" }";
    }
}
