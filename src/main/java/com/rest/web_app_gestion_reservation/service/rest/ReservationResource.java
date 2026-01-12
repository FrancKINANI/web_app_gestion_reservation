package com.rest.web_app_gestion_reservation.service.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.rest.web_app_gestion_reservation.model.Reservation;
import com.rest.web_app_gestion_reservation.service.ReservationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Path("/reservations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReservationResource {

    private final ReservationService reservationService = new ReservationService();

    @GET
    @Path("/user/{userId}")
    public List<Reservation> getReservationsForUser(@PathParam("userId") long userId) {
        return reservationService.listReservationsForUser(userId);
    }

    @POST
    public Response createReservation(@QueryParam("userId") long userId,
            @QueryParam("roomId") long roomId,
            @QueryParam("start") String startIso,
            @QueryParam("end") String endIso) {
        try {
            LocalDateTime start = LocalDateTime.parse(startIso);
            LocalDateTime end = LocalDateTime.parse(endIso);

            Reservation reservation = reservationService.createReservation(userId, roomId, start, end);
            if (reservation == null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Conflit de réservation ou paramètres invalides.")
                        .build();
            }
            return Response.status(Response.Status.CREATED).entity(reservation).build();
        } catch (DateTimeParseException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Format de date/heure invalide, utiliser ISO-8601.")
                    .build();
        }
    }

    @DELETE
    @Path("/{reservationId}")
    public Response cancelReservation(@PathParam("reservationId") long reservationId) {
        boolean ok = reservationService.cancelReservation(reservationId);
        if (!ok) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Impossible d'annuler la réservation.")
                    .build();
        }
        return Response.noContent().build();
    }
}
