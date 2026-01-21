package com.rest.web_app_gestion_reservation.service.client;

import com.rest.web_app_gestion_reservation.model.Reservation;
import com.rest.web_app_gestion_reservation.model.dto.ReservationDTO;
import com.rest.web_app_gestion_reservation.service.ReservationService;
import com.rest.web_app_gestion_reservation.service.rmi.ReservationRmiService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RmiClient {

    private ReservationRmiService rmiService;
    private final ReservationService reservationService = new ReservationService();

    public RmiClient() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            this.rmiService = (ReservationRmiService) registry.lookup("ReservationRmiService");
        } catch (Exception e) {
            System.err.println("FATAL: Error initializing RMI client. Is the RmiServer running?");
            System.err.println("Error: " + e.getMessage());
            this.rmiService = null; // Ensure service is null on failure
        }
    }

    public List<Reservation> listReservationsForUser(long userId) {
        if (rmiService == null) {
            System.err.println("RMI service is not available. Returning empty list.");
            return Collections.emptyList();
        }
        try {
            List<ReservationDTO> dtos = rmiService.listReservationsForUser(userId);
            // We need to convert DTOs back to full Reservation objects to satisfy the controller
            // This is not ideal, but it's the quickest fix.
            // A better solution would be to make the controller use DTOs directly.
            return dtos.stream()
                    .map(dto -> reservationService.findReservationById(dto.getId())) // Fetch the full entity
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error during RMI listReservationsForUser: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Reservation createReservation(long userId, long roomId, LocalDateTime start, LocalDateTime end) {
        if (rmiService == null) {
            System.err.println("RMI service is not available. Cannot create reservation.");
            return null;
        }
        try {
            ReservationDTO dto = rmiService.createReservation(userId, roomId, start, end);
            return dto != null ? reservationService.findReservationById(dto.getId()) : null;
        } catch (Exception e) {
            System.err.println("Error during RMI createReservation: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean cancelReservation(long reservationId) {
        if (rmiService == null) {
            System.err.println("RMI service is not available. Cannot cancel reservation.");
            return false;
        }
        try {
            return rmiService.cancelReservation(reservationId);
        } catch (Exception e) {
            System.err.println("Error during RMI cancelReservation: " + e.getMessage());
            return false;
        }
    }
}
