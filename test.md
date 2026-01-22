# Guide de Lancement Final (Backend 100% Autonome)

Ce document détaille la procédure de lancement finale. Apache Tomcat n'est plus du tout nécessaire.

---

## Architecture Finale

-   **Serveur Backend Unifié** : Une seule application Java qui lance et gère TOUS les services :
    -   Services REST (sur `http://localhost:8080`)
    -   Service SOAP (sur `http://localhost:8081`)
    -   Service RMI (sur le port `1099`)
    -   Service de Notification (sur le port `5001`)
-   **Application Client (JavaFX Fat Jar)** : Le client lourd qui communique avec le serveur backend.

---

## Étape 1 : Build du Projet

Exécutez cette commande une dernière fois pour inclure toutes les dépendances et corrections dans le JAR final.

```bash
mvn clean package
```

---

## Étape 2 : Démarrage du Backend

1.  Dans votre IDE, trouvez le fichier **`BackendServer.java`**.
2.  Exécutez sa méthode `main` (clic droit > "Run").

C'est la **seule** chose à lancer pour le backend. Attendez de voir le message `--- All Backend Services are running ---`.

---

## Étape 3 : Lancement du Client JavaFX

1.  Ouvrez un terminal.
2.  Naviguez jusqu'au répertoire `target/` de votre projet.
3.  Exécutez la commande pour lancer l'application :

```bash
java -jar web_app_gestion_reservation-1.0-SNAPSHOT.jar
```

L'application devrait maintenant démarrer et être entièrement fonctionnelle. Le tableau de bord de l'administrateur devrait se charger et afficher les données.

---

## Étape 4 : Procédures de Test Rapide

-   **Admin** : Connectez-vous avec `admin`/`admin`. Le tableau de bord doit s'afficher avec la liste des salles et des réservations.
-   **Utilisateur** : Créez un compte, connectez-vous, créez et annulez des réservations.
-   **Notifications** : Vérifiez avec `nc localhost 5001` que les actions de réservation (via le client ou SoapUI) génèrent des messages.
-   **REST** : Ouvrez `http://localhost:8080/api/rooms` dans votre navigateur. Vous devriez voir les salles au format JSON.
-   **SOAP** : Utilisez SoapUI pour envoyer des requêtes au service SOAP sur `http://localhost:8081/ws/reservations`.
-  **RMI** : Utilisez un client RMI pour tester les méthodes exposées

### URLs de l'API REST

Voici la liste des URL pour les opérations de l'API REST :

**Réservations**

*   `GET /api/reservations` : Récupère la liste de toutes les réservations.
*   `GET /api/reservations/user/{userId}` : Récupère la liste des réservations pour un utilisateur spécifique.
*   `POST /api/reservations?userId={userId}&roomId={roomId}&start={startIso}&end={endIso}` : Crée une nouvelle réservation.
*   `PUT /api/reservations/{reservationId}?roomId={roomId}&start={startIso}&end={endIso}` : Met à jour une réservation existante.
*   `DELETE /api/reservations/{reservationId}` : Annule une réservation.

**Salles**

*   `GET /api/rooms` : Récupère la liste de toutes les salles.
*   `POST /api/rooms` : Crée une nouvelle salle.
*   `DELETE /api/rooms/{roomId}` : Supprime une salle.