package com.rest.web_app_gestion_reservation.controller;

import javafx.beans.property.SimpleIntegerProperty;
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
import com.rest.web_app_gestion_reservation.model.RoomType;
import com.rest.web_app_gestion_reservation.model.User;
import com.rest.web_app_gestion_reservation.service.ReservationService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminDashboardController {

    @FXML
    private Label currentUserLabel;

    @FXML
    private TableView<Room> roomTable;

    @FXML
    private TableColumn<Room, String> colRoomName;

    @FXML
    private TableColumn<Room, String> colRoomType;

    @FXML
    private TableColumn<Room, Integer> colRoomCapacity;

    @FXML
    private TableColumn<Room, String> colRoomLocation;

    @FXML
    private TextField roomNameField;

    @FXML
    private ComboBox<RoomType> roomTypeComboBox;

    @FXML
    private TextField roomCapacityField;

    @FXML
    private TextField roomLocationField;

    @FXML
    private TextField roomDescriptionField;

    @FXML
    private TableView<Reservation> reservationTable;

    @FXML
    private TableColumn<Reservation, String> colResUser;

    @FXML
    private TableColumn<Reservation, String> colResRoom;

    @FXML
    private TableColumn<Reservation, String> colResStart;

    @FXML
    private TableColumn<Reservation, String> colResEnd;

    private User currentUser;
    private final ReservationService reservationService = new ReservationService();
    private final ObservableList<Room> rooms = FXCollections.observableArrayList();
    private final ObservableList<Reservation> reservations = FXCollections.observableArrayList();

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private void initialize() {
        colRoomName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colRoomType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType().name()));
        colRoomCapacity
                .setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCapacity()).asObject());
        colRoomLocation.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocation()));

        colResUser.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getUser() != null ? data.getValue().getUser().getUsername() : ""));
        colResRoom.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getRoom() != null ? data.getValue().getRoom().getName() : ""));
        colResStart.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getStartDateTime() != null
                        ? data.getValue().getStartDateTime().format(dateTimeFormatter)
                        : ""));
        colResEnd.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEndDateTime() != null ? data.getValue().getEndDateTime().format(dateTimeFormatter)
                        : ""));

        roomTable.setItems(rooms);
        reservationTable.setItems(reservations);
        roomTypeComboBox.setItems(FXCollections.observableArrayList(RoomType.values()));

        roomTable.getSelectionModel().selectedItemProperty().addListener((obs, oldRoom, newRoom) -> {
            if (newRoom != null) {
                roomNameField.setText(newRoom.getName());
                roomTypeComboBox.setValue(newRoom.getType());
                roomCapacityField.setText(Integer.toString(newRoom.getCapacity()));
                roomLocationField.setText(newRoom.getLocation());
                roomDescriptionField.setText(newRoom.getDescription());
            }
        });

        loadRooms();
        loadReservations();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (currentUserLabel != null && user != null) {
            currentUserLabel.setText(user.getFullName() + " (admin)");
        }
    }

    private void loadRooms() {
        List<Room> result = reservationService.listAllRooms();
        rooms.setAll(result);
    }

    private void loadReservations() {
        List<Reservation> result = reservationService.listAllReservations();
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de revenir à l'écran de connexion.");
        }
    }

    @FXML
    private void handleSaveRoom() {
        String name = roomNameField.getText();
        String capacityText = roomCapacityField.getText();
        RoomType type = roomTypeComboBox.getValue();
        String location = roomLocationField.getText();

        if (name == null || name.isBlank() || capacityText == null || capacityText.isBlank() || type == null
                || location == null || location.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        try {
            int capacity = Integer.parseInt(capacityText);
            Room selectedRoom = roomTable.getSelectionModel().getSelectedItem();
            Room room = (selectedRoom != null) ? selectedRoom : new Room();

            room.setName(name);
            room.setCapacity(capacity);
            room.setType(type);
            room.setLocation(location);
            room.setDescription(roomDescriptionField.getText());

            if (reservationService.saveRoom(room) != null) {
                loadRooms();
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement de la salle.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Capacité invalide", "La capacité doit être un nombre entier.");
        }
    }

    private void clearForm() {
        roomTable.getSelectionModel().clearSelection();
        roomNameField.clear();
        roomCapacityField.clear();
        roomTypeComboBox.setValue(null);
        roomLocationField.clear();
        roomDescriptionField.clear();
    }

    @FXML
    private void handleDeleteRoom() {
        Room room = roomTable.getSelectionModel().getSelectedItem();
        if (room != null) {
            if (reservationService.deleteRoom(room.getId())) {
                loadRooms();
                loadReservations();
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer la salle.");
            }
        }
    }

    @FXML
    private void handleRefreshReservations() {
        loadReservations();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
