package com.rest.web_app_gestion_reservation.service.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RmiServer {

    public static void main(String[] args) throws Exception {
        ReservationRmiServiceImpl service = new ReservationRmiServiceImpl();
        Registry registry = LocateRegistry.createRegistry(1099);
        registry.rebind("ReservationRmiService", service);
        System.out.println("RMI server started on port 1099.");
    }
}

