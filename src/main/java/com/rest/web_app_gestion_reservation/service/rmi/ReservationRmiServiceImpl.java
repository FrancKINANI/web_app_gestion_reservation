package com.rest.web_app_gestion_reservation.service.rmi;

import com.rest.web_app_gestion_reservation.model.Reservation;
import com.rest.web_app_gestion_reservation.model.dto.ReservationDTO;
import com.rest.web_app_gestion_reservation.service.ReservationService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationRmiServiceImpl extends UnicastRemoteObject implements ReservationRmiService {

    private final ReservationService reservationService = new ReservationService();

    public ReservationRmiServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public boolean isRoomAvailable(long roomId, LocalDateTime start, LocalDateTime end) throws RemoteException {
        return reservationService.isRoomAvailable(roomId, start, end, null);
    }

    @Override
    public List<ReservationDTO> listReservationsForUser(long userId) throws RemoteException {
        List<Reservation> reservations = reservationService.listReservationsForUser(userId);
        // Convert entities to DTOs before sending them over the wire
        return reservations.stream().map(ReservationDTO::new).collect(Collectors.toList());
    }

    @Override
    public ReservationDTO createReservation(long userId, long roomId, LocalDateTime start, LocalDateTime end)
            throws RemoteException {
        Reservation reservation = reservationService.createReservation(userId, roomId, start, end);
        return reservation != null ? new ReservationDTO(reservation) : null;
    }

    @Override
    public boolean cancelReservation(long reservationId) throws RemoteException {
        return reservationService.cancelReservation(reservationId);
    }
}
