package com.rest.web_app_gestion_reservation.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.rest.web_app_gestion_reservation.model.User;
import com.rest.web_app_gestion_reservation.service.ReservationService;

import java.io.IOException;

public class CreateAccountController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    private final ReservationService reservationService = new ReservationService();

    @FXML
    private void handleCreateAccount(ActionEvent event) {
        String username = usernameField.getText();
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username == null || username.isBlank() || fullName == null || fullName.isBlank()
                || email == null || email.isBlank() || password == null || password.isBlank()
                || confirmPassword == null || confirmPassword.isBlank()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Les mots de passe ne correspondent pas.");
            return;
        }

        User user = reservationService.registerUser(username, email, password, fullName, false);
        if (user != null) {
            handleBackToLogin(event);
        } else {
            errorLabel
                    .setText("Erreur lors de la création du compte (l'utilisateur ou l'email existe peut-être déjà).");
        }
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/rest/web_app_gestion_reservation/ui/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass()
                    .getResource("/com/rest/web_app_gestion_reservation/ui/style/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) {
            errorLabel.setText("Erreur lors du chargement de la page de connexion.");
        }
    }
}
