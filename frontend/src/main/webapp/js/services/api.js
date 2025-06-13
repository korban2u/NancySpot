import { NANCY_CONFIG } from '../config/constants.js';

/**
 * Service pour les appels API vers le backend
 * Gère les requêtes HTTP avec gestion d'erreurs
 * @module services/api
 */

export class ApiService {
    /**
     * Crée une instance du service API
     * @constructor
     */
    constructor() {
        this.baseUrl = NANCY_CONFIG.API_BASE_URL;
    }

    /**
     * Effectue une requête HTTP générique
     * @param {string} endpoint - Point d'entrée de l'API (ex: '/restaurants')
     * @param {Object} [options={}] - Options de la requête fetch
     * @param {string} [options.method] - Méthode HTTP (GET, POST, etc.)
     * @param {Object} [options.headers] - En-têtes HTTP additionnels
     * @param {string} [options.body] - Corps de la requête pour POST
     * @returns {Promise<Object>} Données JSON de la réponse
     * @throws {Error} En cas d'erreur HTTP ou de réponse invalide
     */
    async request(endpoint, options = {}) {
        try {
            const response = await fetch(`${this.baseUrl}${endpoint}`, {
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                    ...options.headers
                },
                ...options
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const data = await response.json();

            // Vérification si l'API renvoie une erreur dans les données
            if (data.error) {
                throw new Error(data.message || 'Erreur serveur');
            }

            return data;
        } catch (error) {
            console.error(`Erreur API ${endpoint}:`, error);
            throw error;
        }
    }

    /**
     * Effectue une requête GET
     * @param {string} endpoint - Point d'entrée de l'API
     * @returns {Promise<Object>} Données JSON de la réponse
     * @throws {Error} En cas d'erreur de requête
     */
    async get(endpoint) {
        return this.request(endpoint);
    }

    /**
     * Effectue une requête POST avec des données JSON
     * @param {string} endpoint - Point d'entrée de l'API
     * @param {Object} data - Données à envoyer dans le corps de la requête
     * @returns {Promise<Object>} Données JSON de la réponse
     * @throws {Error} En cas d'erreur de requête
     */
    async post(endpoint, data) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }
}