package com.rest.web_app_gestion_reservation;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.rest.web_app_gestion_reservation.model.Room;
import com.rest.web_app_gestion_reservation.model.RoomType;
import com.rest.web_app_gestion_reservation.model.User;
import com.rest.web_app_gestion_reservation.util.JpaUtil;


public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        initializeSampleData();

        Parent root = FXMLLoader.load(getClass().getResource("/com/rest/web_app_gestion_reservation/ui/login.fxml"));
        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(
                getClass().getResource("/com/rest/web_app_gestion_reservation/ui/style/style.css").toExternalForm());

        primaryStage.setTitle("Gestion des réservations de salles");
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/ui/meeting.png")));
        } catch (Exception ignored) {
            // Icon is optional
        }
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeSampleData() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // Create admin user if none exists
            TypedQuery<Long> userCountQuery = em.createQuery("SELECT COUNT(u) FROM User u", Long.class);
            Long userCount = userCountQuery.getSingleResult();
            if (userCount == null || userCount == 0) {
                User admin = new User("admin", "admin@meeting.com", "admin", "Administrateur", true);
                em.persist(admin);
            }

            // Create some rooms if none exist
            TypedQuery<Long> roomCountQuery = em.createQuery("SELECT COUNT(r) FROM Room r", Long.class);
            Long roomCount = roomCountQuery.getSingleResult();
            if (roomCount == null || roomCount == 0) {
                Room s1 = new Room("Salle A", RoomType.SMALL, 6, "1er étage, Aile Nord");
                Room m1 = new Room("Salle B", RoomType.MEDIUM, 10, "2ème étage, Aile Sud");
                Room g1 = new Room("Salle C", RoomType.LARGE, 20, "Rez-de-chaussée, Hall Central");
                em.persist(s1);
                em.persist(m1);
                em.persist(g1);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            em.close();
        }
    }

    @Override
    public void stop() {
        JpaUtil.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
