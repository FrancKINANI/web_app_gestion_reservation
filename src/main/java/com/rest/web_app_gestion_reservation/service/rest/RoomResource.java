package com.rest.web_app_gestion_reservation.service.rest;

import com.rest.web_app_gestion_reservation.model.Room;
import com.rest.web_app_gestion_reservation.service.ReservationService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final ReservationService reservationService = new ReservationService();

    @GET
    public List<Room> getRooms() {
        return reservationService.listAllRooms();
    }

    @POST
    public Response createRoom(Room room) {
        Room newRoom = reservationService.saveRoom(room);
        if (newRoom == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to create room.")
                    .build();
        }
        return Response.status(Response.Status.CREATED).entity(newRoom).build();
    }

    @PUT
    @Path("/{roomId}")
    public Response updateRoom(@PathParam("roomId") long roomId, Room room) {
        // Ensure the ID from the path is set on the object to be updated
        room.setId(roomId);
        Room updatedRoom = reservationService.saveRoom(room);
        if (updatedRoom == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to update room.")
                    .build();
        }
        return Response.ok(updatedRoom).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") long roomId) {
        if (reservationService.deleteRoom(roomId)) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("Room not found or could not be deleted.")
                .build();
    }

    @GET
    @Path("/{roomId}/availability")
    public Response checkAvailability(@PathParam("roomId") long roomId,
                                      @QueryParam("start") String startIso,
                                      @QueryParam("end") String endIso) {
        try {
            LocalDateTime start = LocalDateTime.parse(startIso);
            LocalDateTime end = LocalDateTime.parse(endIso);

            boolean isAvailable = reservationService.isRoomAvailable(roomId, start, end, null);

            return Response.ok(Collections.singletonMap("available", isAvailable)).build();

        } catch (DateTimeParseException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Collections.singletonMap("error", "Invalid date/time format, please use ISO-8601."))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Collections.singletonMap("error", "An unexpected error occurred."))
                    .build();
        }
    }
}
