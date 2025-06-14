# Nancy Spot 🗺️

Une application répartie, qui permet d'afficher, dans un navigateur, sur une carte Leaflet des informations hétérogènes sur Nancy, informations vous appartenant, ou pas, transitant parfois par un proxy, qui fera la passerelle entre votre navigateur et des services.
## 🚀 Fonctionnalités

### 🏪 Restaurants & Réservations
- **Localisation** des restaurants sur carte interactive
- **Système de réservation**
- **Validation transactionnelle** pour éviter les conflits

### 🚴 Données Vélib en Temps Réel
- **API GBFS Cyclocity** pour les stations Nancy
- **Disponibilité temps réel** des vélos et places

### 🚧 Incidents de Circulation
- **Données Grand Nancy** en temps réel
- **Localisation précise** des perturbations

### 🗺️ Frontend 
- **Carte Leaflet** 
- **Bootstrap 5**
- **Templates Handlebars**
- **Javascript** ES6 avec les modules

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │ Service Central │    │  Service BD     │
│   (Webetu)      │◄───┤  HTTP/HTTPS     │◄───┤  Oracle + RMI   │
│   JS Modules    │    │  + RMI Registry │    │  Créneaux       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                       ┌─────────────────┐
                       │ Service Proxy   │
                       │ APIs Externes   │
                       │ + RMI           │
                       └─────────────────┘
```

### Services
- **Service Central** : API + Registre RMI + Support HTTPS
- **Service BD** : Oracle
- **Service Proxy** : Accès API externes (Grand Nancy, Cyclocity)
- **Frontend** : Sur webetu

## 📋 Prérequis

### Développement
- **Java 17+** avec RMI
- **Maven 3.8+** pour la compilation
- **Oracle Database et JDBC**
- **Make** pour facilité la compilation

### Déploiement
- **Accès webetu** pour le frontend
- **Ports réseau** : 8080 (HTTP), 8443 (HTTPS), 1098 (RMI)
- **Certificat SSL** 

## ⚡ Démarrage Rapide

### 1. Configuration
```bash
# Copier le template de configuration
cp .env.exemple .env

# Éditer avec vos paramètres
nano .env
```

**Variables essentielles** :
```bash
# Service Central
CENTRAL_HOST=votre_ip_machine
CENTRAL_HTTPS_ENABLED=true

# Base de Données
BD_JDBC_URL=jdbc:oracle:thin:@charlemagne.iutnc.univ-lorraine.fr:1521:infodb
BD_JDBC_USER=votre_login
BD_JDBC_PASSWORD=votre_password

# Webetu (pour déploiement frontend)
WEBETU_USER=votre_login_webetu
```

### 2. Compilation & Démarrage (avec Make)
```bash
# Démarrage complet des services backend
make deploy

# Déploiement du frontend sur webetu
make deploy-frontend
```

### 3. Démarrage Manuel
```bash
# Compilation
mvn clean package dependency:copy-dependencies

# Service Central (Terminal 1)
cd service-central
java -cp "target/*:../common/target/*" Main

# Service BD (Terminal 2) 
cd service-bd
java -cp "target/*:../common/target/*" Main

# Service Proxy (Terminal 3)
cd service-proxy  
java -cp "target/*:../common/target/*" Main

# Frontend (compilation et déploiement)
cd frontend
mvn package -Pprod
# Puis upload manuel sur webetu
```

## 📁 Structure du Projet

```
nancy-spot/
├── common/                 # Interfaces RMI et modèles partagés
│   ├── src/main/java/
│   │   ├── interfaces/     # ServiceBD, ServiceProxy, ServiceCentral
│   │   ├── model/         # Restaurant, Reservation, Creneau, TableResto
│   │   └── utils/         # Configurateur, HttpUtils
├── service-central/        # Service HTTP/RMI central
│   ├── src/main/java/
│   │   ├── handlers/      # RestaurantsHandler, ReserverHandler, etc.
│   │   ├── rmi/          # Serveur (implémentation ServiceCentral)
│   │   └── server/       # HttpServerCentral, CorsFilter
├── service-bd/            # Service base de données
│   ├── src/
│   │   ├── main/java/
│   │   │   ├── dao/      # RestaurantDAO
│   │   │   └── rmi/      # BaseDonnee (implémentation ServiceBD)
│   │   └── create_tables.sql  # Script de création BD
├── service-proxy/         # Service APIs externes
│   ├── src/main/java/
│   │   ├── clients/      # IncidentsClient, BaseHttpClient  
│   │   └── rmi/         # Proxy (implémentation ServiceProxy)
├── frontend/              # Interface web
│   ├── src/main/webapp/
│   │   ├── js/           # Modules ES6
│   │   │   ├── managers/ # RestaurantManager, VelibManager, etc.
│   │   │   ├── services/ # ApiService
│   │   │   └── utils/    # UIUtils
│   │   ├── css/         # Styles personnalisés
│   │   └── index.html   # SPA avec templates Handlebars
├── docker/               # Configuration Docker
├── Makefile             # Automatisation du build/deploy
└── .env.exemple         # Template de configuration
```

## 🎯 Endpoints API

### Restaurants & Réservations
```http
GET    /restaurants                              # Liste restaurants
GET    /creneaux                                # Créneaux disponibles
GET    /tables/libres/{restaurantId}/{date}/{creneauId}    # Tables libres
POST   /reserver                                # Effectuer réservation
GET    /reservations/date/{restaurantId}/{date} # Réservations du jour
```

### Données Externes
```http
GET    /incidents                               # Incidents circulation
GET    /services/etat                          # État des services
```


## 🗄️ Base de Données

```sql
-- Créneaux horaires (Déjeuner, Dîner, Brunch)
CREATE TABLE creneau (
    id NUMBER PRIMARY KEY,
    libelle VARCHAR2(50) NOT NULL,
    heure_debut VARCHAR2(5) NOT NULL,
    heure_fin VARCHAR2(5) NOT NULL,
    actif NUMBER(1) DEFAULT 1
);

-- Restaurants avec géolocalisation
CREATE TABLE restaurant (
    id NUMBER PRIMARY KEY,
    nom VARCHAR2(100) NOT NULL,
    adresse VARCHAR2(200) NOT NULL,
    telephone VARCHAR2(20),
    latitude NUMBER(10,7) NOT NULL,
    longitude NUMBER(10,7) NOT NULL
);

-- Tables par restaurant
CREATE TABLE tables_resto (
    id NUMBER PRIMARY KEY,
    restaurant_id NUMBER NOT NULL,
    numero_table NUMBER NOT NULL,
    nb_places NUMBER NOT NULL,
    FOREIGN KEY (restaurant_id) REFERENCES restaurant(id)
);

-- Réservations avec créneaux
CREATE TABLE reservation (
    id NUMBER PRIMARY KEY,
    table_id NUMBER NOT NULL,
    creneau_id NUMBER NOT NULL,
    date_reservation DATE NOT NULL,
    nom_client VARCHAR2(100) NOT NULL,
    prenom_client VARCHAR2(100) NOT NULL,
    telephone VARCHAR2(20) NOT NULL,
    nb_convives NUMBER NOT NULL,
    statut VARCHAR2(20) DEFAULT 'confirmee',
    FOREIGN KEY (table_id) REFERENCES tables_resto(id),
    FOREIGN KEY (creneau_id) REFERENCES creneau(id),
    UNIQUE (table_id, creneau_id, date_reservation)
);
```

### Initialisation
```bash
# Exécuter le script de création
sqlplus votre_user@charlemagne.iutnc.univ-lorraine.fr:1521/infodb
@service-bd/src/create_tables.sql
```

## 🔧 Commandes Make

```bash
# JavaDoc
make javadoc            # générer la javadoc

# Construction et déploiement
make deploy              # Compile + démarre services backend
make deploy-frontend     # Compile + déploie frontend sur webetu

# Services individuels  
make start-central       # Démarre service central uniquement
make start-bd           # Démarre service BD uniquement
make start-proxy        # Démarre service proxy uniquement

# Monitoring
make status             # État de tous les services
make health             # Test de santé des APIs
make logs               # Logs récents de tous les services
make logs-central       # Logs service central en temps réel

# HTTPS
make check-cert         # Vérifier certificat SSL
make generate-cert      # Générer certificat auto-signé

# Maintenance
make stop               # Arrêter tous les services
make clean              # Nettoyer les fichiers compilés
```

## 🔒 Configuration HTTPS

### Activation
```bash
# Dans .env
CENTRAL_HTTPS_ENABLED=true
CENTRAL_HTTPS_PORT=8443

# Générer certificat auto-signé
make generate-cert
```


## 🌐 APIs Externes

### Vélib Nancy (GBFS)
- **Découverte** : `https://api.cyclocity.fr/contracts/nancy/gbfs/gbfs.json`
- **Stations** : `https://api.cyclocity.fr/contracts/nancy/gbfs/station_information.json`
- **Statut** : `https://api.cyclocity.fr/contracts/nancy/gbfs/station_status.json`

### Incidents Grand Nancy
- **URL** : `https://carto.g-ny.org/data/cifs/cifs_waze_v2.json`
- **Types** : Travaux, accidents, manifestations, embouteillages


## 👥 Groupe
Ryan Korban
Baptiste Delaborde
Baptiste Hennequin
Maxence Eva
