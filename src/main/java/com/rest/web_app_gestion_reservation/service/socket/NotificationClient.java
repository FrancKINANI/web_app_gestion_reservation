package com.rest.web_app_gestion_reservation.service.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Simple TCP client that connects to the NotificationServer and prints
 * any notification lines received from the server.
 *
 * Usage (after starting NotificationServer):
 *   - Run this main, it will connect to localhost:5000 and log messages.
 */
public class NotificationClient {

    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("localhost", 5000);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to notification server. Waiting for messages...");
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Notification: " + line);
            }
        }
    }
}

