package com.rest.web_app_gestion_reservation.service.rmi;

import com.rest.web_app_gestion_reservation.model.Reservation;
import com.rest.web_app_gestion_reservation.service.ReservationService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationRmiServiceImpl extends UnicastRemoteObject implements ReservationRmiService {

    private final ReservationService reservationService = new ReservationService();

    public ReservationRmiServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public List<Reservation> listReservationsForUser(long userId) throws RemoteException {
        return reservationService.listReservationsForUser(userId);
    }

    @Override
    public Reservation createReservation(long userId, long roomId, LocalDateTime start, LocalDateTime end)
            throws RemoteException {
        return reservationService.createReservation(userId, roomId, start, end);
    }

    @Override
    public boolean cancelReservation(long reservationId) throws RemoteException {
        return reservationService.cancelReservation(reservationId);
    }
}
