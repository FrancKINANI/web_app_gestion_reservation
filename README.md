## Meeting Room Reservation System (Enhanced)

Java-based distributed system for managing meeting room reservations, built with **JavaFX**, **JPA/Hibernate + MySQL**, and multiple distributed technologies (**REST, SOAP, RMI, TCP sockets**).

### Main Features

- **Refactored Architecture**: Centralized business logic in `ReservationService` for better maintainability and consistency across all interfaces.
- **User & Admin Roles**
  - **Login & Registration**: Support for full name, username, password, and **email**.
  - **Admin Dashboard**: Manage rooms (CRUD) with location and description details, view/manage all reservations.
  - **User Dashboard**: Create/cancel reservations with conflict detection and a modernized UI.
- **Entity Enhancements**
  - `User`: Added `email` field.
  - `Room`: Added `location` and `description` fields.
- **Distributed Interfaces**
  - JAX-RS REST API.
  - JAX-WS SOAP service.
  - RMI service.
  - TCP notification server.

### Getting Started

For detailed steps see `quickstart.md`. In short:

1. Configure **MySQL** and `persistence.xml`.
2. Run the **JavaFX app** via `ui.MainApp` (login: `admin` / `admin`).

### Documentation

- **Project report**: `report.md` (Updated with architecture details).
- **Diagrams**: see `diagrams/` (PlantUML files).

