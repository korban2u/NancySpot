/**
 * Configuration globale de l'application Nancy Spot
 * Contient les URLs d'API, paramètres de carte et configuration des marqueurs
 * @module config/constants
 */
export const NANCY_CONFIG = {
    /** URL de base du service central - remplacée lors du build Maven */
    API_BASE_URL: 'API_BASE_URL_PLACEHOLDER',

    /** Points d'entrée de l'API REST backend */
    ENDPOINTS: {
        /** Endpoint pour récupérer la liste des restaurants */
        RESTAURANTS: '/restaurants',
        /** Endpoint pour récupérer les tables d'un restaurant */
        TABLES: '/tables',
        /** Endpoint pour effectuer une réservation */
        RESERVER: '/reserver',
        /** Endpoint pour récupérer les incidents de circulation */
        INCIDENTS: '/incidents',
        /** Endpoint pour vérifier l'état des services */
        STATUS: '/services/etat'
    },

    /** APIs externes consommées directement côté frontend */
    EXTERNAL_APIS: {
        /** API Vélib Nancy (Cyclocity GBFS) */
        VELIB: {
            /** URL de découverte GBFS */
            DISCOVERY: 'https://api.cyclocity.fr/contracts/nancy/gbfs/gbfs.json',
            /** Informations statiques des stations */
            STATION_INFO: 'https://api.cyclocity.fr/contracts/nancy/gbfs/station_information.json',
            /** Statut en temps réel des stations */
            STATION_STATUS: 'https://api.cyclocity.fr/contracts/nancy/gbfs/station_status.json'
        }
    },

    /** Configuration de la carte Leaflet */
    MAP: {
        /** Centre de la carte sur Nancy [latitude, longitude] */
        CENTER: [48.6921, 6.1844],
        /** Niveau de zoom par défaut */
        ZOOM: 13
    },

    /** Configuration des marqueurs par type de données */
    MARKERS: {
        /** Configuration des marqueurs restaurants */
        RESTAURANTS: {
            color: '#0d6efd',
            icon: 'bi-shop'
        },
        /** Configuration des marqueurs Vélib */
        VELIB: {
            color: '#198754',
            icon: 'bi-bicycle'
        },
        /** Configuration des marqueurs incidents */
        INCIDENTS: {
            color: '#ffc107',
            icon: 'bi-exclamation-triangle'
        }
    }
};