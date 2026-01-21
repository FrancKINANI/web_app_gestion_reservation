package com.rest.web_app_gestion_reservation.service.soap;

import com.rest.web_app_gestion_reservation.model.Reservation;
import com.rest.web_app_gestion_reservation.model.Room;
import com.rest.web_app_gestion_reservation.model.User;
import com.rest.web_app_gestion_reservation.model.dto.UserDTO;
import com.rest.web_app_gestion_reservation.service.ReservationService;
import com.rest.web_app_gestion_reservation.service.socket.NotificationServer;
import jakarta.jws.WebService;

import java.time.LocalDateTime;
import java.util.List;

@WebService(endpointInterface = "com.rest.web_app_gestion_reservation.service.soap.IReservationSoapService", serviceName = "ReservationSoapService")
public class ReservationSoapService implements IReservationSoapService {

    private final ReservationService reservationService = new ReservationService();

    @Override
    public List<Room> listAllRooms() {
        return reservationService.listAllRooms();
    }

    @Override
    public UserDTO authenticate(String username, String password) {
        User user = reservationService.authenticate(username, password);
        return user != null ? new UserDTO(user) : null;
    }

    @Override
    public UserDTO registerUser(String username, String email, String password, String fullName, boolean isAdmin) {
        User user = reservationService.registerUser(username, email, password, fullName, isAdmin);
        return user != null ? new UserDTO(user) : null;
    }

    @Override
    public Reservation createReservation(long userId, long roomId, String startIso, String endIso) {
        LocalDateTime start = LocalDateTime.parse(startIso);
        LocalDateTime end = LocalDateTime.parse(endIso);
        Reservation reservation = reservationService.createReservation(userId, roomId, start, end);
        if (reservation != null) {
            NotificationServer.getInstance().broadcast("NOUVELLE RESERVATION (SOAP): Salle " + reservation.getRoom().getName() + " réservée.");
        }
        return reservation;
    }

    @Override
    public boolean cancelReservation(long reservationId) {
        boolean success = reservationService.cancelReservation(reservationId);
        if (success) {
            NotificationServer.getInstance().broadcast("RESERVATION ANNULEE (SOAP): La réservation " + reservationId + " a été annulée.");
        }
        return success;
    }
}
