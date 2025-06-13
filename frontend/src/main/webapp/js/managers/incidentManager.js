import { NANCY_CONFIG } from '../config/constants.js';
import { UIUtils } from '../utils/ui.js';

/**
 * Gestionnaire des incidents de circulation
 * Gère le chargement, le formatage et l'affichage des incidents de trafic
 * @module managers/incidentManager
 */
export class IncidentManager {
    /**
     * Crée une instance du gestionnaire d'incidents
     * @constructor
     * @param {ApiService} apiService - Service API pour les requêtes
     * @param {MapManager} mapManager - Gestionnaire de carte pour l'affichage
     */
    constructor(apiService, mapManager) {

        this.api = apiService;

        this.map = mapManager;
        this.incidents = [];
        this.template = null;

        this.initTemplate();
    }

    /**
     * Initialise le template Handlebars pour les popups des incidents
     */
    initTemplate() {
        const template = document.getElementById('incident-popup-template');
        if (template) {
            this.template = Handlebars.compile(template.innerHTML);
        }
    }

    /**
     * Charge la liste des incidents depuis l'API
     * @async
     * @returns {Promise<Array>} Liste des incidents ou tableau vide en cas d'erreur
     * @throws {Error} En cas d'erreur de communication avec l'API
     */
    async loadIncidents() {
        try {
            const data = await this.api.get(NANCY_CONFIG.ENDPOINTS.INCIDENTS);
            this.incidents = data.incidents || [];
            return this.incidents;
        } catch (error) {
            console.error('Erreur chargement incidents:', error);
            UIUtils.showToast('Erreur lors du chargement des incidents', 'warning');
            return [];
        }
    }

    /**
     * Affiche tous les incidents sur la carte sous forme de marqueurs
     * Traite les données des incidents pour améliorer l'affichage
     */
    displayOnMap() {
        this.map.clearMarkers('incidents');

        this.incidents.forEach(incident => {
            const processedIncident = {
                ...incident,
                typeLabel: this.getTypeLabel(incident.type),
                dateDebut: this.formatDate(incident.dateDebut),
                dateFin: this.formatDate(incident.dateFin)
            };

            const popupContent = this.template ? this.template(processedIncident) :
                `<h6>${incident.titre}</h6><p>${incident.description}</p>`;

            this.map.addMarker('incidents', incident, popupContent);
        });
    }

    /**
     * Convertit un type d'incident en libellé lisible
     * @private
     * @param {string} type - Type d'incident brut (ex: 'travaux', 'accident')
     * @returns {string} Libellé formaté pour l'affichage
     */
    getTypeLabel(type) {
        const labels = {
            'travaux': 'Travaux',
            'accident': 'Accident',
            'manifestation': 'Manifestation',
            'panne': 'Panne',
            'deviation': 'Déviation'
        };
        return labels[type] || type.toUpperCase();
    }

    /**
     * Formate une chaîne de date en format français lisible
     * @private
     * @param {string} dateStr - Date au format ISO ou chaîne de date
     * @returns {string} Date formatée en français ou chaîne originale si erreur
     */
    formatDate(dateStr) {
        if (!dateStr) return '';
        const date = new Date(dateStr);
        if (isNaN(date)) return dateStr;

        return date.toLocaleDateString('fr-FR', {
            day: '2-digit', month: '2-digit', year: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });
    }
}