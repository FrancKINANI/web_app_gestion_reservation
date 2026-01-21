## 1. Introduction

### 1.1 Contexte

Dans les entreprises modernes, les salles de réunion sont des ressources partagées et limitées. Leur gestion manuelle (tableurs, échanges de mails, réservations orales) entraîne rapidement des problèmes : conflits de réservation, salles vides alors qu’elles sont marquées comme occupées, manque de visibilité sur les disponibilités, et difficulté à analyser l’usage réel des ressources.  
Pour répondre à ces enjeux, il est nécessaire de disposer d’un système centralisé, fiable et accessible, permettant de réserver une salle, de consulter les créneaux disponibles et de suivre l’historique des réservations.

Le mini-projet présenté dans ce document consiste à concevoir et implémenter un **système distribué de gestion de réservation de salles de réunion**. Ce système s’appuie sur une base de données centralisée (MySQL) et propose plusieurs modes d’accès :

- Une **application de bureau JavaFX** pour les utilisateurs finaux et les administrateurs.
- Des **services distribués** (REST, SOAP, RMI, Sockets TCP) permettant l’intégration avec d’autres clients ou systèmes.

### 1.2 Objectifs

L’objectif principal est de fournir une solution permettant :

- **La gestion des salles** : création, modification, suppression, typage (petite, moyenne, grande) et capacité.
- **Gestion des réservations** : création, consultation, annulation.
- **La gestion des utilisateurs** : distinction entre utilisateurs simples et administrateurs, avec inscription et authentification.
- **La consultation des disponibilités** en temps réel.
- **L’accès distribué** aux fonctionnalités via plusieurs technologies Java :
  - JAX-RS (API REST),
  - JAX-WS (services SOAP),
  - RMI (Remote Method Invocation),
  - Sockets TCP pour les notifications en temps réel.

### 1.3 Technologies utilisées

- **JavaFX + CSS** : développement de l’interface graphique.
- **JPA (Jakarta Persistence) + Hibernate + MySQL** : couche de persistance, mapping objet–relationnel.
- **JAX-RS (REST)** : exposition d’API REST pour la gestion des ressources.
- **JAX-WS (SOAP)** : services Web SOAP pour la gestion des utilisateurs.
- **Java RMI** : communication distante pour la logique métier des utilisateurs.
- **Sockets TCP** : système de notification asynchrone.

---

## 2. Architecture du système

### 2.1 Vue d’ensemble (Architecture Finale)

Le système a été refactorisé pour suivre le pattern **Service Layer**. Toute la logique métier est centralisée dans la classe `ReservationService`.

- **Couche Présentation (JavaFX)** : Contrôleurs (`LoginController`, `AdminDashboardController`, `UserDashboardController`) qui délèguent toutes les opérations à la couche service.

- **Couche Service (Cœur du système)** : `ReservationService` agit comme une façade unique pour toutes les interfaces (UI, REST, SOAP, RMI). Elle gère l’authentification, l’enregistrement des utilisateurs, le CRUD des salles, et la logique complexe de réservation (détection de conflits).

- **Couche Persistance** : Entités JPA (`User`, `Room`, `Reservation`) et `JpaUtil` pour l'accès à la base de données MySQL.

### 2.2 Évolution de l'Architecture : Du Plan à la Réalité

Le projet a évolué par rapport à son plan initial pour répondre de manière plus pragmatique aux défis techniques rencontrés.

-   **JAX-WS (SOAP)** : Initialement prévu pour gérer les réservations et les utilisateurs, son rôle a été **recentré sur la gestion des utilisateurs uniquement** (authentification, inscription). La transmission d’objets de base de données complexes (entités JPA) via SOAP s’est avérée peu pratique et a été déplacée vers RMI.

-   **RMI** : Prévu pour des opérations de réservation simples, RMI est devenu le **cœur de la logique métier pour le client utilisateur**. Sa capacité à gérer nativement les appels de méthode Java à distance en a fait la solution la plus fiable et directe pour la création, la consultation et l'annulation des réservations par l'utilisateur.

-   **JAX-RS (REST)** : Le rôle de REST a été clarifié. Il est maintenant utilisé de manière classique pour la **gestion des ressources** : l'administrateur gère les salles et consulte la liste globale des réservations via REST, et les clients l'utilisent pour obtenir la liste des salles disponibles.

-   **Sockets TCP** : Cette partie est restée fidèle au plan initial et remplit parfaitement son rôle en envoyant des **notifications en temps réel** à tous les clients connectés.

Cette architecture finale est plus spécialisée, chaque technologie étant utilisée là où elle excelle le plus.

### 2.3 Architecture des services distribués (Implémentation finale)

Chaque technologie de communication expose la logique métier de `ReservationService` de manière spécialisée :

- **REST (JAX-RS)** : `RoomResource` expose la gestion des salles (CRUD) et `ReservationResource` expose les opérations sur les réservations. Principalement utilisé par l'administrateur et pour les consultations générales.

- **SOAP (JAX-WS)** : `ReservationSoapService` expose les opérations liées aux utilisateurs (`authenticate`, `registerUser`). Le `SoapPublisher` publie le service et son WSDL.

- **RMI** : `ReservationRmiService` définit les méthodes distantes pour le client utilisateur (`listReservationsForUser`, `createReservation`, `cancelReservation`). Le `RmiServer` enregistre le service dans le registre RMI.

- **Sockets TCP** : `NotificationServer` (port `8082`) accepte des connexions TCP, maintient une liste de clients, et leur diffuse des messages en temps réel via la méthode `broadcast()`.

---

## 3. Modèle de données et persistance

Le cœur du système repose sur un modèle de données simple mais efficace, matérialisé par des entités JPA.

-   **User** : représente un utilisateur. Il contient un identifiant, un nom d'utilisateur, un mot de passe et un booléen `admin` pour gérer les droits.
-   **Room** : représente une salle de réunion. Elle est définie par un nom, une capacité, et un type (`RoomType`).
-   **Reservation** : C'est l'entité centrale qui lie un `User` à une `Room` pour un créneau horaire donné (`startDateTime` et `endDateTime`).

La persistance est gérée par JPA avec Hibernate comme fournisseur, configuré dans `src/META-INF/persistence.xml` pour se connecter à une base de données MySQL.

## 4. Gestion des conflits de réservation

Avant de créer une nouvelle réservation, une requête est exécutée pour compter le nombre de réservations existantes pour la même salle qui entrent en conflit avec le créneau demandé. La condition de conflit est la suivante : une réservation existante entre en conflit si son heure de début est avant l'heure de fin de la nouvelle réservation ET son heure de fin est après l'heure de début de la nouvelle réservation. Si un conflit est détecté, la réservation n'est pas créée.

## 5. Description des interfaces graphiques

L'application JavaFX propose une interface utilisateur simple et fonctionnelle, définie avec FXML.

-   **Vue de connexion (`login.fxml`)** : Point d'entrée de l'application avec un formulaire de connexion.
-   **Tableau de bord utilisateur (`user_dashboard.fxml`)** : Permet aux utilisateurs de créer une réservation et de lister/annuler leurs propres réservations.
-   **Tableau de bord administrateur (`admin_dashboard.fxml`)** : Permet aux administrateurs de gérer les salles (CRUD) et de visualiser toutes les réservations du système.

## 6. Scénarios d’exécution

1.  **Un utilisateur réserve une salle** : Il se connecte, est dirigé vers son tableau de bord, remplit le formulaire de réservation. L'appel passe par **RMI** (`RmiClient` -> `ReservationRmiService`). En cas de succès, la liste des réservations se met à jour.

2.  **Un administrateur gère les salles** : Il se connecte, est dirigé vers le tableau de bord admin. Il peut ajouter/modifier/supprimer une salle. Les appels passent par **REST** (`RestClient` -> `RoomResource`).

3.  **Un utilisateur reçoit une notification** : Lorsqu'une réservation est créée, le `NotificationServer` envoie un message. Tous les clients connectés (y compris l'admin et les autres utilisateurs) reçoivent le message en temps réel, qui s'affiche sous forme d'alerte, et leurs listes de réservations sont rafraîchies.

## 7. Évaluation

**Points forts** :
-   **Architecture modulaire et spécialisée**.
-   **Multi-canal** : La logique métier est exposée via plusieurs technologies.
-   **Gestion centralisée des règles métier** dans `ReservationService`.

**Points faibles** :
-   **Sécurité limitée** : mots de passe en clair.
-   **Interface utilisateur simple**.
-   **Pas de tests automatisés**.

## 8. Pistes d’amélioration

-   **Sécurité** : Hacher les mots de passe (ex: avec BCrypt), mettre en place un système de tokens pour l'API REST.
-   **Fonctionnalités** : Ajouter des réservations récurrentes.
-   **Interface utilisateur** : Intégrer un composant de calendrier pour une vue plus visuelle.
-   **Tests** : Rédiger des tests unitaires (JUnit) et d'intégration.
