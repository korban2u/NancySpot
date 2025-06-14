/**
 * Configuration globale de l'application Nancy Interactive
 * Contient les URLs d'API, paramètres de carte et configuration des marqueurs
 * @module config/constants
 * @constant {Object} NANCY_CONFIG
 * @property {string} API_BASE_URL - URL de base pour les appels API
 * @property {Object} ENDPOINTS - Points d'entrée de l'API
 * @property {Object} MAP - Configuration de la carte Leaflet
 * @property {Object} MARKERS - Configuration des marqueurs de carte
 */
export const NANCY_CONFIG = {
    /** URL de base du service central - remplacée lors du build Maven */
    API_BASE_URL: 'https://172.22.152.208:8443',

    /** Points d'entrée de l'API REST */
    ENDPOINTS: {
        /** Endpoint pour récupérer la liste des restaurants */
        RESTAURANTS: '/restaurants',
        /** Endpoint pour récupérer les tables d'un restaurant */
        TABLES: '/tables',
        /** Endpoint pour effectuer une réservation */
        RESERVER: '/reserver',
        /** Endpoint pour récupérer les données Vélib */
        VELIB: '/velib',
        /** Endpoint pour récupérer les incidents de circulation */
        INCIDENTS: '/incidents',
        /** Endpoint pour vérifier l'état des services */
        STATUS: '/services/etat'
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
            /** Couleur de fond du marqueur restaurant */
            color: '#0d6efd',
            /** Icône Bootstrap utilisée pour les restaurants */
            icon: 'bi-shop'
        },
        /** Configuration des marqueurs Vélib */
        VELIB: {
            /** Couleur de fond du marqueur Vélib */
            color: '#198754',
            /** Icône Bootstrap utilisée pour les stations Vélib */
            icon: 'bi-bicycle'
        },
        /** Configuration des marqueurs incidents */
        INCIDENTS: {
            /** Couleur de fond du marqueur incident */
            color: '#ffc107',
            /** Icône Bootstrap utilisée pour les incidents */
            icon: 'bi-exclamation-triangle'
        }
    }
};