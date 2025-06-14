import { NANCY_CONFIG } from '../config/constants.js';
import { UIUtils } from '../utils/ui.js';

/**
 * Gestionnaire des restaurants et réservations avec support des créneaux horaires
 * Version améliorée avec workflow de réservation en 3 étapes
 * @module managers/restaurantManager
 */
export class RestaurantManager {
    constructor(apiService, mapManager) {
        this.api = apiService;
        this.map = mapManager;
        this.restaurants = [];
        this.creneaux = [];

        // État de la réservation en cours
        this.reservationState = {
            restaurantId: null,
            restaurantName: null,
            selectedDate: null,
            selectedCreneauId: null,
            selectedTableId: null,
            currentStep: 'date' // 'date', 'creneau', 'table'
        };

        this.templates = {
            popup: null,
            reservationForm: null,
            creneauxDisponibles: null,
            tablesAvecStatut: null,
            reservationSummary: null,
            reservationStatus: null
        };

        this.initTemplates();
    }

    /**
     * Initialise tous les templates Handlebars
     */
    initTemplates() {
        this.templates.popup = UIUtils.compileTemplate('restaurant-popup-creneaux-template');
        this.templates.reservationForm = UIUtils.compileTemplate('reservation-form-creneaux-template');
        this.templates.creneauxDisponibles = UIUtils.compileTemplate('creneaux-disponibles-template');
        this.templates.tablesAvecStatut = UIUtils.compileTemplate('tables-avec-statut-template');
        this.templates.reservationSummary = UIUtils.compileTemplate('reservation-summary-template');
        this.templates.reservationStatus = UIUtils.compileTemplate('reservation-status-template');
    }

    /**
     * Charge la liste des restaurants depuis l'API
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
     * Charge la liste des créneaux disponibles
     */
    async loadCreneaux() {
        try {
            const data = await this.api.get(NANCY_CONFIG.ENDPOINTS.CRENEAUX);
            this.creneaux = data.creneaux || [];
            console.log('Créneaux chargés:', this.creneaux);
            return this.creneaux;
        } catch (error) {
            console.error('Erreur chargement créneaux:', error);
            UIUtils.showToast('Erreur lors du chargement des créneaux', 'warning');
            return [];
        }
    }

    /**
     * Affiche tous les restaurants sur la carte
     */
    displayOnMap() {
        this.map.clearMarkers('restaurants');

        this.restaurants.forEach(restaurant => {
            const popupContent = this.templates.popup ?
                this.templates.popup(restaurant) :
                `<div><h6>${restaurant.nom}</h6><p>${restaurant.adresse}</p></div>`;

            this.map.addMarker('restaurants', restaurant, popupContent);
        });
    }

    /**
     * Affiche le formulaire modal de réservation
     */
    async showReservationForm(restaurantId, restaurantName) {
        if (!this.templates.reservationForm) {
            UIUtils.showToast('Templates non initialisés', 'danger');
            return;
        }

        // Initialisation de l'état
        this.reservationState = {
            restaurantId: parseInt(restaurantId),
            restaurantName: restaurantName,
            selectedDate: null,
            selectedCreneauId: null,
            selectedTableId: null,
            currentStep: 'date'
        };

        // Chargement des créneaux si nécessaire
        if (this.creneaux.length === 0) {
            await this.loadCreneaux();
        }

        // Génération du formulaire
        const formHtml = this.templates.reservationForm({
            restaurantId: restaurantId,
            restaurantName: restaurantName
        });

        document.body.insertAdjacentHTML('beforeend', formHtml);

        this.setMinimumDate();
        this.setupReservationEvents();

        console.log('Formulaire de réservation affiché pour:', restaurantName);
    }

    /**
     * Configure tous les événements du formulaire de réservation
     */
    setupReservationEvents() {
        // Fermeture du modal
        document.addEventListener('click', this.handleModalClose.bind(this));

        // Navigation entre étapes
        document.addEventListener('click', this.handleStepNavigation.bind(this));

        // Sélection de créneau
        document.addEventListener('click', this.handleCreneauSelection.bind(this));

        // Sélection de table
        document.addEventListener('click', this.handleTableSelection.bind(this));

        // Soumission du formulaire
        document.addEventListener('submit', this.handleFormSubmission.bind(this));

        // Validation temps réel
        document.addEventListener('input', this.handleFormValidation.bind(this));
    }

    /**
     * Gère la fermeture du modal
     */
    handleModalClose(e) {
        if (e.target.matches('#close-reservation, #cancel-reservation')) {
            this.hideReservationForm();
        }
    }

    /**
     * Gère la navigation entre les étapes
     */
    async handleStepNavigation(e) {
        if (e.target.matches('#btn-next-date')) {
            await this.goToCreneauStep();
        } else if (e.target.matches('#btn-back-date')) {
            this.goToDateStep();
        } else if (e.target.matches('#btn-back-creneau')) {
            this.goToCreneauStep();
        }
    }

    /**
     * Passe à l'étape de sélection des créneaux
     */
    async goToCreneauStep() {
        const dateInput = document.getElementById('dateReservation');
        const selectedDate = dateInput.value;

        if (!selectedDate) {
            UIUtils.showToast('Veuillez sélectionner une date', 'warning');
            return;
        }

        // Validation de la date
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const reservationDate = new Date(selectedDate);

        if (reservationDate < today) {
            UIUtils.showToast('La date ne peut pas être dans le passé', 'warning');
            return;
        }

        this.reservationState.selectedDate = selectedDate;
        this.reservationState.currentStep = 'creneau';

        // Affichage de l'étape créneaux
        this.showStep('step-creneau');

        // Mise à jour de l'affichage de la date
        const dateDisplay = document.getElementById('selected-date-display');
        if (dateDisplay) {
            dateDisplay.textContent = this.formatDate(selectedDate);
        }

        // Chargement et affichage des créneaux
        await this.loadAndDisplayCreneaux();
    }

    /**
     * Retourne à l'étape de sélection de date
     */
    goToDateStep() {
        this.reservationState.currentStep = 'date';
        this.reservationState.selectedCreneauId = null;
        this.reservationState.selectedTableId = null;
        this.showStep('step-date');
    }

    /**
     * Charge et affiche les créneaux disponibles
     */
    async loadAndDisplayCreneaux() {
        const container = document.getElementById('creneaux-container');
        const loading = document.getElementById('loading-creneaux');

        if (!container) return;

        try {
            loading.style.display = 'block';
            container.innerHTML = '';

            // Les créneaux sont déjà chargés globalement
            const creneauxActifs = this.creneaux.filter(c => c.actif !== false);

            if (this.templates.creneauxDisponibles) {
                container.innerHTML = this.templates.creneauxDisponibles({
                    creneaux: creneauxActifs
                });
            } else {
                container.innerHTML = '<div class="alert alert-warning">Erreur de chargement des créneaux</div>';
            }

        } catch (error) {
            console.error('Erreur chargement créneaux:', error);
            container.innerHTML = '<div class="alert alert-danger">Erreur lors du chargement des créneaux</div>';
        } finally {
            loading.style.display = 'none';
        }
    }

    /**
     * Gère la sélection d'un créneau
     */
    async handleCreneauSelection(e) {
        const creneauCard = e.target.closest('.creneau-card');
        if (!creneauCard) return;

        const creneauId = parseInt(creneauCard.dataset.creneauId);

        // Mise à jour visuelle
        document.querySelectorAll('.creneau-card').forEach(card => {
            card.classList.remove('selected');
        });
        creneauCard.classList.add('selected');

        this.reservationState.selectedCreneauId = creneauId;

        // Délai pour l'effet visuel
        setTimeout(async () => {
            await this.goToTableStep();
        }, 300);
    }

    /**
     * Passe à l'étape de sélection des tables
     */
    async goToTableStep() {
        this.reservationState.currentStep = 'table';
        this.showStep('step-table');

        // Mise à jour de l'affichage du créneau
        const creneauDisplay = document.getElementById('selected-creneau-display');
        if (creneauDisplay) {
            const selectedCreneau = this.creneaux.find(c => c.id === this.reservationState.selectedCreneauId);
            if (selectedCreneau) {
                creneauDisplay.textContent = `${selectedCreneau.libelle} (${selectedCreneau.heureDebut} - ${selectedCreneau.heureFin})`;
            }
        }

        // Mise à jour des champs cachés
        const creneauIdInput = document.getElementById('selectedCreneauId');
        const dateInput = document.getElementById('selectedDate');

        if (creneauIdInput) creneauIdInput.value = this.reservationState.selectedCreneauId;
        if (dateInput) dateInput.value = this.reservationState.selectedDate;

        // Chargement des tables
        await this.loadAndDisplayTables();
    }

    /**
     * Charge et affiche les tables avec leur statut
     */
    async loadAndDisplayTables() {
        const container = document.getElementById('tables-container');
        const loading = document.getElementById('loading-tables');

        if (!container) return;

        try {
            loading.style.display = 'block';
            container.innerHTML = '';

            const url = `${NANCY_CONFIG.ENDPOINTS.TABLES_STATUT}/${this.reservationState.restaurantId}/${this.reservationState.selectedDate}/${this.reservationState.selectedCreneauId}`;
            const data = await this.api.get(url);

            if (this.templates.tablesAvecStatut) {
                container.innerHTML = this.templates.tablesAvecStatut({
                    tables: data.tables || []
                });
            }

            // Mise à jour du bouton de confirmation
            this.updateConfirmButton();

        } catch (error) {
            console.error('Erreur chargement tables:', error);
            container.innerHTML = '<div class="alert alert-danger">Erreur lors du chargement des tables</div>';
        } finally {
            loading.style.display = 'none';
        }
    }

    /**
     * Gère la sélection d'une table
     */
    handleTableSelection(e) {
        const tableCard = e.target.closest('.table-card[data-selectable="true"]');
        if (!tableCard) return;

        const tableId = parseInt(tableCard.dataset.tableId);

        // Mise à jour visuelle
        document.querySelectorAll('.table-card').forEach(card => {
            card.classList.remove('selected');
        });
        tableCard.classList.add('selected');

        this.reservationState.selectedTableId = tableId;

        // Mise à jour du champ caché
        const tableIdInput = document.getElementById('tableId');
        if (tableIdInput) {
            tableIdInput.value = tableId;
        }

        // Mise à jour de l'affichage de la capacité
        const tableCapacity = tableCard.querySelector('.card-body').textContent.match(/(\d+) places/)?.[1] || 0;
        const capacityDisplay = document.getElementById('table-capacity');
        const nbConvivesInput = document.getElementById('nbConvives');

        if (capacityDisplay) {
            capacityDisplay.textContent = tableCapacity;
        }

        // Ajuster le max du input
        if (nbConvivesInput) {
            nbConvivesInput.max = tableCapacity;

            // Si le nombre actuel dépasse la capacité, l'ajuster
            if (parseInt(nbConvivesInput.value) > parseInt(tableCapacity)) {
                nbConvivesInput.value = tableCapacity;
            }
        }

        this.updateConfirmButton();
    }

    /**
     * Met à jour l'état du bouton de confirmation
     */
    updateConfirmButton() {
        const confirmBtn = document.getElementById('confirm-reservation');
        if (confirmBtn) {
            const isFormValid = this.reservationState.selectedTableId &&
                this.isClientFormValid();
            confirmBtn.disabled = !isFormValid;
        }
    }

    /**
     * Vérifie si le formulaire client est valide
     */
    isClientFormValid() {
        const form = document.getElementById('reservation-form');
        if (!form) return false;

        const requiredFields = ['prenomClient', 'nomClient', 'telephone', 'nbConvives'];
        return requiredFields.every(field => {
            const input = form.querySelector(`[name="${field}"]`);
            return input && input.value.trim() !== '';
        });
    }

    /**
     * Gère la validation temps réel du formulaire
     */
    handleFormValidation(e) {
        if (e.target.closest('#reservation-form')) {
            setTimeout(() => this.updateConfirmButton(), 100);
        }
    }

    /**
     * Gère la soumission du formulaire de réservation
     */
    async handleFormSubmission(e) {
        if (!e.target.matches('#reservation-form')) return;

        e.preventDefault();

        // Prévenir les soumissions multiples
        const submitBtn = document.getElementById('confirm-reservation');
        if (submitBtn.disabled) {
            console.log('Soumission déjà en cours, ignorée');
            return;
        }

        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Réservation en cours...';

        const formData = new FormData(e.target);

        const reservation = {
            tableId: parseInt(formData.get('tableId')),
            creneauId: parseInt(formData.get('creneauId')),
            dateReservation: formData.get('dateReservation'),
            nomClient: formData.get('nomClient'),
            prenomClient: formData.get('prenomClient'),
            telephone: formData.get('telephone'),
            nbConvives: parseInt(formData.get('nbConvives'))
        };

        // Validation finale avec vérification capacité table
        if (!this.validateReservation(reservation)) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="bi bi-check-lg me-1"></i>Confirmer la réservation';
            return;
        }

        try {
            UIUtils.showLoading(true);

            const result = await this.api.post(NANCY_CONFIG.ENDPOINTS.RESERVER, reservation);

            if (result.success) {
                this.showReservationSuccess(result, reservation);
            } else {
                this.showReservationError(result.message || 'Erreur lors de la réservation');
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<i class="bi bi-check-lg me-1"></i>Confirmer la réservation';
            }
        } catch (error) {
            console.error('Erreur réservation:', error);
            this.showReservationError('Erreur de connexion au serveur');
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="bi bi-check-lg me-1"></i>Confirmer la réservation';
        } finally {
            UIUtils.showLoading(false);
        }
    }

    /**
     * Valide les données de réservation
     */
    validateReservation(reservation) {
        const errors = [];

        if (!reservation.tableId) errors.push('Table non sélectionnée');
        if (!reservation.creneauId) errors.push('Créneau non sélectionné');
        if (!reservation.dateReservation) errors.push('Date non sélectionnée');
        if (!reservation.nomClient?.trim()) errors.push('Nom requis');
        if (!reservation.prenomClient?.trim()) errors.push('Prénom requis');
        if (!reservation.telephone?.trim()) errors.push('Téléphone requis');
        if (!reservation.nbConvives || reservation.nbConvives < 1) errors.push('Nombre de convives invalide');

        // Validation du téléphone
        if (reservation.telephone && !/^[0-9]{10}$/.test(reservation.telephone.replace(/\s/g, ''))) {
            errors.push('Format de téléphone invalide (10 chiffres)');
        }

        if (errors.length > 0) {
            this.showValidationErrors(errors);
            return false;
        }

        return true;
    }

    /**
     * Affiche les erreurs de validation
     */
    showValidationErrors(errors) {
        if (!this.templates.reservationStatus) return;

        const alertHtml = this.templates.reservationStatus({
            type: 'danger',
            icon: 'bi-exclamation-triangle-fill',
            title: 'Données incomplètes',
            details: errors,
            showCloseButton: true
        });

        // Insertion de l'alerte dans le modal
        const modalBody = document.querySelector('#reservation-modal .modal-body');
        if (modalBody) {
            const existingAlert = modalBody.querySelector('.alert');
            if (existingAlert) existingAlert.remove();

            modalBody.insertAdjacentHTML('afterbegin', alertHtml);

            // Scroll vers le haut pour voir l'erreur
            modalBody.scrollTop = 0;
        }
    }

    /**
     * Affiche le succès de la réservation
     */
    showReservationSuccess(result) {
        this.hideReservationForm();

        // Création du modal de succès
        const creneau = this.creneaux.find(c => c.id === this.reservationState.selectedCreneauId);

        const summaryData = {
            reservationId: result.reservationId,
            restaurantName: this.reservationState.restaurantName,
            dateReservation: this.formatDate(this.reservationState.selectedDate),
            creneau: creneau,
            table: {
                numeroTable: result.reservation?.table?.numeroTable || 'N/A',
                nbPlaces: result.reservation?.table?.nbPlaces || 'N/A'
            },
            prenomClient: result.reservation?.prenomClient || '',
            nomClient: result.reservation?.nomClient || '',
            nbConvives: result.reservation?.nbConvives || 0
        };

        if (this.templates.reservationSummary) {
            const summaryHtml = this.templates.reservationSummary(summaryData);

            // Création d'un modal temporaire pour le succès
            const successModal = `
                <div class="modal fade show" style="display: block;" id="success-modal">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header border-0">
                                <button type="button" class="btn-close" onclick="this.closest('.modal').remove()"></button>
                            </div>
                            <div class="modal-body">
                                ${summaryHtml}
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-primary" onclick="this.closest('.modal').remove()">
                                    Fermer
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-backdrop fade show"></div>
            `;

            document.body.insertAdjacentHTML('beforeend', successModal);
        }

        UIUtils.showToast('Réservation confirmée avec succès!', 'success');
    }

    /**
     * Affiche les erreurs de réservation
     */
    showReservationError(message) {
        if (this.templates.reservationStatus) {
            const alertHtml = this.templates.reservationStatus({
                type: 'danger',
                icon: 'bi-x-circle-fill',
                title: 'Erreur de réservation',
                message: message,
                showCloseButton: true
            });

            const modalBody = document.querySelector('#reservation-modal .modal-body');
            if (modalBody) {
                const existingAlert = modalBody.querySelector('.alert');
                if (existingAlert) existingAlert.remove();

                modalBody.insertAdjacentHTML('afterbegin', alertHtml);
                modalBody.scrollTop = 0;
            }
        }

        UIUtils.showToast(message, 'danger');
    }

    /**
     * Affiche une étape spécifique du formulaire
     */
    showStep(stepId) {
        // Masquer toutes les étapes
        document.querySelectorAll('.step-container').forEach(step => {
            step.style.display = 'none';
        });

        // Afficher l'étape demandée
        const targetStep = document.getElementById(stepId);
        if (targetStep) {
            targetStep.style.display = 'block';
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

        // Réinitialisation de l'état
        this.reservationState = {
            restaurantId: null,
            restaurantName: null,
            selectedDate: null,
            selectedCreneauId: null,
            selectedTableId: null,
            currentStep: 'date'
        };
    }

    /**
     * Définit la date minimum pour les réservations (aujourd'hui)
     */
    setMinimumDate() {
        const dateInput = document.getElementById('dateReservation');
        if (dateInput) {
            const today = new Date();
            const todayStr = today.toISOString().split('T')[0];
            dateInput.min = todayStr;

            // Date par défaut: demain
            const tomorrow = new Date(today);
            tomorrow.setDate(today.getDate() + 1);
            dateInput.value = tomorrow.toISOString().split('T')[0];
        }
    }

    /**
     * Formate une date pour l'affichage
     */
    formatDate(dateString) {
        try {
            const date = new Date(dateString);
            return date.toLocaleDateString('fr-FR', {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            });
        } catch (error) {
            return dateString;
        }
    }

    /**
     * Méthode utilitaire pour déboguer l'état de la réservation
     */
    debugReservationState() {
        console.log('État de la réservation:', this.reservationState);
        console.log('Créneaux disponibles:', this.creneaux);
    }
}