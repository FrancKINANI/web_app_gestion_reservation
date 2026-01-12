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
- **La gestion des réservations** : création, consultation, modification/annulation, avec vérification automatique des conflits.
- **La gestion des utilisateurs** : distinction entre utilisateurs simples et administrateurs.
- **La consultation des disponibilités** en temps (quasi) réel.
- **L’accès distribué** aux fonctionnalités de réservation via plusieurs technologies Java :
  - JAX-RS (API REST),
  - JAX-WS (services SOAP),
  - RMI (Remote Method Invocation),
  - Sockets TCP pour les notifications en temps réel.

Au-delà des fonctionnalités métier, le projet a également un objectif pédagogique : mettre en pratique la conception en couches (présentation, service, persistance), l’architecture orientée services, la persistance avec JPA/Hibernate et les principaux mécanismes de communication distribuée en Java.

### 1.3 Technologies utilisées

- **JavaFX + CSS** : développement de l’interface graphique avec une ergonomie moderne (glassmorphism, dégradés sombres, ombres portées, composants stylisés).
- **JPA (Jakarta Persistence) + Hibernate + MySQL** : couche de persistance, mapping objet–relationnel des entités `User`, `Room`, `Reservation`.
- **JAX-RS (REST)** : exposition d’API REST pour la gestion des ressources.
- **JAX-WS (SOAP)** : services Web SOAP pour l'interopérabilité.
- **Java RMI** : communication distante entre JVM Java.
- **Sockets TCP** : système de notification asynchrone.

---

## 2. Architecture du système

### 2.1 Vue d’ensemble (Architecture refactorisée)

Le système a été refactorisé pour suivre le pattern **Service Layer**. Toute la logique métier, auparavant dispersée dans les contrôleurs UI et les couches d'accès, est désormais centralisée dans la classe `ReservationService`.

- **Couche Présentation (JavaFX)**
  - Utilise les contrôleurs `LoginController`, `AdminDashboardController`, `UserDashboardController`.
  - Ces contrôleurs n'interagissent plus directement avec l' `EntityManager`. Ils délèguent toutes les opérations à `ReservationService`.
  - Le style graphique a été modernisé via un fichier `style.css` enrichi (effets visuels avancés).

- **Couche Service (Cœur du système)**
  - `ReservationService` agit comme une façade unique pour toutes les interfaces (UI, REST, SOAP, RMI).
  - Elle gère l'authentification, l'enregistrement des utilisateurs, le CRUD des salles, et la logique complexe de réservation (détection de conflits).
  - Assure une cohérence totale des données quel que soit le point d'entrée.

- **Couche Persistance**
  - Entités enrichies :
    - `User` : ajout de l'attribut `email`.
    - `Room` : ajout de `location` (localisation physique) et `description`.
  - Utilisation de **Fetch Joins** dans les requêtes JPQL pour optimiser les performances et éviter les problèmes de chargement paresseux (`LazyInitializationException`) dans l'interface graphique.

### 2.2 Diagramme d’architecture logique (description)

Le diagramme d’architecture (voir fichier `diagrams/architecture.puml`) comporte les éléments suivants :

- **Client JavaFX**
  - Communique directement avec la couche service (dans la même JVM) via les contrôleurs.
- **Clients distants** :
  - **Client REST** (navigateur, application front-end, autre service) → API REST.
  - **Client SOAP** → Service SOAP.
  - **Client RMI** → Service RMI.
  - **Client TCP** → Serveur de notifications.
- **Couche service** :
  - `ReservationService`, éventuellement d’autres services (gestion des utilisateurs, des salles, etc.).
- **Couche persistance** :
  - Entités JPA et `JpaUtil` connectés à une base **MySQL**.

Les flèches indiquent :

- Les appels de la couche présentation / clients distants vers les services.
- Les accès de la couche service à la couche persistance.

### 2.3 Architecture de données

Le modèle de données repose sur trois entités principales :

- **User**
  - Attributs : `id`, `username`, `password`, `fullName`, `admin` (booléen).
  - Rôle : représente un utilisateur du système (simple utilisateur ou administrateur).
- **Room**
  - Attributs : `id`, `name`, `type` (`SMALL`, `MEDIUM`, `LARGE`), `capacity`, indicateurs d’équipement (`whiteboard`, `projector`, `videoConference`, `microphones`, `presentationScreen`).
  - Rôle : représente une salle de réunion avec ses caractéristiques physiques.
- **Reservation**
  - Attributs : `id`, `user`, `room`, `startDateTime`, `endDateTime`.
  - Associations :
    - `user` : many-to-one vers `User`.
    - `room` : many-to-one vers `Room`.

La base de données MySQL comporte donc trois tables principales (`users`, `rooms`, `reservations`) reliées par des clés étrangères. Ce schéma est suffisant pour :

- Enregistrer qui a réservé quelle salle, à quelle date et pour quel créneau horaire.
- Retrouver facilement l’historique par utilisateur ou par salle.
- Vérifier les conflits de réservation sur un intervalle de temps donné.

### 2.4 Architecture des services distribués

Chaque technologie de communication repose sur la même logique métier :

- **REST (JAX-RS)**
  - `RoomResource` expose les salles et un point d’entrée pour vérifier la disponibilité.
  - `ReservationResource` expose les opérations CRUD sur les réservations (`GET`, `POST`, `DELETE`).
  - Avantages : simplicité, usage d’HTTP, format JSON, facilement consommable par des applications web ou mobiles.

- **SOAP (JAX-WS)**
  - `ReservationSoapService` expose des opérations fortement typées (`createReservation`, `cancelReservation`).
  - `SoapPublisher` permet de publier le service sur une URL donnée et de générer un WSDL.
  - Avantages : contrat formel (WSDL), interopérabilité avec des environnements hétérogènes.

- **RMI**
  - `ReservationRmiService` définit des méthodes distantes proches des méthodes Java locales.
  - `ReservationRmiServiceImpl` délègue à `ReservationService`.
  - `RmiServer` enregistre le service dans un registre RMI.
  - Avantages : transparence des appels pour des clients Java, passage d’objets.

- **Sockets TCP**
  - `NotificationServer` accepte des connexions TCP et maintient une liste de clients.
  - `NotificationClient` se connecte et affiche toutes les notifications reçues.
  - Utilisé pour illustrer la **mise à jour en temps réel** des clients lorsque des réservations sont créées ou annulées.

En combinant ces différentes technologies, le système démontre comment une même base de logique métier peut être exposée de plusieurs manières, en fonction des besoins d’intégration et du type de clients à supporter.

---

## 3. Modèle de données et persistance

Le cœur du système repose sur un modèle de données simple mais efficace, matérialisé par des entités JPA.

-   **User** : représente un utilisateur. Il contient un identifiant, un nom d'utilisateur, un mot de passe et un booléen `admin` pour gérer les droits.
-   **Room** : représente une salle de réunion. Elle est définie par un nom, une capacité, un type (`RoomType`: `SMALL`, `MEDIUM`, `LARGE`), et des équipements (projecteur, etc.).
-   **Reservation** : C'est l'entité centrale qui lie un `User` à une `Room` pour un créneau horaire donné (`startDateTime` et `endDateTime`).
-   **RoomType** : Une énumération simple pour catégoriser les salles.

La persistance est gérée par JPA avec Hibernate comme fournisseur. La configuration se trouve dans `src/META-INF/persistence.xml` et utilise une base de données MySQL. La propriété `hibernate.hbm2ddl.auto` est sur `update`, ce qui permet à Hibernate de mettre à jour le schéma de la base de données au démarrage de l'application.

## 4. Gestion des conflits de réservation

Un point crucial dans un système de réservation est d'empêcher les doubles réservations pour une même salle sur des créneaux qui se chevauchent. Cette logique est implémentée dans la méthode `createReservation` de la classe `ReservationService`.

Avant de créer une nouvelle réservation, une requête est exécutée pour compter le nombre de réservations existantes pour la même salle (`room`) qui entrent en conflit avec le créneau demandé (`startDateTime`, `endDateTime`).

La condition de conflit est la suivante : une réservation existante entre en conflit si son heure de début est avant l'heure de fin de la nouvelle réservation ET son heure de fin est après l'heure de début de la nouvelle réservation.
En SQL (simplifié), cela se traduit par :
`WHERE room_id = ? AND start_date_time < ? AND end_date_time > ?`

Si le compte de conflits est supérieur à zéro, la transaction est annulée (`rollback`) et la réservation n'est pas créée. Cette vérification est effectuée à l'intérieur d'une transaction pour garantir l'atomicité de l'opération.

## 5. Description des interfaces graphiques

L'application JavaFX propose une interface utilisateur simple et fonctionnelle, définie avec FXML.

-   **Vue de connexion (`login.fxml`)** : C'est le point d'entrée de l'application. Elle présente un formulaire simple avec des champs pour le nom d'utilisateur et le mot de passe, ainsi qu'un bouton de connexion. Un label est prévu pour afficher les messages d'erreur (ex: identifiants incorrects).

-   **Tableau de bord utilisateur (`user_dashboard.fxml`)** : Cette vue est destinée aux utilisateurs non-administrateurs. Elle est divisée en deux parties :
    -   Un formulaire pour créer une nouvelle réservation (salle, date, heure de début/fin).
    -   Un tableau qui liste les réservations de l'utilisateur connecté. L'utilisateur peut sélectionner une réservation dans le tableau et l'annuler.

-   **Tableau de bord administrateur (`admin_dashboard.fxml`)** : Cette vue offre des fonctionnalités étendues pour les administrateurs. Elle est également divisée en deux :
    -   Un panneau de gestion des salles, avec un tableau listant toutes les salles et des boutons pour en ajouter, modifier ou supprimer.
    -   Un panneau affichant *toutes* les réservations de *tous* les utilisateurs, avec un bouton pour rafraîchir la liste.

## 6. Scénarios d’exécution

Voici quelques scénarios d'utilisation typiques du système :

1.  **Un utilisateur réserve une salle via l'application JavaFX** :
    1.  L'utilisateur lance l'application et se connecte avec ses identifiants.
    2.  Le système le redirige vers le tableau de bord utilisateur.
    3.  Il remplit le formulaire de réservation en spécifiant une salle, une date et un créneau.
    4.  En cliquant sur "Réserver", l'application appelle `ReservationService.createReservation()`.
    5.  Si le créneau est libre, la réservation est enregistrée en base de données et la liste de ses réservations est mise à jour. Sinon, un message d'erreur s'affiche.

2.  **Un administrateur gère les salles** :
    1.  L'administrateur se connecte avec un compte `admin`.
    2.  Le système le redirige vers le tableau de bord administrateur.
    3.  Il peut voir la liste de toutes les salles, en ajouter une nouvelle en remplissant les champs prévus, ou en sélectionner une pour la modifier ou la supprimer.

3.  **Un client externe utilise l'API REST** :
    1.  Un client (par exemple, un script Python ou une application web) envoie une requête HTTP POST à l'endpoint `/api/reservations`.
    2.  La requête contient l'ID de l'utilisateur, l'ID de la salle et les dates de début et de fin.
    3.  `ReservationResource` reçoit la requête, appelle `ReservationService.createReservation()`.
    4.  Si la création réussit, le service REST renvoie un code `201 Created` avec les détails de la réservation en JSON. En cas de conflit, il renvoie un `409 Conflict`.

## 7. Évaluation

**Points forts** :
-   **Architecture modulaire** : La séparation claire entre les couches (présentation, service, persistance) rend le code plus facile à maintenir et à faire évoluer.
-   **Multi-canal** : La logique métier est exposée via plusieurs technologies (JavaFX, REST, SOAP, RMI), ce qui démontre une grande flexibilité et interopérabilité.
-   **Gestion centralisée des règles métier** : Toute la logique de réservation, y compris la gestion des conflits, est centralisée dans `ReservationService`, évitant la duplication de code.
-   **Persistance robuste** : L'utilisation de JPA/Hibernate simplifie l'accès à la base de données et assure la portabilité.

**Points faibles** :
-   **Sécurité limitée** : Les mots de passe sont stockés en clair. L'authentification est basique et il n'y a pas de gestion fine des autorisations pour les API.
-   **Interface utilisateur simple** : L'UI est fonctionnelle mais pourrait être améliorée en termes d'ergonomie (ex: un calendrier visuel pour les réservations).
-   **Pas de tests automatisés** : L'absence de tests unitaires ou d'intégration rend les futures modifications risquées.
-   **Gestion des erreurs** : La gestion des erreurs pourrait être plus robuste, notamment pour les clients distants (REST, SOAP).

## 8. Pistes d’amélioration

-   **Sécurité** :
    -   Hacher les mots de passe des utilisateurs en base de données (ex: avec BCrypt).
    -   Mettre en place un système d'authentification basé sur des tokens (ex: JWT) pour l'API REST.
-   **Fonctionnalités** :
    -   Ajouter la possibilité de faire des réservations récurrentes.
    -   Mettre en place un système de notifications par email ou via le client socket lors de la création/annulation d'une réservation.
    -   Permettre la modification d'une réservation existante (plutôt que de devoir l'annuler et la recréer).
-   **Interface utilisateur** :
    -   Intégrer un composant de calendrier (ex: [ControlsFX](https://controlsfx.org/)) pour visualiser et sélectionner les créneaux de réservation de manière plus intuitive.
    -   Améliorer la validation des formulaires côté client.
-   **Tests** :
    -   Rédiger des tests unitaires pour `ReservationService` avec JUnit et Mockito.
    -   Mettre en place des tests d'intégration pour les endpoints REST.
-   **Déploiement** :
    -   Utiliser un outil de build comme Maven ou Gradle pour gérer les dépendances et automatiser la construction du projet.
    -   Conteneuriser l'application et sa base de données avec Docker pour faciliter le déploiement.

