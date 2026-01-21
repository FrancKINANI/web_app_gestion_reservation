package com.rest.web_app_gestion_reservation.service.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.function.Consumer;

public class NotificationClient implements Runnable {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8082;
    private static final int MAX_RETRIES = 5; // Max number of connection attempts
    private static final long RETRY_DELAY_MS = 2000; // 2 seconds between attempts

    private final Consumer<String> onMessageReceived;
    private volatile boolean running = true;
    private Socket socket;

    public NotificationClient(Consumer<String> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    @Override
    public void run() {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            if (!running) return; // Exit if stopped during retries

            try {
                socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                System.out.println("Notification client connected to " + SERVER_ADDRESS + ":" + SERVER_PORT);
                break; // Connection successful, exit the retry loop
            } catch (IOException e) {
                System.err.println("Could not connect to notification server (attempt " + attempt + "/" + MAX_RETRIES + "). Retrying...");
                if (attempt == MAX_RETRIES) {
                    System.err.println("All attempts to connect to the notification server have failed.");
                    return; // All retries failed, stop the client thread
                }
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    System.err.println("Notification client retry wait was interrupted.");
                    return;
                }
            }
        }

        if (socket == null || !running) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;
            while (running && (message = reader.readLine()) != null) {
                onMessageReceived.accept(message);
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Lost connection to notification server: " + e.getMessage());
            }
        } finally {
            stop();
        }
    }

    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
                System.out.println("Notification client disconnected.");
            } catch (IOException e) {
                System.err.println("Error closing notification client socket: " + e.getMessage());
            }
        }
    }
}
