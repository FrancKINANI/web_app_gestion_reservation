package com.rest.web_app_gestion_reservation.service.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.rest.web_app_gestion_reservation.model.Room;
import com.rest.web_app_gestion_reservation.service.ReservationService;

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

    @GET
    @Path("/{roomId}/availability")
    public Response checkAvailability(@PathParam("roomId") long roomId,
                                      @QueryParam("start") String startIso,
                                      @QueryParam("end") String endIso) {
        // This method is intentionally kept high-level for the mini-project.
        // You would parse ISO-8601 date-times and delegate to ReservationService.isRoomAvailable(...)
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("Availability check endpoint: parse 'start'/'end' as ISO-8601 and call ReservationService.isRoomAvailable(...)")
                .build();
    }
}

