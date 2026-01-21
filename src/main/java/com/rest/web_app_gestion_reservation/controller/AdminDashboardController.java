package com.rest.web_app_gestion_reservation.controller;

import com.rest.web_app_gestion_reservation.model.Room;
import com.rest.web_app_gestion_reservation.model.RoomType;
import com.rest.web_app_gestion_reservation.model.User;
import com.rest.web_app_gestion_reservation.model.dto.ReservationDTO;
import com.rest.web_app_gestion_reservation.service.client.NotificationClient;
import com.rest.web_app_gestion_reservation.service.client.RestClient;
import javafx.application.Platform;
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

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminDashboardController {

    @FXML private Label currentUserLabel;
    @FXML private TableView<Room> roomTable;
    @FXML private TableColumn<Room, String> colRoomName;
    @FXML private TableColumn<Room, String> colRoomType;
    @FXML private TableColumn<Room, Integer> colRoomCapacity;
    @FXML private TableColumn<Room, String> colRoomLocation;
    @FXML private TextField roomNameField;
    @FXML private ComboBox<RoomType> roomTypeComboBox;
    @FXML private TextField roomCapacityField;
    @FXML private TextField roomLocationField;
    @FXML private TextField roomDescriptionField;
    @FXML private TableView<ReservationDTO> reservationTable;
    @FXML private TableColumn<ReservationDTO, String> colResUser;
    @FXML private TableColumn<ReservationDTO, String> colResRoom;
    @FXML private TableColumn<ReservationDTO, String> colResStart;
    @FXML private TableColumn<ReservationDTO, String> colResEnd;

    private User currentUser;
    private RestClient restClient;
    private NotificationClient notificationClient;
    private final ObservableList<Room> rooms = FXCollections.observableArrayList();
    private final ObservableList<ReservationDTO> reservations = FXCollections.observableArrayList();

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private void initialize() {
        this.restClient = new RestClient();

        // Room table setup
        colRoomName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colRoomType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType().name()));
        colRoomCapacity.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCapacity()).asObject());
        colRoomLocation.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocation()));
        roomTable.setItems(rooms);

        // Reservation table setup
        colResUser.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserName()));
        colResRoom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoomName()));
        colResStart.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStartDateTime().format(dateTimeFormatter)));
        colResEnd.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEndDateTime().format(dateTimeFormatter)));
        reservationTable.setItems(reservations);

        roomTypeComboBox.setItems(FXCollections.observableArrayList(RoomType.values()));

        roomTable.getSelectionModel().selectedItemProperty().addListener((obs, oldRoom, newRoom) -> {
            if (newRoom != null) {
                roomNameField.setText(newRoom.getName());
                roomTypeComboBox.setValue(newRoom.getType());
                roomCapacityField.setText(String.valueOf(newRoom.getCapacity()));
                roomLocationField.setText(newRoom.getLocation());
                roomDescriptionField.setText(newRoom.getDescription());
            } else {
                clearForm();
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
        // Start notification client
        startNotificationClient();
    }

    private void startNotificationClient() {
        this.notificationClient = new NotificationClient(message -> {
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.INFORMATION, "Notification", message);
                handleRefreshAll(); // Refresh data on notification
            });
        });
        new Thread(notificationClient).start();
    }

    private void stopNotificationClient() {
        if (notificationClient != null) {
            notificationClient.stop();
        }
    }

    private void loadRooms() {
        List<Room> result = restClient.getRooms();
        rooms.setAll(result);
    }

    private void loadReservations() {
        List<ReservationDTO> result = restClient.getAllReservations();
        reservations.setAll(result);
    }

    @FXML
    private void handleRefreshAll() {
        loadRooms();
        loadReservations();
    }

    @FXML
    private void handleDeleteRoom() {
        Room room = roomTable.getSelectionModel().getSelectedItem();
        if (room != null) {
            if (restClient.deleteRoom(room.getId())) {
                loadRooms();
                loadReservations();
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer la salle.");
            }
        }
    }

    @FXML
    private void handleLogout() {
        stopNotificationClient(); // Stop listening for notifications
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rest/web_app_gestion_reservation/ui/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) currentUserLabel.getScene().getWindow();
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/com/rest/web_app_gestion_reservation/ui/style/style.css").toExternalForm());
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

        if (name.isBlank() || capacityText.isBlank() || type == null || location.isBlank()) {
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

            if (restClient.saveRoom(room) != null) {
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
