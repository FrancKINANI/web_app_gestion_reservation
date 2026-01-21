package com.rest.web_app_gestion_reservation.controller;

import com.rest.web_app_gestion_reservation.model.Reservation;
import com.rest.web_app_gestion_reservation.model.Room;
import com.rest.web_app_gestion_reservation.model.User;
import com.rest.web_app_gestion_reservation.service.client.NotificationClient;
import com.rest.web_app_gestion_reservation.service.client.RestClient;
import com.rest.web_app_gestion_reservation.service.client.RmiClient;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserDashboardController {

    @FXML private Label currentUserLabel;
    @FXML private ComboBox<Room> roomComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<LocalTime> startTimeComboBox;
    @FXML private ComboBox<LocalTime> endTimeComboBox;
    @FXML private Label reservationErrorLabel;
    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, String> colRoom;
    @FXML private TableColumn<Reservation, String> colDate;
    @FXML private TableColumn<Reservation, String> colStart;
    @FXML private TableColumn<Reservation, String> colEnd;

    private User currentUser;
    private RestClient restClient;
    private RmiClient rmiClient;
    private NotificationClient notificationClient;

    private final ObservableList<Reservation> reservations = FXCollections.observableArrayList();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private void initialize() {
        this.restClient = new RestClient();
        this.rmiClient = new RmiClient();

        setupTableColumns();
        setupComboBoxes();
        loadRooms();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (currentUserLabel != null && user != null) {
            currentUserLabel.setText(user.getFullName());
        }
        loadReservations();
        startNotificationClient();
    }

    private void startNotificationClient() {
        this.notificationClient = new NotificationClient(message -> {
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.INFORMATION, "Notification", message);
                handleRefresh();
            });
        });
        new Thread(notificationClient).start();
    }

    private void stopNotificationClient() {
        if (notificationClient != null) {
            notificationClient.stop();
        }
    }

    private void setupTableColumns() {
        colRoom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoom() != null ? data.getValue().getRoom().getName() : ""));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStartDateTime() != null ? data.getValue().getStartDateTime().toLocalDate().format(dateFormatter) : ""));
        colStart.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStartDateTime() != null ? data.getValue().getStartDateTime().toLocalTime().format(timeFormatter) : ""));
        colEnd.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEndDateTime() != null ? data.getValue().getEndDateTime().toLocalTime().format(timeFormatter) : ""));
        reservationTable.setItems(reservations);
    }

    private void setupComboBoxes() {
        roomComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Room item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName() + " (" + item.getLocation() + ")");
            }
        });
        roomComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Room item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        ObservableList<LocalTime> times = FXCollections.observableArrayList();
        for (int hour = 7; hour <= 22; hour++) {
            times.add(LocalTime.of(hour, 0));
            if (hour != 22) times.add(LocalTime.of(hour, 30));
        }
        startTimeComboBox.setItems(times);
        endTimeComboBox.setItems(times);
    }

    private void loadRooms() {
        List<Room> rooms = restClient.getRooms();
        if (rooms != null) {
            roomComboBox.setItems(FXCollections.observableArrayList(rooms));
        }
    }

    private void loadReservations() {
        if (currentUser == null) return;
        List<Reservation> result = rmiClient.listReservationsForUser(currentUser.getId());
        reservations.setAll(result);
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

        LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(date, endTime);

        Reservation res = rmiClient.createReservation(currentUser.getId(), room.getId(), startDateTime, endDateTime);
        if (res != null) {
            loadReservations();
            clearForm();
        } else {
            reservationErrorLabel.setText("Conflit : La salle est déjà occupée.");
        }
    }

    @FXML
    private void handleCancelReservation() {
        Reservation reservation = reservationTable.getSelectionModel().getSelectedItem();
        if (reservation != null) {
            if (rmiClient.cancelReservation(reservation.getId())) {
                loadReservations();
            } else {
                reservationErrorLabel.setText("Erreur lors de l'annulation.");
            }
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
        loadRooms();
        loadReservations();
    }

    @FXML
    private void handleLogout() {
        stopNotificationClient();
        try {
            Stage stage = (Stage) currentUserLabel.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/rest/web_app_gestion_reservation/ui/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/com/rest/web_app_gestion_reservation/ui/style/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
