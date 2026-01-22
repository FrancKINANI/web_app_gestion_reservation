package com.rest.web_app_gestion_reservation.service.client;

import com.rest.web_app_gestion_reservation.model.Reservation;
import com.rest.web_app_gestion_reservation.model.User;
import com.rest.web_app_gestion_reservation.model.dto.UserDTO;
import com.rest.web_app_gestion_reservation.service.soap.IReservationSoapService;
import jakarta.xml.ws.Service;
import javax.xml.namespace.QName;
import java.net.URL;

public class SoapServiceClient {

    private IReservationSoapService soapService;

    public SoapServiceClient() {
        try {
            URL wsdlUrl = new URL("http://localhost:8081/soap/reservation?wsdl");
            QName serviceName = new QName("http://soap.service.web_app_gestion_reservation.rest.com/", "ReservationSoapService");
            Service service = Service.create(wsdlUrl, serviceName);
            this.soapService = service.getPort(IReservationSoapService.class);
        } catch (Exception e) {
            System.err.println("FATAL: Error initializing SOAP client. Is the SoapPublisher running?");
            System.err.println("Error: " + e.getMessage());
            this.soapService = null;
        }
    }

    public User authenticate(String username, String password) {
        if (soapService == null) return null;
        try {
            UserDTO userDTO = soapService.authenticate(username, password);
            if (userDTO != null) {
                // Convert DTO back to a User object for the application
                User user = new User(userDTO.getUsername(), userDTO.getEmail(), null, userDTO.getFullName(), userDTO.isAdmin());
                // Manually set the ID, which is now available from the DTO
                // This is a workaround since we can't directly modify the User constructor to accept an ID.
                try {
                    java.lang.reflect.Field idField = User.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(user, userDTO.getId());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    System.err.println("Error setting user ID via reflection: " + e.getMessage());
                }
                return user;
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error during SOAP authentication: " + e.getMessage());
            return null;
        }
    }

    public User registerUser(String username, String email, String password, String fullName, boolean isAdmin) {
        if (soapService == null) return null;
        try {
            UserDTO userDTO = soapService.registerUser(username, email, password, fullName, isAdmin);
             if (userDTO != null) {
                return userDTO.toUser(); // Use the DTO's conversion method
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error during SOAP user registration: " + e.getMessage());
            return null;
        }
    }

    public Reservation createReservation(long userId, long roomId, String startIso, String endIso) {
        if (soapService == null) return null;
        try {
            return soapService.createReservation(userId, roomId, startIso, endIso);
        } catch (Exception e) {
            System.err.println("Error during SOAP reservation creation: " + e.getMessage());
            return null;
        }
    }

    public boolean cancelReservation(long reservationId) {
        if (soapService == null) return false;
        try {
            return soapService.cancelReservation(reservationId);
        } catch (Exception e) {
            System.err.println("Error during SOAP reservation cancellation: " + e.getMessage());
            return false;
        }
    }
}
