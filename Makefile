.PHONY: help build start stop restart deploy clean
.PHONY: build-central build-bd build-proxy build-frontend
.PHONY: start-central stop-central start-bd stop-bd start-proxy stop-proxy
.PHONY: logs logs-central logs-bd logs-proxy status health
.PHONY: deploy-frontend

MAKEFLAGS += --no-print-directory

# Couleurs
GREEN=\033[0;32m
YELLOW=\033[1;33m
RED=\033[0;31m
BLUE=\033[0;34m
NC=\033[0m

# Variables d'environnement
ifneq (,$(wildcard ./.env))
    include .env
    export
endif

# Configuration
BUILD_DIR = build
LOGS_DIR = $(BUILD_DIR)/logs
PIDS_DIR = $(BUILD_DIR)/pids
CONFIG_DIR = $(BUILD_DIR)/config

# Ports par défaut
CENTRAL_HTTP_PORT ?= 8080
CENTRAL_HTTPS_PORT ?= 8443
CENTRAL_RMI_PORT ?= 1098
CENTRAL_HOST ?= localhost
CENTRAL_HTTPS_ENABLED ?= false

# Webetu
WEBETU_USER ?= korban2u
WEBETU_HOST = webetu.iutnc.univ-lorraine.fr
WEBETU_PROJECT_DIR = NancySpot

# JARs
CENTRAL_JAR = service-central/target/service-central-1.0-SNAPSHOT.jar
BD_JAR = service-bd/target/service-bd-1.0-SNAPSHOT.jar
PROXY_JAR = service-proxy/target/service-proxy-1.0-SNAPSHOT.jar
COMMON_JAR = common/target/common-1.0-SNAPSHOT.jar

help: ## Afficher l'aide
	@echo "$(GREEN)=== Nancy Spot - Commandes ===$(NC)"
	@echo ""
	@echo "$(YELLOW)Compilation:$(NC)"
	@echo "  $(GREEN)build$(NC)            Compiler tout"
	@echo "  $(GREEN)build-central$(NC)    Compiler service-central uniquement"
	@echo "  $(GREEN)build-bd$(NC)         Compiler service-bd uniquement"
	@echo "  $(GREEN)build-proxy$(NC)      Compiler service-proxy uniquement"
	@echo "  $(GREEN)build-frontend$(NC)   Compiler frontend uniquement"
	@echo ""
	@echo "$(YELLOW)Principales:$(NC)"
	@echo "  $(GREEN)deploy$(NC)           Compiler + démarrer services"
	@echo "  $(GREEN)start$(NC)            Démarrer les services backend"
	@echo "  $(GREEN)stop$(NC)             Arrêter les services"
	@echo "  $(GREEN)status$(NC)           Statut des services"
	@echo "  $(GREEN)logs$(NC)             Voir les logs"
	@echo "  $(GREEN)health$(NC)           Test de santé"
	@echo "  $(GREEN)clean$(NC)            Nettoyer tout"
	@echo ""
	@echo "$(YELLOW)Frontend:$(NC)"
	@echo "  $(GREEN)deploy-frontend$(NC)  Déployer sur webetu"
	@echo ""
	@echo "$(YELLOW)Services individuels:$(NC)"
	@echo "  $(GREEN)start-central$(NC), $(GREEN)stop-central$(NC), $(GREEN)logs-central$(NC)"
	@echo "  $(GREEN)start-bd$(NC), $(GREEN)stop-bd$(NC), $(GREEN)logs-bd$(NC)"
	@echo "  $(GREEN)start-proxy$(NC), $(GREEN)stop-proxy$(NC), $(GREEN)logs-proxy$(NC)"

build: ## Compiler tous les modules
	@echo "$(GREEN)=== Compilation complète ===$(NC)"
	@mvn clean package dependency:copy-dependencies -DskipTests
	@mkdir -p $(BUILD_DIR) $(LOGS_DIR) $(PIDS_DIR) $(CONFIG_DIR)
	@$(MAKE) -s generate-configs
	@echo "$(GREEN)✓ Compilation terminée$(NC)"

build-central: ## Compiler le service central
	@echo "$(GREEN)=== Compilation service-central ===$(NC)"
	@cd common && mvn clean package -DskipTests
	@cd service-central && mvn clean package dependency:copy-dependencies -DskipTests
	@mkdir -p $(BUILD_DIR) $(LOGS_DIR) $(PIDS_DIR) $(CONFIG_DIR)
	@$(MAKE) -s generate-config-central
	@echo "$(GREEN)✓ Service Central compilé$(NC)"

build-bd: ## Compiler le service BD
	@echo "$(GREEN)=== Compilation service-bd ===$(NC)"
	@cd common && mvn clean package -DskipTests
	@cd service-bd && mvn clean package dependency:copy-dependencies -DskipTests
	@mkdir -p $(BUILD_DIR) $(LOGS_DIR) $(PIDS_DIR) $(CONFIG_DIR)
	@$(MAKE) -s generate-config-bd
	@echo "$(GREEN)✓ Service BD compilé$(NC)"

build-proxy: ## Compiler le service proxy
	@echo "$(GREEN)=== Compilation service-proxy ===$(NC)"
	@cd common && mvn clean package -DskipTests
	@cd service-proxy && mvn clean package dependency:copy-dependencies -DskipTests
	@mkdir -p $(BUILD_DIR) $(LOGS_DIR) $(PIDS_DIR) $(CONFIG_DIR)
	@$(MAKE) -s generate-config-proxy
	@echo "$(GREEN)✓ Service Proxy compilé$(NC)"

build-frontend: ## Compiler le frontend
	@echo "$(GREEN)=== Compilation Frontend ===$(NC)"
	@cd frontend && mvn clean package -DskipTests
	@echo "$(GREEN)✓ Frontend compilé$(NC)"

generate-configs: generate-config-central generate-config-bd generate-config-proxy

generate-config-central:
	@echo "central.rmi.port=$(CENTRAL_RMI_PORT)" > $(CONFIG_DIR)/central.properties
	@echo "central.host=$(CENTRAL_HOST)" >> $(CONFIG_DIR)/central.properties
	@echo "central.http.port=$(CENTRAL_HTTP_PORT)" >> $(CONFIG_DIR)/central.properties
	@echo "central.https.enabled=$(CENTRAL_HTTPS_ENABLED)" >> $(CONFIG_DIR)/central.properties
	@echo "central.https.port=$(CENTRAL_HTTPS_PORT)" >> $(CONFIG_DIR)/central.properties
	@echo "central.keystore.path=$(CENTRAL_KEYSTORE_PATH)" >> $(CONFIG_DIR)/central.properties
	@echo "central.keystore.password=$(CENTRAL_KEYSTORE_PASSWORD)" >> $(CONFIG_DIR)/central.properties

generate-config-bd:
	@echo "bd.jdbc.url=$(BD_JDBC_URL)" > $(CONFIG_DIR)/bd.properties
	@echo "bd.jdbc.user=$(BD_JDBC_USER)" >> $(CONFIG_DIR)/bd.properties
	@echo "bd.jdbc.password=$(BD_JDBC_PASSWORD)" >> $(CONFIG_DIR)/bd.properties
	@echo "central.host=$(CENTRAL_HOST)" >> $(CONFIG_DIR)/bd.properties
	@echo "central.rmi.port=$(CENTRAL_RMI_PORT)" >> $(CONFIG_DIR)/bd.properties

generate-config-proxy:
	@echo "proxy.use.iut.proxy=$(PROXY_USE_IUT_PROXY)" > $(CONFIG_DIR)/proxy.properties
	@echo "proxy.iut.host=$(PROXY_IUT_HOST)" >> $(CONFIG_DIR)/proxy.properties
	@echo "proxy.iut.port=$(PROXY_IUT_PORT)" >> $(CONFIG_DIR)/proxy.properties
	@echo "central.host=$(CENTRAL_HOST)" >> $(CONFIG_DIR)/proxy.properties
	@echo "central.rmi.port=$(CENTRAL_RMI_PORT)" >> $(CONFIG_DIR)/proxy.properties

deploy: build start ## Déploiement complet des services backend
	@echo "$(GREEN)✓ Services backend déployés$(NC)"
	@echo "$(BLUE)API: http://$(CENTRAL_HOST):$(CENTRAL_HTTP_PORT)$(NC)"
	@echo "$(YELLOW)Pour le frontend: make deploy-frontend$(NC)"

start: start-central start-bd start-proxy ## Démarrer tous les services
	@echo "$(GREEN)✓ Tous les services démarrés$(NC)"

stop: stop-proxy stop-bd stop-central ## Arrêter tous les services
	@echo "$(GREEN)✓ Tous les services arrêtés$(NC)"

# Services individuels
start-central: ## Démarrer service central
	@if [ -f $(PIDS_DIR)/central.pid ] && kill -0 $$(cat $(PIDS_DIR)/central.pid) 2>/dev/null; then \
		echo "$(YELLOW)Service Central déjà démarré$(NC)"; exit 0; \
	fi
	@echo "$(YELLOW)Démarrage Service Central...$(NC)"
	@nohup java -Xmx1g -Djava.rmi.server.hostname=$(CENTRAL_HOST) \
		-cp "$(CENTRAL_JAR):service-central/target/dependency/*:$(COMMON_JAR)" \
		Main $(CONFIG_DIR)/central.properties > $(LOGS_DIR)/central.log 2>&1 & \
	echo $$! > $(PIDS_DIR)/central.pid
	@sleep 2
	@echo "$(GREEN)✓ Service Central démarré$(NC)"

stop-central: ## Arrêter service central
	@if [ -f $(PIDS_DIR)/central.pid ]; then \
		kill $$(cat $(PIDS_DIR)/central.pid) 2>/dev/null || true; \
		rm -f $(PIDS_DIR)/central.pid; \
		echo "$(GREEN)✓ Service Central arrêté$(NC)"; \
	fi

start-bd: ## Démarrer service BD
	@if [ -f $(PIDS_DIR)/bd.pid ] && kill -0 $$(cat $(PIDS_DIR)/bd.pid) 2>/dev/null; then \
		echo "$(YELLOW)Service BD déjà démarré$(NC)"; exit 0; \
	fi
	@echo "$(YELLOW)Démarrage Service BD...$(NC)"
	@nohup java -Xmx512m -Djava.rmi.server.hostname=$(CENTRAL_HOST) \
		-cp "$(BD_JAR):service-bd/target/dependency/*:$(COMMON_JAR)" \
		Main $(CONFIG_DIR)/bd.properties > $(LOGS_DIR)/bd.log 2>&1 & \
	echo $$! > $(PIDS_DIR)/bd.pid
	@sleep 1
	@echo "$(GREEN)✓ Service BD démarré$(NC)"

stop-bd: ## Arrêter service BD
	@if [ -f $(PIDS_DIR)/bd.pid ]; then \
		kill $$(cat $(PIDS_DIR)/bd.pid) 2>/dev/null || true; \
		rm -f $(PIDS_DIR)/bd.pid; \
		echo "$(GREEN)✓ Service BD arrêté$(NC)"; \
	fi

start-proxy: ## Démarrer service proxy
	@if [ -f $(PIDS_DIR)/proxy.pid ] && kill -0 $$(cat $(PIDS_DIR)/proxy.pid) 2>/dev/null; then \
		echo "$(YELLOW)Service Proxy déjà démarré$(NC)"; exit 0; \
	fi
	@echo "$(YELLOW)Démarrage Service Proxy...$(NC)"
	@nohup java -Xmx512m -Djava.rmi.server.hostname=$(CENTRAL_HOST) \
		-cp "$(PROXY_JAR):service-proxy/target/dependency/*:$(COMMON_JAR)" \
		Main $(CONFIG_DIR)/proxy.properties > $(LOGS_DIR)/proxy.log 2>&1 & \
	echo $$! > $(PIDS_DIR)/proxy.pid
	@sleep 1
	@echo "$(GREEN)✓ Service Proxy démarré$(NC)"

stop-proxy: ## Arrêter service proxy
	@if [ -f $(PIDS_DIR)/proxy.pid ]; then \
		kill $$(cat $(PIDS_DIR)/proxy.pid) 2>/dev/null || true; \
		rm -f $(PIDS_DIR)/proxy.pid; \
		echo "$(GREEN)✓ Service Proxy arrêté$(NC)"; \
	fi

# Logs
logs: ## Voir tous les logs (dernières lignes)
	@echo "$(GREEN)=== Logs récents ===$(NC)"
	@for service in central bd proxy; do \
		if [ -f $(LOGS_DIR)/$$service.log ]; then \
			echo "$(BLUE)--- $$service ---$(NC)"; \
			tail -3 $(LOGS_DIR)/$$service.log; \
			echo ""; \
		fi; \
	done

logs-central: ## Suivre logs central en temps réel
	@tail -f $(LOGS_DIR)/central.log 2>/dev/null || echo "$(RED)Logs central non trouvés$(NC)"

logs-bd: ## Suivre logs BD en temps réel
	@tail -f $(LOGS_DIR)/bd.log 2>/dev/null || echo "$(RED)Logs BD non trouvés$(NC)"

logs-proxy: ## Suivre logs proxy en temps réel
	@tail -f $(LOGS_DIR)/proxy.log 2>/dev/null || echo "$(RED)Logs proxy non trouvés$(NC)"

# Statut et santé
status: ## Statut des services
	@echo "$(GREEN)=== Statut des services ===$(NC)"
	@printf "%-10s %-8s %-6s\n" "SERVICE" "STATUT" "PID"
	@printf "%-10s %-8s %-6s\n" "-------" "------" "---"
	@for service in central bd proxy; do \
		if [ -f $(PIDS_DIR)/$$service.pid ]; then \
			PID=$$(cat $(PIDS_DIR)/$$service.pid 2>/dev/null || echo ""); \
			if [ -n "$$PID" ] && kill -0 $$PID 2>/dev/null; then \
				printf "%-10s $(GREEN)%-8s$(NC) %-6s\n" "$$service" "ACTIF" "$$PID"; \
			else \
				printf "%-10s $(RED)%-8s$(NC) %-6s\n" "$$service" "MORT" "$$PID"; \
			fi; \
		else \
			printf "%-10s $(RED)%-8s$(NC) %-6s\n" "$$service" "ARRÊTÉ" "-"; \
		fi; \
	done
	@echo ""
	@echo "$(BLUE)API: http://$(CENTRAL_HOST):$(CENTRAL_HTTP_PORT)$(NC)"

health: ## Test de santé des APIs
	@echo "$(YELLOW)Test de santé...$(NC)"
	@API_URL="http://$(CENTRAL_HOST):$(CENTRAL_HTTP_PORT)"; \
	for endpoint in services/etat restaurants velib incidents; do \
		printf "%-15s " "$$endpoint:"; \
		if curl -s --connect-timeout 3 "$$API_URL/$$endpoint" >/dev/null 2>&1; then \
			echo "$(GREEN)✓$(NC)"; \
		else \
			echo "$(RED)✗$(NC)"; \
		fi; \
	done

deploy-frontend: build-frontend ## Déployer le frontend sur webetu
	@echo "$(GREEN)=== Déploiement Frontend sur Webetu ===$(NC)"
	@if [ -z "$(WEBETU_USER)" ]; then \
		echo "$(RED)Erreur: WEBETU_USER non défini dans .env$(NC)"; \
		exit 1; \
	fi

	@echo "$(YELLOW)Création archive...$(NC)"
	@cd frontend/target && \
	tar --exclude="*.class" --exclude="*/WEB-INF/classes/*" \
		-czf nancy-frontend.tar.gz nancy-frontend/

	@echo "$(YELLOW)Upload sur webetu...$(NC)"
	@scp frontend/target/nancy-frontend.tar.gz $(WEBETU_USER)@$(WEBETU_HOST):

	@echo "$(YELLOW)Déploiement...$(NC)"
	@ssh $(WEBETU_USER)@$(WEBETU_HOST) '\
		rm -rf www/$(WEBETU_PROJECT_DIR) 2>/dev/null || true; \
		mkdir -p www; \
		cd www; \
		tar -xzf ~/nancy-frontend.tar.gz; \
		mv nancy-frontend $(WEBETU_PROJECT_DIR); \
		rm ~/nancy-frontend.tar.gz'

	@echo "$(GREEN)✓ Frontend déployé$(NC)"
	@echo "$(BLUE)URL: https://webetu.iutnc.univ-lorraine.fr/~$(WEBETU_USER)/$(WEBETU_PROJECT_DIR)/$(NC)"

clean: stop ## Nettoyer tout
	@echo "$(YELLOW)Nettoyage...$(NC)"
	@rm -rf $(BUILD_DIR)
	@mvn clean -q
	@echo "$(GREEN)✓ Nettoyage terminé$(NC)"