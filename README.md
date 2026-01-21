# Système de Réservation de Salles de Réunion

Ce projet est une application distribuée pour la gestion et la réservation de salles de réunion, développée en Java.

---

## 1. Objectif du Projet

L'objectif est de fournir une solution centralisée permettant aux utilisateurs de réserver, consulter et gérer des réservations de salles en temps réel. Le système inclut un tableau de bord pour les utilisateurs et un autre pour les administrateurs.

---

## 2. Architecture Finale

Le projet est divisé en deux composants principaux :

1.  **Serveur Backend Unifié (`BackendServer.java`)**
    -   Une seule application Java autonome qui lance et gère tous les services nécessaires.
    -   **Services REST (via Grizzly @ `http://localhost:8080`)** : Utilisé pour la gestion des ressources. Principalement pour le tableau de bord admin (gestion des salles, consultation de toutes les réservations) et pour que les clients puissent lister les salles.
    -   **Service SOAP (via JAX-WS @ `http://localhost:8081`)** : Dédié à la gestion du cycle de vie des utilisateurs (authentification et inscription).
    -   **Service RMI (via Java RMI @ port `1099`)** : Cœur de la logique métier pour le client simple. Permet de créer, lister et annuler ses propres réservations.
    -   **Service de Notification (via Sockets @ port `8082`)** : Pousse des notifications textuelles en temps réel à tous les clients connectés lors d'événements (création, annulation de réservation).

2.  **Application Client (JavaFX "Fat Jar")**
    -   Une application de bureau packagée en un seul fichier JAR exécutable (`.jar`) qui contient toutes les dépendances nécessaires.
    -   Communique avec les différents services du backend pour fournir une interface utilisateur interactive.

---

## 3. Évolution de l'Architecture : Du Plan à la Réalité

Le projet a évolué par rapport à son plan initial pour répondre de manière plus pragmatique aux défis techniques rencontrés.

-   **JAX-WS (SOAP)** : Initialement prévu pour gérer les réservations et les utilisateurs, son rôle a été **recentré sur la gestion des utilisateurs uniquement** (authentification, inscription). La transmission d'objets de base de données complexes (entités JPA) via SOAP s'est avérée peu pratique et a été déplacée vers RMI.

-   **RMI** : Prévu pour des opérations de réservation simples, RMI est devenu le **cœur de la logique métier pour le client utilisateur**. Sa capacité à gérer nativement les appels de méthode Java à distance en a fait la solution la plus fiable et directe pour la création, la consultation et l'annulation des réservations par l'utilisateur.

-   **JAX-RS (REST)** : Le rôle de REST a été clarifié. Il est maintenant utilisé de manière classique pour la **gestion des ressources** : l'administrateur gère les salles et consulte la liste globale des réservations via REST, et les clients l'utilisent pour obtenir la liste des salles disponibles.

-   **Sockets TCP** : Cette partie est restée fidèle au plan initial et remplit parfaitement son rôle en envoyant des **notifications en temps réel** à tous les clients connectés.

Cette architecture finale est plus spécialisée, chaque technologie étant utilisée là où elle excelle le plus.

---

## 4. Technologies Utilisées

-   **Interface Utilisateur** : JavaFX
-   **Services Web** : JAX-RS (REST) avec Grizzly, JAX-WS (SOAP)
-   **Communication Distribuée** : RMI (Remote Method Invocation)
-   **Notifications Temps Réel** : Sockets TCP
-   **Base de Données** : MySQL avec Hibernate (JPA)
-   **Build** : Apache Maven

---

## 5. Prérequis

-   **JDK 17** ou supérieur.
-   **Maven** installé et configuré.
-   **MySQL** : Un serveur de base de données MySQL doit être accessible. La configuration se trouve dans `src/main/resources/META-INF/persistence.xml` (par défaut : `jdbc:mysql://localhost:3306/meeting_rooms_db`, utilisateur `appuser`, mot de passe `root`).

---

## 6. Comment Lancer le Projet

La procédure est simple et se fait en 3 étapes.

### Étape A : Build du Projet

Ouvrez un terminal à la racine du projet et exécutez la commande suivante. Cela créera un fichier JAR exécutable dans le dossier `target/`.

```bash
mvn clean package
```

### Étape B : Démarrage du Backend

Dans votre IDE (IntelliJ, Eclipse, etc.), trouvez le fichier `src/main/java/com/rest/web_app_gestion_reservation/BackendServer.java` et exécutez sa méthode `main`.

C'est la **seule** chose à lancer pour le backend. Attendez de voir le message `--- All Backend Services are running ---` dans la console.

### Étape C : Lancement du Client JavaFX

1.  Ouvrez un nouveau terminal.
2.  Naviguez jusqu'au répertoire `target/` de votre projet.
3.  Exécutez la commande suivante (le nom du fichier peut varier légèrement) :

```bash
java -jar web_app_gestion_reservation-1.0-SNAPSHOT.jar
```

L'application cliente devrait maintenant démarrer et être prête à l'emploi.

---

## 7. Identifiants par Défaut

Au premier lancement, un utilisateur administrateur est créé si la base de données est vide :

-   **Utilisateur** : `admin`
-   **Mot de passe** : `admin`
