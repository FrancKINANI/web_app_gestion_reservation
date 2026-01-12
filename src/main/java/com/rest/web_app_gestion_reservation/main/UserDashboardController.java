package com.rest.web_app_gestion_reservation.main;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.rest.web_app_gestion_reservation.model.Reservation;
import com.rest.web_app_gestion_reservation.model.Room;
import com.rest.web_app_gestion_reservation.model.User;
import com.rest.web_app_gestion_reservation.service.ReservationService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class UserDashboardController {

    @FXML
    private Label currentUserLabel;

    @FXML
    private ComboBox<Room> roomComboBox;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<LocalTime> startTimeComboBox;

    @FXML
    private ComboBox<LocalTime> endTimeComboBox;

    @FXML
    private Label reservationErrorLabel;

    @FXML
    private TableView<Reservation> reservationTable;

    @FXML
    private TableColumn<Reservation, String> colRoom;

    @FXML
    private TableColumn<Reservation, String> colDate;

    @FXML
    private TableColumn<Reservation, String> colStart;

    @FXML
    private TableColumn<Reservation, String> colEnd;

    private User currentUser;
    private final ReservationService reservationService = new ReservationService();
    private final ObservableList<Reservation> reservations = FXCollections.observableArrayList();

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private void initialize() {
        colRoom.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getRoom() != null ? data.getValue().getRoom().getName() : ""));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getStartDateTime() != null
                        ? data.getValue().getStartDateTime().toLocalDate().format(dateFormatter)
                        : ""));
        colStart.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getStartDateTime() != null
                        ? data.getValue().getStartDateTime().toLocalTime().format(timeFormatter)
                        : ""));
        colEnd.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEndDateTime() != null
                        ? data.getValue().getEndDateTime().toLocalTime().format(timeFormatter)
                        : ""));

        reservationTable.setItems(reservations);

        // Customize room ComboBox to show name and location properly
        roomComboBox.setCellFactory(lv -> new ListCell<Room>() {
            @Override
            protected void updateItem(Room item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    String loc = item.getLocation() != null ? " (" + item.getLocation() + ")" : "";
                    setText(item.getName() + loc);
                }
            }
        });
        roomComboBox.setButtonCell(new ListCell<Room>() {
            @Override
            protected void updateItem(Room item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    String loc = item.getLocation() != null ? " (" + item.getLocation() + ")" : "";
                    setText(item.getName() + loc);
                }
            }
        });

        // Initialize time ComboBoxes
        ObservableList<LocalTime> times = FXCollections.observableArrayList();
        LocalTime time = LocalTime.of(7, 0);
        while (!time.isAfter(LocalTime.of(22, 0))) {
            times.add(time);
            time = time.plusMinutes(30);
        }
        startTimeComboBox.setItems(times);
        endTimeComboBox.setItems(times);

        loadRooms();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (currentUserLabel != null && user != null) {
            currentUserLabel.setText(user.getFullName());
        }
        loadReservations();
    }

    private void loadRooms() {
        List<Room> rooms = reservationService.listAllRooms();
        roomComboBox.setItems(FXCollections.observableArrayList(rooms));
    }

    private void loadReservations() {
        if (currentUser == null)
            return;
        List<Reservation> result = reservationService.listReservationsForUser(currentUser.getId());
        reservations.setAll(result);
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/rest/web_app_gestion_reservation/ui/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) currentUserLabel.getScene().getWindow();
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass()
                    .getResource("/com/rest/web_app_gestion_reservation/ui/style/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) {
            reservationErrorLabel.setText("Impossible de revenir à l'écran de connexion.");
        }
    }

    @FXML
    private void handleCreateReservation() {
        reservationErrorLabel.setText("");

        Room room = roomComboBox.getValue();
        LocalDate date = datePicker.getValue();
        LocalTime startTime = startTimeComboBox.getValue();
        LocalTime endTime = endTimeComboBox.getValue();

        if (room == null || date == null || startTime == null || endTime == null) {
            reservationErrorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        if (!endTime.isAfter(startTime)) {
            reservationErrorLabel.setText("L'heure de fin doit être après l'heure de début.");
            return;
        }

        LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(date, endTime);

        Reservation res = reservationService.createReservation(currentUser.getId(), room.getId(), startDateTime,
                endDateTime);
        if (res != null) {
            loadReservations();
            clearForm();
        } else {
            reservationErrorLabel.setText("Conflit de réservation ou erreur.");
        }
    }

    private void clearForm() {
        roomComboBox.setValue(null);
        datePicker.setValue(null);
        startTimeComboBox.setValue(null);
        endTimeComboBox.setValue(null);
    }

    @FXML
    private void handleRefresh() {
        loadReservations();
    }

    @FXML
    private void handleCancelReservation() {
        Reservation reservation = reservationTable.getSelectionModel().getSelectedItem();
        if (reservation != null) {
            if (reservationService.cancelReservation(reservation.getId())) {
                loadReservations();
            } else {
                reservationErrorLabel.setText("Erreur lors de l'annulation.");
            }
        }
    }
}
