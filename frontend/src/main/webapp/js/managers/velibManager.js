import { NANCY_CONFIG } from '../config/constants.js';
import { UIUtils } from '../utils/ui.js';

/**
 * Gestionnaire des stations Vélib
 * Gère le chargement et l'affichage des stations de vélos en libre-service
 * @module managers/velibManager
 */
export class VelibManager {
    /**
     * Crée une instance du gestionnaire Vélib
     * @constructor
     * @param {ApiService} apiService - Service API pour les requêtes
     * @param {MapManager} mapManager - Gestionnaire de carte pour l'affichage
     */
    constructor(apiService, mapManager) {
        /** @private {ApiService} Service pour les appels API */
        this.api = apiService;

        /** @private {MapManager} Gestionnaire de la carte */
        this.map = mapManager;

        /** @public {Array} Liste des stations Vélib chargées */
        this.stations = [];

        /** @private {Function|null} Template Handlebars compilé pour les popups */
        this.template = null;

        this.initTemplate();
    }

    /**
     * Initialise le template Handlebars pour les popups des stations
     * @private
     * @example
     * this.initTemplate(); // Appelé automatiquement dans le constructeur
     */
    initTemplate() {
        const template = document.getElementById('velib-popup-template');
        if (template) {
            this.template = Handlebars.compile(template.innerHTML);
        }
    }

    /**
     * Charge la liste des stations Vélib depuis l'API
     * @async
     * @returns {Promise<Array>} Liste des stations ou tableau vide en cas d'erreur
     * @throws {Error} En cas d'erreur de communication avec l'API
     * @example
     * const stations = await velibManager.loadStations();
     * console.log(`${stations.length} stations Vélib chargées`);
     */
    async loadStations() {
        try {
            const data = await this.api.get(NANCY_CONFIG.ENDPOINTS.VELIB);
            this.stations = data.stations || [];
            return this.stations;
        } catch (error) {
            console.error('Erreur chargement Vélib:', error);
            UIUtils.showToast('Erreur lors du chargement des stations Vélib', 'warning');
            return [];
        }
    }

    /**
     * Affiche toutes les stations Vélib sur la carte sous forme de marqueurs
     * Utilise le template Handlebars pour générer le contenu des popups
     * @example
     * await velibManager.loadStations();
     * velibManager.displayOnMap();
     */
    displayOnMap() {
        this.map.clearMarkers('velib');

        this.stations.forEach(station => {
            const popupContent = this.template ? this.template(station) :
                `<h6>${station.nom}</h6>
                 <p>Vélos: ${station.velosDisponibles} | Places: ${station.placesDisponibles}</p>`;

            this.map.addMarker('velib', station, popupContent);
        });
    }
}