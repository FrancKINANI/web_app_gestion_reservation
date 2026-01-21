package com.rest.web_app_gestion_reservation.service.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RmiServer {

    public static void start() throws Exception {
        // The RMI service registry
        Registry registry = LocateRegistry.createRegistry(1099);

        // Bind the implementation with a specific name
        registry.rebind("ReservationRmiService", new ReservationRmiServiceImpl());
    }

    public static void main(String[] args) {
        try {
            start();
            System.out.println("RMI server is running and bound to 'ReservationRmiService'...");
        } catch (Exception e) {
            System.err.println("RMI server failed to start:");
            e.printStackTrace();
        }
    }
}
