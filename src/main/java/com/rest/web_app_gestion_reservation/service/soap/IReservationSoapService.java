package com.rest.web_app_gestion_reservation.service.soap;

import com.rest.web_app_gestion_reservation.model.Reservation;
import com.rest.web_app_gestion_reservation.model.Room;
import com.rest.web_app_gestion_reservation.model.dto.UserDTO;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

import java.util.List;

@WebService
public interface IReservationSoapService {

    @WebMethod
    List<Room> listAllRooms();

    @WebMethod
    UserDTO authenticate(@WebParam(name = "username") String username, @WebParam(name = "password") String password);

    @WebMethod
    UserDTO registerUser(
            @WebParam(name = "username") String username,
            @WebParam(name = "email") String email,
            @WebParam(name = "password") String password,
            @WebParam(name = "fullName") String fullName,
            @WebParam(name = "isAdmin") boolean isAdmin);

    @WebMethod
    Reservation createReservation(
            @WebParam(name = "userId") long userId,
            @WebParam(name = "roomId") long roomId,
            @WebParam(name = "start") String startIso,
            @WebParam(name = "end") String endIso);

    @WebMethod
    boolean cancelReservation(@WebParam(name = "reservationId") long reservationId);
}
