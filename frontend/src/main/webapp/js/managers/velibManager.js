import { NANCY_CONFIG } from '../config/constants.js';
import { UIUtils } from '../utils/ui.js';

/**
 * Gestionnaire des stations Vélib - Version API directe
 * Consomme directement l'API Cyclocity GBFS sans passer par le backend
 */
export class VelibManager {
    constructor(mapManager) {
        this.map = mapManager;
        this.stations = [];
        this.template = null;
        this.initTemplate();
    }

    initTemplate() {
        const template = document.getElementById('velib-popup-template');
        if (template) {
            this.template = Handlebars.compile(template.innerHTML);
        }
    }

    /**
     * Charge les stations Vélib directement depuis l'API Cyclocity
     */
    async loadStations() {
        try {
            console.log('Chargement des données Vélib depuis l\'API Cyclocity...');

            // Utilisation des URLs depuis les constantes
            const [stationInfoResponse, stationStatusResponse] = await Promise.all([
                fetch(NANCY_CONFIG.EXTERNAL_APIS.VELIB.STATION_INFO),
                fetch(NANCY_CONFIG.EXTERNAL_APIS.VELIB.STATION_STATUS)
            ]);

            if (!stationInfoResponse.ok || !stationStatusResponse.ok) {
                throw new Error('Erreur lors de la récupération des données Vélib');
            }

            const stationInfoData = await stationInfoResponse.json();
            const stationStatusData = await stationStatusResponse.json();

            // Traitement des données
            this.stations = this.processStationsData(
                stationInfoData.data.stations,
                stationStatusData.data.stations
            );

            console.log(`${this.stations.length} stations Vélib chargées`);
            return this.stations;

        } catch (error) {
            console.error('Erreur chargement Vélib:', error);
            UIUtils.showToast('Erreur lors du chargement des stations Vélib', 'warning');
            return [];
        }
    }

    /**
     * Traite et combine les données des stations
     */
    processStationsData(stationsInfo, stationsStatus) {
        // Créer un index des statuts par station_id
        const statusMap = new Map();
        stationsStatus.forEach(status => {
            statusMap.set(status.station_id, status);
        });

        // Combiner les informations et statuts
        return stationsInfo.map(station => {
            const status = statusMap.get(station.station_id);

            return {
                id: station.station_id,
                nom: station.name,
                latitude: station.lat,
                longitude: station.lon,
                velosDisponibles: status ? status.num_bikes_available : 0,
                placesDisponibles: status ? status.num_docks_available : 0,
                estOuverte: status ? status.is_installed && status.is_renting : false,
                capacite: station.capacity || 0
            };
        }).filter(station => station.estOuverte); // Ne garder que les stations ouvertes
    }

    /**
     * Affiche les stations sur la carte
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

    /**
     * Méthode utilitaire pour vérifier la disponibilité de l'API
     */
    async checkApiHealth() {
        try {
            const response = await fetch(NANCY_CONFIG.EXTERNAL_APIS.VELIB.DISCOVERY);
            return response.ok;
        } catch (error) {
            return false;
        }
    }
}