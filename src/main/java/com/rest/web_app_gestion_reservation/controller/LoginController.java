package com.rest.web_app_gestion_reservation.controller;

import com.rest.web_app_gestion_reservation.service.client.SoapServiceClient;
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

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private SoapServiceClient soapServiceClient;

    @FXML
    public void initialize() {
        this.soapServiceClient = new SoapServiceClient();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        User user = soapServiceClient.authenticate(username, password);
        if (user != null) {
            openDashboard(user);
        } else {
            errorLabel.setText("Identifiants incorrects ou service indisponible.");
        }
    }

    private void openDashboard(User user) {
        try {
            String fxml = user.isAdmin() ? "/com/rest/web_app_gestion_reservation/ui/admin_dashboard.fxml"
                    : "/com/rest/web_app_gestion_reservation/ui/user_dashboard.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            if (user.isAdmin()) {
                AdminDashboardController controller = loader.getController();
                controller.setCurrentUser(user);
            } else {
                UserDashboardController controller = loader.getController();
                controller.setCurrentUser(user);
            }

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, 1100, 700);
            scene.getStylesheets().add(getClass()
                    .getResource("/com/rest/web_app_gestion_reservation/ui/style/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors du chargement du tableau de bord.");
        }
    }

    @FXML
    private void handleOpenCreateAccount(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/rest/web_app_gestion_reservation/ui/create_account.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass()
                    .getResource("/com/rest/web_app_gestion_reservation/ui/style/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors du chargement de la page de création de compte.");
        }
    }
}
