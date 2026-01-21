package com.rest.web_app_gestion_reservation.model.dto;

import com.rest.web_app_gestion_reservation.model.Reservation;
import java.io.Serializable;
import java.time.LocalDateTime;

public class ReservationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String userName;
    private String roomName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    public ReservationDTO() {
        // No-argument constructor for JSON/RMI deserialization
    }

    public ReservationDTO(Reservation reservation) {
        this.id = reservation.getId();
        this.userName = reservation.getUser() != null ? reservation.getUser().getUsername() : "N/A";
        this.roomName = reservation.getRoom() != null ? reservation.getRoom().getName() : "N/A";
        this.startDateTime = reservation.getStartDateTime();
        this.endDateTime = reservation.getEndDateTime();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }
}
