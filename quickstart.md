## Quickstart

This document explains how to run the main components of the meeting room reservation system.

### 1. Prerequisites

- JDK 17+ (or the version required by your JavaFX/Jakarta stack).
- MySQL server running locally.
- A JPA/Jakarta-compatible driver for MySQL (`com.mysql.cj.jdbc.Driver`) on the classpath.

### 2. Database Setup

1. Create a MySQL user and database (example):

```sql
CREATE DATABASE meeting_rooms_db CHARACTER SET utf8mb4;
CREATE USER 'root'@'localhost' IDENTIFIED BY 'root';
GRANT ALL PRIVILEGES ON meeting_rooms_db.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

2. In `src/META-INF/persistence.xml`, adjust:

- `jakarta.persistence.jdbc.url`
- `jakarta.persistence.jdbc.user`
- `jakarta.persistence.jdbc.password`

to match your environment.

On first run, Hibernate will create/update the schema automatically.

### 3. Running the JavaFX Client

1. Ensure JavaFX is configured in your IDE / run configuration.
2. Run the main class:
   - `com.rest.web_app_gestion_reservation.main.MainApp`

3. On first launch:
   - An **admin user** (`admin` / `admin`) is created automatically.

4. Account Creation:
   - Users can now create their own accounts via the "Créer un compte" link on the login screen.
   - All users must provide an **email**, a name, and a username.

### 4. Running Distributed Services

#### 4.1 REST API (JAX-RS)
Deploy to a Jakarta EE server (context: `web_app_gestion_reservation`).
- `GET http://localhost:8080/web_app_gestion_reservation/api/reservations/user/{id}`
- `DELETE http://localhost:8080/web_app_gestion_reservation/api/reservations/{id}`

#### 4.2 SOAP Service
- Run: `com.rest.web_app_gestion_reservation.service.soap.SoapPublisher`
- URL: `http://localhost:8081/soap/reservation`

#### 4.3 RMI Server
- Run: `com.rest.web_app_gestion_reservation.service.rmi.RmiServer`

#### 4.4 TCP Notifications
- Server: `com.rest.web_app_gestion_reservation.service.socket.NotificationServer`
- Client: `com.rest.web_app_gestion_reservation.service.socket.NotificationClient`

