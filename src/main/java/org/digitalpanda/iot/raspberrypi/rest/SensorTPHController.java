package org.digitalpanda.iot.raspberrypi.rest;


import org.digitalpanda.iot.raspberrypi.service.SensorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SensorTPHController {

    @Autowired
    private SensorService sensorService;

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @CrossOrigin
    @RequestMapping("/greeting")
    public String getIt() {
        return sensorService.getInfo();
    }
}