package com.rest.web_app_gestion_reservation.service.rest;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/api")
public class RestApplication extends ResourceConfig {
    public RestApplication() {
        // Register REST resource classes by package
        packages("com.rest.web_app_gestion_reservation.service.rest");
    }
}
