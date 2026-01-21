package com.rest.web_app_gestion_reservation.service.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * A reusable TCP client that connects to the NotificationServer,
 * listens for messages on a background thread, and invokes a callback for each message.
 */
public class NotificationClient implements AutoCloseable {

    private static final String HOST = "localhost";
    private static final int PORT = 5001;

    private Socket socket;
    private Thread listenerThread;
    private volatile boolean isRunning = false;
    private final Consumer<String> onMessageReceived;

    public NotificationClient(Consumer<String> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public void start() {
        if (isRunning) return;
        isRunning = true;
        
        listenerThread = new Thread(() -> {
            try {
                socket = new Socket(HOST, PORT);
                try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    System.out.println("Connected to notification server. Waiting for messages...");
                    String line;
                    while (isRunning && (line = in.readLine()) != null) {
                        if (onMessageReceived != null) {
                            onMessageReceived.accept(line);
                        }
                    }
                }
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Connection to notification server lost: " + e.getMessage());
                }
            } finally {
                isRunning = false;
                System.out.println("Notification client stopped.");
            }
        });
        listenerThread.setDaemon(true); // Ensure thread doesn't prevent application exit
        listenerThread.start();
    }

    public void stop() {
        isRunning = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing notification client socket: " + e.getMessage());
        }
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
    }

    @Override
    public void close() {
        stop();
    }
}
