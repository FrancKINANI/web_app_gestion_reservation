package com.rest.web_app_gestion_reservation.service.rest;

import com.rest.web_app_gestion_reservation.model.dto.ReservationDTO;
import com.rest.web_app_gestion_reservation.service.socket.NotificationServer;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.rest.web_app_gestion_reservation.model.Reservation;
import com.rest.web_app_gestion_reservation.service.ReservationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Path("/reservations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReservationResource {

    private final ReservationService reservationService = new ReservationService();

    @GET
    public List<ReservationDTO> getAllReservations() {
        List<Reservation> reservations = reservationService.listAllReservations();
        return reservations.stream().map(ReservationDTO::new).collect(Collectors.toList());
    }

    @GET
    @Path("/user/{userId}")
    public List<Reservation> getReservationsForUser(@PathParam("userId") long userId) {
        // This one is likely not used by the REST client, but we'll leave it for now.
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

            NotificationServer.getInstance().broadcast("NOUVELLE RESERVATION: Salle " + reservation.getRoom().getName() + " réservée par " + reservation.getUser().getUsername());

            return Response.status(Response.Status.CREATED).entity(new ReservationDTO(reservation)).build();
        } catch (DateTimeParseException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Format de date/heure invalide, utiliser ISO-8601.")
                    .build();
        }
    }

    // Other methods (PUT, DELETE) would also benefit from returning DTOs, but we'll focus on the GET problem for now.
    @PUT
    @Path("/{reservationId}")
    public Response updateReservation(@PathParam("reservationId") long reservationId,
                                      @QueryParam("roomId") long roomId,
                                      @QueryParam("start") String startIso,
                                      @QueryParam("end") String endIso) {
        try {
            LocalDateTime start = LocalDateTime.parse(startIso);
            LocalDateTime end = LocalDateTime.parse(endIso);

            Reservation updatedReservation = reservationService.updateReservation(reservationId, roomId, start, end);
            if (updatedReservation == null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Conflit de réservation ou paramètres invalides pour la mise à jour.")
                        .build();
            }

            NotificationServer.getInstance().broadcast("RESERVATION MISE A JOUR: La réservation " + reservationId + " a été modifiée.");

            return Response.ok(new ReservationDTO(updatedReservation)).build();
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

        NotificationServer.getInstance().broadcast("RESERVATION ANNULEE: La réservation " + reservationId + " a été annulée.");

        return Response.noContent().build();
    }
}
