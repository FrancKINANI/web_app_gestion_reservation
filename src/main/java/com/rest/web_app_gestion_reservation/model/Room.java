package com.rest.web_app_gestion_reservation.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type;

    @Column(nullable = false)
    private int capacity;

    @Column
    private String location;

    @Column(length = 500)
    private String description;

    @Column
    private boolean whiteboard;

    @Column
    private boolean projector;

    @Column
    private boolean videoConference;

    @Column
    private boolean microphones;

    @Column
    private boolean presentationScreen;

    public Room() {
    }

    public Room(String name, RoomType type, int capacity, String location) {
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.location = location;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isWhiteboard() {
        return whiteboard;
    }

    public void setWhiteboard(boolean whiteboard) {
        this.whiteboard = whiteboard;
    }

    public boolean isProjector() {
        return projector;
    }

    public void setProjector(boolean projector) {
        this.projector = projector;
    }

    public boolean isVideoConference() {
        return videoConference;
    }

    public void setVideoConference(boolean videoConference) {
        this.videoConference = videoConference;
    }

    public boolean isMicrophones() {
        return microphones;
    }

    public void setMicrophones(boolean microphones) {
        this.microphones = microphones;
    }

    public boolean isPresentationScreen() {
        return presentationScreen;
    }

    public void setPresentationScreen(boolean presentationScreen) {
        this.presentationScreen = presentationScreen;
    }

    @Override
    public String toString() {
        return name;
    }
}
