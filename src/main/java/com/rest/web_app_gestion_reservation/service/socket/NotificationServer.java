package com.rest.web_app_gestion_reservation.service.socket;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationServer {

    private static final int PORT = 8082; // Standardized port
    private static volatile NotificationServer instance;
    private static final Object lock = new Object();

    private final Set<PrintWriter> clients = Collections.synchronizedSet(new HashSet<>());
    private final ExecutorService serverExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean isRunning = false;

    private NotificationServer() {
        // private constructor for singleton
    }

    public static NotificationServer getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new NotificationServer();
                    instance.startServerThread();
                }
            }
        }
        return instance;
    }

    private void startServerThread() {
        if (isRunning) return;
        isRunning = true;
        serverExecutor.submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Notification TCP server started on port " + PORT);
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        clients.add(out);
                        System.out.println("Notification client connected.");
                    } catch (IOException e) {
                        if (Thread.currentThread().isInterrupted()) {
                            System.out.println("Notification server shutting down.");
                            break;
                        }
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println("Could not start notification server on port " + PORT + ": " + e.getMessage());
                isRunning = false;
            }
        });
    }

    public void broadcast(String message) {
        if (!isRunning) return;
        synchronized (clients) {
            Iterator<PrintWriter> iterator = clients.iterator();
            while (iterator.hasNext()) {
                PrintWriter out = iterator.next();
                out.println(message);
                if (out.checkError()) {
                    iterator.remove();
                    System.out.println("Removed disconnected client.");
                }
            }
        }
        System.out.println("Broadcasted notification: " + message);
    }

    public void stop() {
        isRunning = false;
        serverExecutor.shutdownNow();
        synchronized (clients) {
            clients.forEach(PrintWriter::close);
            clients.clear();
        }
        System.out.println("Notification server stopped.");
    }

    public static void main(String[] args) {
        // To run standalone for testing
        NotificationServer.getInstance();
        
        // Keep main thread alive to allow server to run, or add shutdown logic
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
