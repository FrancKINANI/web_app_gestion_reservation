package com.rest.web_app_gestion_reservation.service.socket;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Very simple TCP server that could be used to push real-time notifications
 * (nouvelle réservation, annulation, etc.) to connected clients.
 *
 * For the mini-project, you mainly need this as an architectural example.
 */
public class NotificationServer {

    private final Set<PrintWriter> clients =
            Collections.synchronizedSet(new HashSet<>());

    public void start(int port) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Notification TCP server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                clients.add(out);

                // In a real implementation, you would also read from the client
                // and remove the writer when the client disconnects.
            }
        }
    }

    public void broadcast(String message) {
        synchronized (clients) {
            for (PrintWriter out : clients) {
                out.println(message);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        NotificationServer server = new NotificationServer();
        server.start(5000);
    }
}

