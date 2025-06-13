import { NANCY_CONFIG } from '../config/constants.js';
import { UIUtils } from '../utils/ui.js';

/**
 * Gestionnaire des restaurants et réservations
 * Gère le chargement, l'affichage et la réservation des restaurants avec templates Handlebars
 * @module managers/restaurantManager
 */
export class RestaurantManager {
    /**
     * Crée une instance du gestionnaire de restaurants
     * @constructor
     * @param {ApiService} apiService - Service API pour les requêtes
     * @param {MapManager} mapManager - Gestionnaire de carte pour l'affichage
     */
    constructor(apiService, mapManager) {

        this.api = apiService;

        this.map = mapManager;

        this.restaurants = [];

        this.templates = {
            popup: null,
            reservation: null,
            tableOptions: null
        };

        this.initTemplates();
    }

    /**
     * Initialise tous les templates Handlebars pour les restaurants
     */
    initTemplates() {
        // Template pour les popups des marqueurs restaurants
        const popupTemplate = document.getElementById('restaurant-popup-template');
        if (popupTemplate) {
            this.templates.popup = Handlebars.compile(popupTemplate.innerHTML);
        }

        // Template pour le formulaire de réservation
        const reservationTemplate = document.getElementById('reservation-form-template');
        if (reservationTemplate) {
            this.templates.reservation = Handlebars.compile(reservationTemplate.innerHTML);
        }

        // Template pour les options de tables disponibles
        const tableOptionsTemplate = document.getElementById('table-options-template');
        if (tableOptionsTemplate) {
            this.templates.tableOptions = Handlebars.compile(tableOptionsTemplate.innerHTML);
        }
    }

    /**
     * Charge la liste des restaurants depuis l'API
     * @async
     * @returns {Promise<Array>} Liste des restaurants ou tableau vide en cas d'erreur
     * @throws {Error} En cas d'erreur de communication avec l'API
     */
    async loadRestaurants() {
        try {
            const data = await this.api.get(NANCY_CONFIG.ENDPOINTS.RESTAURANTS);
            this.restaurants = data.restaurants || [];
            return this.restaurants;
        } catch (error) {
            console.error('Erreur chargement restaurants:', error);
            UIUtils.showToast('Erreur lors du chargement des restaurants', 'danger');
            return [];
        }
    }

    /**
     * Affiche tous les restaurants sur la carte sous forme de marqueurs
     * Utilise uniquement les templates Handlebars pour générer le contenu
     */
    displayOnMap() {
        this.map.clearMarkers('restaurants');

        this.restaurants.forEach(restaurant => {
            // Utilisation exclusive du template Handlebars
            const popupContent = this.templates.popup ?
                this.templates.popup(restaurant) :
                `<div><h6>${restaurant.nom}</h6><p>${restaurant.adresse}</p></div>`;

            this.map.addMarker('restaurants', restaurant, popupContent);
        });
    }

    /**
     * Affiche le formulaire modal de réservation pour un restaurant
     * Utilise Handlebars pour générer tout le HTML du formulaire
     * @async
     * @param {string|number} restaurantId - ID du restaurant
     * @param {string} restaurantName - Nom du restaurant pour l'affichage
     */
    async showReservationForm(restaurantId, restaurantName) {
        if (!this.templates.reservation) return;

        // Génération du formulaire avec Handlebars
        const formHtml = this.templates.reservation({
            restaurantId,
            restaurantName
        });

        document.body.insertAdjacentHTML('beforeend', formHtml);
        await this.loadAvailableTables(restaurantId);
        this.setMinimumDate();
        this.setupReservationEvents();
    }

    /**
     * Charge les tables disponibles et met à jour le select avec un template
     * @async
     * @param {string|number} restaurantId - ID du restaurant
     */
    async loadAvailableTables(restaurantId) {
        const select = document.getElementById('tableId');
        if (!select) return;

        try {
            // État de chargement avec template
            select.innerHTML = this.templates.tableOptions ?
                this.templates.tableOptions({ loading: true }) :
                '<option value="">Chargement...</option>';

            const data = await this.api.get(`${NANCY_CONFIG.ENDPOINTS.TABLES}/${restaurantId}`);
            const tables = data.tables || [];

            if (tables.length === 0) {
                // Aucune table disponible avec template
                select.innerHTML = this.templates.tableOptions ?
                    this.templates.tableOptions({ empty: true }) :
                    '<option value="">Aucune table disponible</option>';
                UIUtils.showToast('Aucune table disponible', 'warning');
                return;
            }

            // Tables disponibles avec template
            select.innerHTML = this.templates.tableOptions ?
                this.templates.tableOptions({ tables: tables }) :
                '<option value="">Choisir une table...</option>' +
                tables.map(table =>
                    `<option value="${table.id}">Table ${table.numeroTable} (${table.nbPlaces} places)</option>`
                ).join('');

        } catch (error) {
            // Erreur avec template
            select.innerHTML = this.templates.tableOptions ?
                this.templates.tableOptions({ error: true }) :
                '<option value="">Erreur chargement</option>';
            UIUtils.showToast('Erreur lors du chargement des tables', 'danger');
        }
    }

    /**
     * Configure les événements pour le formulaire de réservation
     */
    setupReservationEvents() {
        document.addEventListener('click', (e) => {
            if (e.target.matches('#close-reservation, #cancel-reservation')) {
                this.hideReservationForm();
            }
        });

        document.addEventListener('submit', (e) => {
            if (e.target.matches('#reservation-form')) {
                e.preventDefault();
                this.handleReservation(e.target);
            }
        });
    }

    /**
     * Traite la soumission du formulaire de réservation
     * @async
     * @param {HTMLFormElement} form - Formulaire de réservation soumis
     */
    async handleReservation(form) {
        const formData = new FormData(form);

        const reservation = {
            tableId: parseInt(formData.get('tableId')),
            nomClient: formData.get('nomClient'),
            prenomClient: formData.get('prenomClient'),
            telephone: formData.get('telephone'),
            nbConvives: parseInt(formData.get('nbConvives')),
            dateReservation: formData.get('dateReservation').replace('T', ' ')
        };

        try {
            UIUtils.showLoading(true);
            const result = await this.api.post(NANCY_CONFIG.ENDPOINTS.RESERVER, reservation);

            if (result.success) {
                this.hideReservationForm();
                UIUtils.showToast('Réservation confirmée avec succès!', 'success');
            } else {
                UIUtils.showToast(result.message || 'Erreur lors de la réservation', 'danger');
            }
        } catch (error) {
            UIUtils.showToast('Erreur de connexion au serveur', 'danger');
        } finally {
            UIUtils.showLoading(false);
        }
    }

    /**
     * Ferme et supprime le formulaire modal de réservation
     */
    hideReservationForm() {
        const modal = document.getElementById('reservation-modal');
        const backdrop = document.querySelector('.modal-backdrop');
        if (modal) modal.remove();
        if (backdrop) backdrop.remove();
    }

    /**
     * Définit la date minimum pour les réservations (30 minutes dans le futur)
     */
    setMinimumDate() {
        const dateInput = document.getElementById('dateReservation');
        if (dateInput) {
            const now = new Date();
            now.setMinutes(now.getMinutes() + 30);
            dateInput.min = now.toISOString().slice(0, 16);
        }
    }
}