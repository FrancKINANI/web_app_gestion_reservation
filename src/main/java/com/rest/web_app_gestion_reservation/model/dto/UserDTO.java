package com.rest.web_app_gestion_reservation.model.dto;

import com.rest.web_app_gestion_reservation.model.User;

import java.io.Serializable;

public class UserDTO implements Serializable {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private boolean admin;

    public UserDTO() {}

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.admin = user.isAdmin();
    }

    // Standard getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
    
    // Helper to convert back to a domain object if needed
    public User toUser() {
        User user = new User(this.username, this.email, null, this.fullName, this.admin);
        // Note: The ID is not set here because it's a generated value.
        // We are creating a new User object, so we don't need to set the ID.
        return user;
    }
}
