package com.rest.web_app_gestion_reservation.service.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS activator for the REST API.
 *
 * When deployed in a Jakarta EE / JAX-RS compatible container (e.g. Payara, WildFly),
 * the REST endpoints will be available under context-path + "/api", e.g.:
 *
 *   GET http://localhost:8080/your-app/api/rooms
 */
@ApplicationPath("/api")
public class RestApplication extends Application {
    // No extra config needed for basic usage; resources are auto-discovered.
}

