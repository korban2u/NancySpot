/**
 * Configuration globale de l'application Nancy Spot
 * @module config/constants
 */
export const NANCY_CONFIG = {
    /** URL de base du service central - remplacée lors du build Maven */
    API_BASE_URL: 'API_BASE_URL_PLACEHOLDER',

    /** Points d'entrée de l'API REST backend */
    ENDPOINTS: {
        /** Endpoint pour récupérer la liste des restaurants */
        RESTAURANTS: '/restaurants',

        /** Endpoint pour récupérer les tables d'un restaurant (déprécié) */
        TABLES: '/tables',

        /** Endpoint pour récupérer les tables libres pour un créneau spécifique */
        TABLES_LIBRES_CRENEAU: '/tables/libres',

        /** Endpoint pour récupérer toutes les tables avec leur statut */
        TABLES_STATUT: '/tables/statut',

        /** Endpoint pour effectuer une réservation */
        RESERVER: '/reserver',

        /** Endpoint pour récupérer la liste des créneaux disponibles */
        CRENEAUX: '/creneaux',

        /** Endpoint pour récupérer un créneau spécifique */
        CRENEAU_BY_ID: '/creneaux',

        /** Endpoint pour vérifier la disponibilité d'une table */
        VERIFIER_DISPONIBILITE: '/tables/disponibilite',

        /** Endpoint pour récupérer les réservations d'une date */
        RESERVATIONS_DATE: '/reservations/date',

        /** Endpoint pour annuler une réservation */
        ANNULER_RESERVATION: '/reservations/annuler',

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
    },

    /** Configuration des créneaux horaires */
    CRENEAUX: {
        /** Styles CSS pour les différents types de créneaux */
        STYLES: {
            DEJEUNER: {
                color: '#0d6efd',
                icon: 'bi-sun',
                badge: 'bg-primary'
            },
            DINER: {
                color: '#6f42c1',
                icon: 'bi-moon',
                badge: 'bg-purple'
            },
            BRUNCH: {
                color: '#fd7e14',
                icon: 'bi-cup-hot',
                badge: 'bg-orange'
            },
            DEFAULT: {
                color: '#6c757d',
                icon: 'bi-clock',
                badge: 'bg-secondary'
            }
        },

        /** Configuration des validations */
        VALIDATION: {
            /** Délai minimum entre la réservation et la date (en heures) */
            MIN_ADVANCE_HOURS: 2,
            /** Nombre maximum de jours à l'avance pour réserver */
            MAX_ADVANCE_DAYS: 30,
            /** Nombre maximum de convives par table */
            MAX_CONVIVES: 20
        }
    },

    /** Messages et textes de l'interface */
    MESSAGES: {
        /** Messages de succès */
        SUCCESS: {
            RESERVATION_CONFIRMEE: 'Réservation confirmée avec succès !',
            RESERVATION_ANNULEE: 'Réservation annulée avec succès',
            DONNEES_CHARGEES: 'Données chargées avec succès'
        },

        /** Messages d'erreur */
        ERROR: {
            CONNEXION_SERVEUR: 'Erreur de connexion au serveur',
            DONNEES_INVALIDES: 'Données saisies invalides',
            TABLE_INDISPONIBLE: 'Cette table n\'est plus disponible',
            CRENEAU_COMPLET: 'Ce créneau est complet',
            DATE_INVALIDE: 'Date de réservation invalide',
            TELEPHONE_INVALIDE: 'Format de téléphone invalide (10 chiffres requis)'
        },

        /** Messages d'avertissement */
        WARNING: {
            AUCUNE_TABLE: 'Aucune table disponible pour ce créneau',
            AUCUN_CRENEAU: 'Aucun créneau disponible pour cette date',
            DONNEES_MANQUANTES: 'Veuillez remplir tous les champs obligatoires'
        },

        /** Messages informatifs */
        INFO: {
            SELECTION_DATE: 'Sélectionnez une date pour voir les créneaux disponibles',
            SELECTION_CRENEAU: 'Choisissez un créneau horaire',
            SELECTION_TABLE: 'Sélectionnez une table disponible',
            CHARGEMENT: 'Chargement en cours...'
        }
    },

    /** Configuration du format des dates et heures */
    FORMATS: {
        /** Format de date pour l'API (ISO) */
        DATE_API: 'YYYY-MM-DD',
        /** Format de date pour l'affichage */
        DATE_DISPLAY: 'DD/MM/YYYY',
        /** Format de date et heure pour l'affichage */
        DATETIME_DISPLAY: 'DD/MM/YYYY HH:mm',
        /** Format d'heure pour les créneaux */
        TIME_DISPLAY: 'HH:mm'
    },

    /** Configuration des animations et délais */
    ANIMATION: {
        /** Délai d'attente avant transition (ms) */
        TRANSITION_DELAY: 300,
        /** Durée des toasts (ms) */
        TOAST_DURATION: 4000,
        /** Délai de validation temps réel (ms) */
        VALIDATION_DELAY: 100
    },

    /** Configuration du débogage */
    DEBUG: {
        /** Activer les logs détaillés */
        ENABLED: false,
        /** Activer les logs d'état de réservation */
        RESERVATION_STATE: false,
        /** Activer les logs d'API */
        API_CALLS: false
    }
};