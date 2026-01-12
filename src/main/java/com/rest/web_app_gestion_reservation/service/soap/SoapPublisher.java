package com.rest.web_app_gestion_reservation.service.soap;

import jakarta.xml.ws.Endpoint;

/**
 * Simple standalone SOAP publisher.
 *
 * Run this main class to publish the SOAP service at:
 *   http://localhost:8081/soap/reservation
 *
 * The WSDL will be available at:
 *   http://localhost:8081/soap/reservation?wsdl
 */
public class SoapPublisher {

    public static void main(String[] args) {
        String url = "http://localhost:8081/soap/reservation";
        Endpoint.publish(url, new ReservationSoapService());
        System.out.println("SOAP service published at " + url);
    }
}

