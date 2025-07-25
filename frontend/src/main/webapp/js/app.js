import { NANCY_CONFIG } from './config/constants.js';
import { ApiService } from './services/api.js';
import { UIUtils } from './utils/ui.js';
import { MapManager } from './managers/mapManager.js';
import { RestaurantManager } from './managers/restaurantManager.js';
import { VelibManager } from './managers/velibManager.js';
import { IncidentManager } from './managers/incidentManager.js';

// Enregistrement des helpers Handlebars
Handlebars.registerHelper('eq', (a, b) => a === b);

/**
 * Application principale Nancy Spot
 * Point d'entrée de l'application, coordonne tous les gestionnaires avec Handlebars
 * @module app
 */
class NancyApp {
    /**
     * Crée une instance de l'application Nancy Spot
     * @constructor
     */
    constructor() {
        this.api = new ApiService();
        this.map = new MapManager();
        this.restaurantManager = new RestaurantManager(this.api, this.map);

        this.velibManager = new VelibManager(this.map);

        this.incidentManager = new IncidentManager(this.api, this.map);

        this.state = {
            activeTab: 'carte',
            filters: {
                restaurants: true,
                velib: true,
                incidents: true
            }
        };

        this.templates = {
            layout: null,
            carte: null,
            compteRendu: null,
            compteRenduEnrichi: null
        };
    }

    /**
     * Initialise l'application
     * Compile les templates, génère l'interface, initialise la carte et charge les données
     * @async
     */
    async init() {
        console.log('Initialisation Nancy Spot...');

        // Initialisation des templates Handlebars
        this.initTemplates();
        UIUtils.initTemplates();

        this.renderLayout();

        if (this.state.activeTab === 'carte') {
            await this.initMap();
        }

        this.setupEvents();
        await this.loadAllData();

        console.log('Application initialisée');
    }

    /**
     * Initialise tous les templates Handlebars principaux
     */
    initTemplates() {
        // Compilation des templates principaux
        this.templates.layout = UIUtils.compileTemplate('layout-template');
        this.templates.carte = UIUtils.compileTemplate('carte-template');
        this.templates.compteRendu = UIUtils.compileTemplate('compte-rendu-template');
        this.templates.compteRenduEnrichi = UIUtils.compileTemplate('compte-rendu-enrichi-template');
    }

    /**
     * Génère l'interface utilisateur avec les templates Handlebars
     */
    renderLayout() {
        if (!this.templates.layout || !this.templates.carte || !this.templates.compteRendu) {
            console.error('Templates manquants pour le rendu');
            return;
        }

        // Génération du contenu avec les templates
        const carteContent = this.templates.carte({ loading: false });
        const compteRenduContent = this.templates.compteRendu({});

        // Rendu principal avec Handlebars
        const layoutHtml = this.templates.layout({
            activeTab: this.state.activeTab,
            filters: this.state.filters,
            carteContent: carteContent,
            compteRenduContent: compteRenduContent
        });

        // Injection dans le DOM
        const appContainer = document.getElementById('app');
        if (appContainer) {
            appContainer.innerHTML = layoutHtml;
        }

        this.updateContentVisibility();
    }

    /**
     * Initialise la carte avec un délai pour s'assurer que le DOM est prêt
     * @async
     */
    async initMap() {
        await new Promise(resolve => setTimeout(resolve, 100));
        this.map.init();
    }

    /**
     * Configure tous les événements de l'interface utilisateur
     */
    setupEvents() {
        // Navigation entre onglets
        document.addEventListener('click', (e) => {
            if (e.target.closest('[data-tab]')) {
                const tab = e.target.closest('[data-tab]').dataset.tab;
                this.switchTab(tab);
            }

            if (e.target.closest('#refresh-button')) {
                this.loadAllData();
            }

            if (e.target.closest('.reserve-btn')) {
                const btn = e.target.closest('.reserve-btn');
                this.restaurantManager.showReservationForm(
                    btn.dataset.restaurantId,
                    btn.dataset.restaurantName
                );
            }
        });

        // Filtres de la carte
        document.addEventListener('change', (e) => {
            if (e.target.matches('[data-type]')) {
                const type = e.target.dataset.type;
                this.toggleFilter(type);
            }
        });
    }

    /**
     * Change l'onglet actif et met à jour l'interface
     * @param {string} tab - Nom de l'onglet ('carte' ou 'compte-rendu')
     */
    switchTab(tab) {
        this.state.activeTab = tab;

        document.querySelectorAll('[data-tab]').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.tab === tab);
        });

        const mapControls = document.getElementById('map-controls');
        if (mapControls) {
            mapControls.classList.toggle('d-none', tab !== 'carte');
        }

        this.updateContentVisibility();

        // Si on passe au compte-rendu, on peut l'enrichir avec les données actuelles
        if (tab === 'compte-rendu') {
            this.updateCompteRendu();
        }

        if (tab === 'carte' && this.map.map) {
            this.map.invalidateSize();
        }
    }

    /**
     * Met à jour le compte-rendu avec les données actuelles via Handlebars
     */
    updateCompteRendu() {
        if (!this.templates.compteRenduEnrichi) return;

        const compteRenduContainer = document.querySelector('.container.my-4');
        if (compteRenduContainer) {
            const compteRenduHtml = this.templates.compteRenduEnrichi({
                stats: {
                    restaurants: this.restaurantManager.restaurants.length,
                    velib: this.velibManager.stations.length,
                    incidents: this.incidentManager.incidents.length
                }
            });
            compteRenduContainer.innerHTML = compteRenduHtml;
        }
    }

    /**
     * Met à jour la visibilité des contenus selon l'onglet actif
     */
    updateContentVisibility() {
        // Utilisation des conteneurs spécifiques définis dans le template
        const carteContainer = document.getElementById('carte-container');
        const compteRenduContainer = document.getElementById('compte-rendu-container');

        console.log('Changement onglet vers:', this.state.activeTab);

        if (this.state.activeTab === 'carte') {
            // Afficher la carte, masquer le compte-rendu
            if (carteContainer) {
                carteContainer.classList.remove('d-none');
                console.log('Carte affichée');
            }
            if (compteRenduContainer) {
                compteRenduContainer.classList.add('d-none');
                console.log('Compte-rendu masqué');
            }
        } else if (this.state.activeTab === 'compte-rendu') {
            // Masquer la carte, afficher le compte-rendu
            if (carteContainer) {
                carteContainer.classList.add('d-none');
                console.log('Carte masquée');
            }
            if (compteRenduContainer) {
                compteRenduContainer.classList.remove('d-none');
                console.log('Compte-rendu affiché');
            }
        }
    }

    /**
     * Active/désactive un filtre d'affichage sur la carte
     * @param {string} type - Type de filtre (restaurants, velib, incidents)
     */
    toggleFilter(type) {
        this.state.filters[type] = !this.state.filters[type];

        if (this.state.filters[type]) {
            this.displayDataOnMap(type);
        } else {
            this.map.clearMarkers(type);
        }
    }

    /**
     * Charge toutes les données depuis les APIs
     * Vérifie l'état des services avant de charger les données
     * @async
     */
    async loadAllData() {
        UIUtils.showLoading(true);

        try {
            // Vérifier le status en arrière-plan
            const statusPromise = this.api.get(NANCY_CONFIG.ENDPOINTS.STATUS)
                .catch(err => {
                    console.warn('Services backend non disponibles:', err);
                    return { serviceBD: { disponible: false }, serviceProxy: { disponible: false } };
                });

            // 1. Charger et afficher Vélib immédiatement (indépendant du backend)
            this.velibManager.loadStations()
                .then(stations => {
                    if (this.state.filters.velib && stations.length > 0) {
                        this.velibManager.displayOnMap();
                        this.updateStats();
                    }
                })
                .catch(err => {
                    console.error('Erreur chargement Vélib:', err);
                    UIUtils.showToast('Impossible de charger les données Vélib', 'warning');
                });

            // 2. Attendre le status puis charger le reste
            const status = await statusPromise;
            console.log('État des services:', status);

            // 3. Charger restaurants si BD disponible
            if (status.serviceBD?.disponible) {
                this.restaurantManager.loadRestaurants()
                    .then(restaurants => {
                        // Charger aussi les créneaux
                        return this.restaurantManager.loadCreneaux().then(() => restaurants);
                    })
                    .then(restaurants => {
                        if (this.state.filters.restaurants && restaurants.length > 0) {
                            this.restaurantManager.displayOnMap();
                            this.updateStats();
                        }
                    })
                    .catch(err => {
                        console.error('Erreur chargement restaurants:', err);
                    });
            }

            // 4. Charger incidents si Proxy disponible
            if (status.serviceProxy?.disponible) {
                this.incidentManager.loadIncidents()
                    .then(incidents => {
                        if (this.state.filters.incidents && incidents.length > 0) {
                            this.incidentManager.displayOnMap();
                            this.updateStats();
                        }
                    })
                    .catch(err => {
                        console.error('Erreur chargement incidents:', err);
                    });
            }

            // Message final
            setTimeout(() => {
                if (!status.serviceBD?.disponible && !status.serviceProxy?.disponible) {
                    UIUtils.showToast('Données Vélib chargées (services backend indisponibles)', 'info');
                } else {
                    UIUtils.showToast('Données chargées avec succès', 'success');
                }
                UIUtils.showLoading(false);
            }, 1000);

        } catch (error) {
            console.error('Erreur critique lors du chargement:', error);
            UIUtils.showToast('Erreur critique lors du chargement', 'danger');
            UIUtils.showLoading(false);
        }
    }

// Méthode helper pour mettre à jour les stats
    updateStats() {
        UIUtils.updateStats(
            this.restaurantManager.restaurants,
            this.velibManager.stations,
            this.incidentManager.incidents
        );
    }

    /**
     * Affiche un type de données spécifique sur la carte
     * @param {string} type - Type de données à afficher (restaurants, velib, incidents)
     */
    displayDataOnMap(type) {
        switch (type) {
            case 'restaurants':
                if (this.state.filters.restaurants) {
                    this.restaurantManager.displayOnMap();
                }
                break;
            case 'velib':
                if (this.state.filters.velib) {
                    this.velibManager.displayOnMap();
                }
                break;
            case 'incidents':
                if (this.state.filters.incidents) {
                    this.incidentManager.displayOnMap();
                }
                break;
        }
    }

    /**
     * Affiche tous les types de données activés sur la carte
     */
    displayAllDataOnMap() {
        ['restaurants', 'velib', 'incidents'].forEach(type => {
            this.displayDataOnMap(type);
        });
    }
}

// point d'entrée de l'application
document.addEventListener('DOMContentLoaded', () => {
    const app = new NancyApp();
    app.init();
});