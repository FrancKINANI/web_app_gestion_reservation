## Quickstart

Ce document explique comment lancer rapidement le système de réservation de salles.

### 1. Prérequis

- **JDK 17+**
- **Maven**
- **MySQL** : Le serveur doit être accessible. La configuration se trouve dans `src/main/resources/META-INF/persistence.xml`.

### 2. Lancement (2 Étapes)

La nouvelle architecture unifie le démarrage de tous les services backend.

#### Étape 1 : Lancer le Backend

Dans votre IDE, trouvez et exécutez la méthode `main` de la classe :
`com.rest.web_app_gestion_reservation.BackendServer`

Attendez que la console affiche `--- All Backend Services are running ---`. Cela signifie que les serveurs REST, SOAP, RMI et de notification sont tous démarrés et prêts.

#### Étape 2 : Lancer le Client JavaFX

Dans un terminal, à la racine du projet, exécutez :

```bash
# 1. Construire le "fat jar" qui contient tout ce qu'il faut
mvn clean package

# 2. Lancer le client
java -jar target/web_app_gestion_reservation-1.0-SNAPSHOT.jar
```

L'interface graphique va se lancer. Vous pouvez vous connecter avec le compte admin par défaut (`admin`/`admin`) ou créer un nouveau compte.

### 3. Endpoints des Services (Pour Information)

Le `BackendServer` expose les services suivants :

-   **REST (JAX-RS)** @ `http://localhost:8080/api`
    -   `GET /rooms` : Liste toutes les salles.
    -   `POST /rooms` : Crée une salle.
    -   `GET /reservations` : Liste toutes les réservations.
    -   `POST /reservations` : Crée une réservation.
    -   ...et d'autres.

-   **SOAP (JAX-WS)** @ `http://localhost:8081/soap/reservation?wsdl`
    -   Expose les méthodes `authenticate` et `registerUser`.

-   **RMI (Java RMI)** sur le port `1099`
    -   Expose le service `ReservationRmiService` pour la logique métier du client.

-   **Sockets (TCP)** sur le port `8082`
    -   Le serveur de notification auquel les clients se connectent pour recevoir les messages en temps réel.
