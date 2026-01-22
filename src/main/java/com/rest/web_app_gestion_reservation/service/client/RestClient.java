package com.rest.web_app_gestion_reservation.service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import com.rest.web_app_gestion_reservation.model.Room;
import com.rest.web_app_gestion_reservation.model.dto.ReservationDTO;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;

public class RestClient {

    private static final String BASE_URI = "http://localhost:8080/api";
    private final Client client;

    public RestClient() {
        // Create an ObjectMapper and configure it to handle Java 8 dates
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Create a provider that uses our configured mapper
        JacksonJsonProvider jacksonProvider = new JacksonJsonProvider();
        jacksonProvider.setMapper(objectMapper);

        // Build the client with the custom provider
        this.client = ClientBuilder.newClient().register(jacksonProvider);
    }

    public List<Room> getRooms() {
        try {
            return client.target(BASE_URI)
                    .path("rooms")
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<Room>>() {});
        } catch (Exception e) {
            System.err.println("Error fetching rooms via REST: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public Room saveRoom(Room room) {
        // This method assumes the server returns the full Room object, which is fine.
        try {
            Response response = client.target(BASE_URI)
                    .path("rooms")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(room));
            if (response.getStatus() == Response.Status.CREATED.getStatusCode() || response.getStatus() == Response.Status.OK.getStatusCode()) {
                return response.readEntity(Room.class);
            }
        } catch (Exception e) {
            System.err.println("Error saving room via REST: " + e.getMessage());
        }
        return null;
    }

    public boolean deleteRoom(Long roomId) {
        try {
            Response response = client.target(BASE_URI)
                    .path("rooms").path(String.valueOf(roomId))
                    .request()
                    .delete();
            return response.getStatus() == Response.Status.NO_CONTENT.getStatusCode();
        } catch (Exception e) {
            System.err.println("Error deleting room via REST: " + e.getMessage());
            return false;
        }
    }

    public List<ReservationDTO> getAllReservations() {
        try {
            return client.target(BASE_URI)
                    .path("reservations")
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<ReservationDTO>>() {}); // Expecting a list of DTOs
        } catch (Exception e) {
            System.err.println("Error fetching all reservations via REST: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for more details
            return Collections.emptyList();
        }
    }
}
