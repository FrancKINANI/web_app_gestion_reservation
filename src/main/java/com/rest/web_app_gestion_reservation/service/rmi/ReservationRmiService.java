package com.rest.web_app_gestion_reservation.service.rmi;

import com.rest.web_app_gestion_reservation.model.dto.ReservationDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRmiService extends Remote {

    boolean isRoomAvailable(long roomId, LocalDateTime start, LocalDateTime end) throws RemoteException;

    List<ReservationDTO> listReservationsForUser(long userId) throws RemoteException;

    ReservationDTO createReservation(long userId, long roomId, LocalDateTime start, LocalDateTime end)
            throws RemoteException;

    boolean cancelReservation(long reservationId) throws RemoteException;
}
