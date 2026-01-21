package com.rest.web_app_gestion_reservation;

import com.rest.web_app_gestion_reservation.service.rest.RestApplication;
import com.rest.web_app_gestion_reservation.service.rmi.RmiServer;
import com.rest.web_app_gestion_reservation.service.socket.NotificationServer;
import com.rest.web_app_gestion_reservation.service.soap.SoapPublisher;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class BackendServer {

    private static final String BASE_URI = "http://localhost:8080/";

    public static HttpServer startRestServer() {
        final ResourceConfig rc = new RestApplication();
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) {
        try {
            // Start the REST server
            final HttpServer restServer = startRestServer();
            System.out.println(String.format("REST services available at %sapi/", BASE_URI));

            // Start the Notification server
            NotificationServer.getInstance();
            System.out.println("Notification Server module started.");

            // Start the RMI server
            RmiServer.start();
            System.out.println("RMI Server module started.");

            // Start the SOAP publisher
            SoapPublisher.publish();
            System.out.println("SOAP Publisher module started.");

            System.out.println("\n--- All Backend Services are running ---");
            System.out.println("Press Ctrl+C to stop the server.");

            // Keep the main thread alive
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Failed to start backend services.");
            e.printStackTrace();
        }
    }
}
