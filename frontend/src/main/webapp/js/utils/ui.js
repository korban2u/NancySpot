/**
 * Utilitaires pour l'interface utilisateur
 * Gère les toasts, le loading et les statistiques avec templates Handlebars
 * @module utils/ui
 */
export class UIUtils {
    /**
     * Templates Handlebars compilés pour les éléments UI
     * @static
     */
    static templates = {
        stats: null
    };

    /**
     * Initialise les templates Handlebars pour les utilitaires UI
     * @static
     */
    static initTemplates() {
        const statsTemplate = document.getElementById('stats-template');
        if (statsTemplate) {
            this.templates.stats = Handlebars.compile(statsTemplate.innerHTML);
        }
    }

    /**
     * Affiche un toast de notification à l'utilisateur
     * @static
     * @param {string} message - Message à afficher dans le toast
     * @param {string} [type='info'] - Type de toast (success, danger, warning, info)
     */
    static showToast(message, type = 'info') {
        const toastElement = document.getElementById('toast');
        const icon = document.getElementById('toast-icon');
        const messageEl = document.getElementById('toast-message');

        if (!toastElement) return;

        // Configuration des styles selon le type de toast
        const config = {
            success: { bgClass: 'bg-success', textClass: 'text-white', icon: 'bi-check-circle-fill' },
            danger: { bgClass: 'bg-danger', textClass: 'text-white', icon: 'bi-x-circle-fill' },
            warning: { bgClass: 'bg-warning', textClass: 'text-dark', icon: 'bi-exclamation-triangle-fill' },
            info: { bgClass: 'bg-info', textClass: 'text-white', icon: 'bi-info-circle-fill' }
        }[type] || { bgClass: 'bg-info', textClass: 'text-white', icon: 'bi-info-circle-fill' };

        // Application des styles au toast
        const toastHeader = toastElement.querySelector('.toast-header');
        toastHeader.className = `toast-header ${config.bgClass} ${config.textClass}`;
        icon.className = `bi ${config.icon} me-2`;
        messageEl.textContent = message;

        // Affichage du toast avec Bootstrap
        const toast = new bootstrap.Toast(toastElement, { autohide: true, delay: 4000 });
        toast.show();
    }

    /**
     * Affiche ou masque l'indicateur de chargement
     * @static
     * @param {boolean} show - true pour afficher, false pour masquer
     */
    static showLoading(show) {
        const spinner = document.querySelector('.spinner-border')?.closest('.position-absolute');
        if (spinner) {
            spinner.style.display = show ? 'flex' : 'none';
        }
    }

    /**
     * Met à jour les statistiques affichées sur la carte en utilisant Handlebars
     * @static
     * @param {Array} restaurants - Liste des restaurants chargés
     * @param {Array} velib - Liste des stations Vélib chargées
     * @param {Array} incidents - Liste des incidents chargés
     */
    static updateStats(restaurants, velib, incidents) {
        const statsContainer = document.querySelector('.card-body.p-2');
        if (!statsContainer || !this.templates.stats) return;

        // Utilisation du template Handlebars au lieu de innerHTML manuel
        const statsHtml = this.templates.stats({
            restaurantsCount: restaurants.length,
            velibCount: velib.length,
            incidentsCount: incidents.length
        });

        statsContainer.innerHTML = statsHtml;
    }

    /**
     * Rend un template Handlebars dans un conteneur donné
     * @static
     * @param {string} containerId - ID du conteneur HTML
     * @param {Function} template - Template Handlebars compilé
     * @param {Object} data - Données à passer au template
     */
    static renderTemplate(containerId, template, data) {
        const container = document.getElementById(containerId);
        if (container && template) {
            container.innerHTML = template(data);
        }
    }

    /**
     * Compile et met en cache un template Handlebars
     * @static
     * @param {string} templateId - ID du script template dans le HTML
     * @returns {Function|null} Template compilé ou null si non trouvé
     */
    static compileTemplate(templateId) {
        const templateElement = document.getElementById(templateId);
        if (templateElement) {
            return Handlebars.compile(templateElement.innerHTML);
        }
        return null;
    }
}