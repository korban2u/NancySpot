import { NANCY_CONFIG } from '../config/constants.js';

/**
 * Gestionnaire de la carte Leaflet
 * Gère l'initialisation, les marqueurs et les interactions avec la carte
 * @module managers/mapManager
 */
export class MapManager {
    /**
     * Crée une instance du gestionnaire de carte
     * @constructor
     */
    constructor() {
        this.map = null;

        this.markers = {
            restaurants: [],
            velib: [],
            incidents: []
        };
    }

    /**
     * Initialise la carte Leaflet dans le conteneur #map
     * Configure la carte avec le centre et zoom définis dans la configuration
     * @throws {Error} Si l'élément #map n'existe pas dans le DOM
     */
    init() {
        this.map = L.map('map', {
            center: NANCY_CONFIG.MAP.CENTER,
            zoom: NANCY_CONFIG.MAP.ZOOM
        });

        // Ajout de la couche de tuiles OpenStreetMap
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
        }).addTo(this.map);
    }

    /**
     * Crée une icône personnalisée pour un type de marqueur donné
     * @private
     * @param {string} type - Type de marqueur (restaurants, velib, incidents)
     * @returns {L.DivIcon} Icône Leaflet personnalisée avec couleur et symbole
     */
    createMarkerIcon(type) {
        const config = NANCY_CONFIG.MARKERS[type.toUpperCase()];
        return L.divIcon({
            html: `<div class="marker-icon" style="background-color: ${config.color}">
                     <i class="bi ${config.icon}"></i>
                   </div>`,
            className: 'custom-div-icon',
            iconSize: [36, 36],
            iconAnchor: [18, 36],
            popupAnchor: [0, -36]
        });
    }

    /**
     * Ajoute un marqueur sur la carte
     * @param {string} type - Type de marqueur pour définir l'apparence
     * @param {Object} data - Données contenant les coordonnées
     * @param {number} data.latitude - Latitude du point
     * @param {number} data.longitude - Longitude du point
     * @param {string} popupContent - Contenu HTML du popup (tooltip)
     * @returns {L.Marker|null} Le marqueur créé ou null si coordonnées invalides
     */
    addMarker(type, data, popupContent) {
        if (!this.isValidCoordinate(data.latitude, data.longitude)) {
            return null;
        }

        const marker = L.marker([data.latitude, data.longitude], {
            icon: this.createMarkerIcon(type)
        });

        marker.bindPopup(popupContent, { maxWidth: 300 });
        marker.addTo(this.map);
        this.markers[type].push(marker);

        return marker;
    }

    /**
     * Supprime tous les marqueurs d'un type donné
     * @param {string} type - Type de marqueurs à supprimer (restaurants, velib, incidents)
     */
    clearMarkers(type) {
        this.markers[type].forEach(marker => marker.remove());
        this.markers[type] = [];
    }

    /**
     * Valide que les coordonnées sont correctes
     * @private
     * @param {number} lat - Latitude à valider
     * @param {number} lng - Longitude à valider
     * @returns {boolean} true si les coordonnées sont valides, false sinon
     */
    isValidCoordinate(lat, lng) {
        return lat && lng && !isNaN(lat) && !isNaN(lng) &&
            lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
    }

    /**
     * Force le recalcul de la taille de la carte
     * Utile après un changement de taille du conteneur ou d'onglet
     */
    invalidateSize() {
        if (this.map) {
            setTimeout(() => this.map.invalidateSize(), 100);
        }
    }
}