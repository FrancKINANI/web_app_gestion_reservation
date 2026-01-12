package com.rest.web_app_gestion_reservation.service.soap;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import com.rest.web_app_gestion_reservation.model.Reservation;
import com.rest.web_app_gestion_reservation.service.ReservationService;

import java.time.LocalDateTime;

@WebService(serviceName = "ReservationSoapService")
public class ReservationSoapService {

    private final ReservationService reservationService = new ReservationService();

    @WebMethod
    public Reservation createReservation(
            @WebParam(name = "userId") long userId,
            @WebParam(name = "roomId") long roomId,
            @WebParam(name = "start") String startIso,
            @WebParam(name = "end") String endIso) {
        LocalDateTime start = LocalDateTime.parse(startIso);
        LocalDateTime end = LocalDateTime.parse(endIso);
        return reservationService.createReservation(userId, roomId, start, end);
    }

    @WebMethod
    public boolean cancelReservation(
            @WebParam(name = "reservationId") long reservationId) {
        return reservationService.cancelReservation(reservationId);
    }
}
