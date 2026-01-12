package com.rest.web_app_gestion_reservation.service.rmi;

import com.rest.web_app_gestion_reservation.model.Reservation;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRmiService extends Remote {

    List<Reservation> listReservationsForUser(long userId) throws RemoteException;

    Reservation createReservation(long userId, long roomId, LocalDateTime start, LocalDateTime end)
            throws RemoteException;

    boolean cancelReservation(long reservationId) throws RemoteException;
}
