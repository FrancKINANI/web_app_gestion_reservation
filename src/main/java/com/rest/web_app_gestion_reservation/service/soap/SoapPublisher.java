package com.rest.web_app_gestion_reservation.service.soap;

import jakarta.xml.ws.Endpoint;

public class SoapPublisher {

    private static final String URL = "http://localhost:8081/soap/reservation";

    public static void publish() {
        Endpoint.publish(URL, new ReservationSoapService());
    }

    public static void main(String[] args) {
        publish();
        System.out.println("SOAP service published at " + URL);
    }
}
