# Makefile avec support des profils Maven pour Nancy Spot
.PHONY: help build start stop restart clean logs status
.PHONY: start-central stop-central start-bd stop-bd start-proxy stop-proxy
.PHONY: logs-central logs-bd logs-proxy generate-cert
.PHONY: build-dev build-prod build-iut build-docker
.PHONY: deploy-webetu deploy-webetu-iut deploy-webetu-clean clean-webetu test-webetu logs-webetu

# Variables
ifneq (,$(wildcard ./.env))
    include .env
    export
endif

# Configuration par défaut
CENTRAL_HOST ?= localhost
CENTRAL_HTTP_PORT ?= 8080
CENTRAL_HTTPS_PORT ?= 8443
CENTRAL_HTTPS_ENABLED ?= false
CENTRAL_RMI_PORT ?= 1098
CENTRAL_KEYSTORE_PATH ?= nancy-keystore.jks
SSL_KEYSTORE_PASSWORD ?= nancy2024

# Profil Maven par défaut
MAVEN_PROFILE ?= dev

BUILD_DIR = build
LOGS_DIR = $(BUILD_DIR)/logs
PIDS_DIR = $(BUILD_DIR)/pids
CONFIG_DIR = $(BUILD_DIR)/config

# Déploiement sur webetu
WEBETU_USER ?= $(if $(strip $(WEBETU_USER)),$(WEBETU_USER),$(USER))
WEBETU_HOST = webetu.iutnc.univ-lorraine.fr
WEBETU_PATH = www/NancySpot
FRONTEND_BUILD_DIR = frontend/target/nancy-frontend

help: ## Afficher l'aide
	@echo "Commandes disponibles :"
	@echo ""
	@echo "Build avec profils Maven :"
	@echo "  build-dev      - Build pour développement local (http://localhost:8080)"
	@echo "  build-prod     - Build pour production (variables d'environnement)"
	@echo "  build-iut      - Build pour machines IUT (https://172.22.152.208:8443)"
	@echo "  build-docker   - Build pour Docker (https://service-central:8443)"
	@echo "  build          - Build avec profil par défaut ($(MAVEN_PROFILE))"
	@echo ""
	@echo "Services :"
	@echo "  start          - Démarrer tous les services"
	@echo "  stop           - Arrêter tous les services"
	@echo "  restart        - Redémarrer tous les services"
	@echo "  status         - Voir le statut des services"
	@echo "  logs           - Voir les logs récents"
	@echo "  clean          - Nettoyer"
	@echo "  generate-cert  - Générer certificat HTTPS"
	@echo ""
	@echo "Déploiement webetu :"
	@echo "  deploy-webetu         - Déployer frontend sur webetu"
	@echo "  deploy-webetu-iut     - Build IUT + déployer sur webetu"
	@echo "  deploy-webetu-clean   - Nettoyer + déployer sur webetu"
	@echo "  clean-webetu          - Nettoyer le frontend sur webetu"
	@echo "  test-webetu           - Tester le frontend déployé"
	@echo "  logs-webetu           - Voir les fichiers sur webetu"
	@echo ""
	@echo "Services individuels :"
	@echo "  start-central  - Démarrer service central"
	@echo "  start-bd       - Démarrer service BD"
	@echo "  start-proxy    - Démarrer service proxy"
	@echo ""
	@echo "Logs individuels :"
	@echo "  logs-central   - Suivre logs service central"
	@echo "  logs-bd        - Suivre logs service BD"
	@echo "  logs-proxy     - Suivre logs service proxy"
	@echo ""
	@echo "Variables d'environnement :"
	@echo "  MAVEN_PROFILE=$(MAVEN_PROFILE)"
	@echo "  CENTRAL_HOST=$(CENTRAL_HOST)"
	@echo "  CENTRAL_HTTPS_ENABLED=$(CENTRAL_HTTPS_ENABLED)"
	@echo "  WEBETU_USER=$(WEBETU_USER) (configuré dans .env)"

# Builds spécialisés avec profils Maven
build-dev: ## Build pour développement local
	@echo "Build pour développement local (HTTP)..."
	@mvn clean package -Pdev -DskipTests
	@$(MAKE) -s post-build
	@echo "✓ Build dev terminé - Frontend configuré pour http://localhost:8080"

build-dev-https: ## Build pour développement local avec HTTPS
	@echo "Build pour développement local (HTTPS)..."
	@mvn clean package -Pdev-https -DskipTests
	@$(MAKE) -s post-build
	@echo "✓ Build dev-https terminé - Frontend configuré pour https://localhost:8443"

build-prod: ## Build pour production
	@echo "Build pour production..."
	@if [ -z "$(CENTRAL_HOST)" ]; then \
		echo "ERREUR: Variable CENTRAL_HOST non définie"; \
		exit 1; \
	fi
	@mvn clean package -Pprod -DskipTests
	@$(MAKE) -s post-build
	@echo "✓ Build prod terminé - Frontend configuré pour $(CENTRAL_HOST)"

build-iut: ## Build pour machines IUT
	@echo "Build pour machines IUT..."
	@mvn clean package -Piut -DskipTests
	@$(MAKE) -s post-build
	@echo "✓ Build IUT terminé - Frontend configuré pour https://172.22.152.208:8443"

build-docker: ## Build pour Docker
	@echo "Build pour Docker..."
	@mvn clean package -Pdocker -DskipTests
	@$(MAKE) -s post-build
	@echo "✓ Build Docker terminé - Frontend configuré pour https://service-central:8443"

build: ## Build avec profil par défaut
	@echo "Build avec profil $(MAVEN_PROFILE)..."
	@mvn clean package -P$(MAVEN_PROFILE) -DskipTests
	@$(MAKE) -s post-build
	@echo "✓ Build terminé avec profil $(MAVEN_PROFILE)"

post-build:
	@mkdir -p $(BUILD_DIR) $(LOGS_DIR) $(PIDS_DIR) $(CONFIG_DIR)
	@$(MAKE) -s generate-configs
	@echo "Configuration des services générée"

generate-configs:
	@# Config Central
	@echo "central.rmi.port=$(CENTRAL_RMI_PORT)" > $(CONFIG_DIR)/central.properties
	@echo "central.host=$(CENTRAL_HOST)" >> $(CONFIG_DIR)/central.properties
	@echo "central.http.port=$(CENTRAL_HTTP_PORT)" >> $(CONFIG_DIR)/central.properties
	@echo "central.https.enabled=$(CENTRAL_HTTPS_ENABLED)" >> $(CONFIG_DIR)/central.properties
	@echo "central.https.port=$(CENTRAL_HTTPS_PORT)" >> $(CONFIG_DIR)/central.properties
	@echo "central.keystore.path=$(CENTRAL_KEYSTORE_PATH)" >> $(CONFIG_DIR)/central.properties
	@echo "central.keystore.password=$(CENTRAL_KEYSTORE_PASSWORD)" >> $(CONFIG_DIR)/central.properties
	@echo "ssl.keystore.password=$(SSL_KEYSTORE_PASSWORD)" >> $(CONFIG_DIR)/central.properties

	@# Config BD
	@echo "bd.jdbc.url=$(BD_JDBC_URL)" > $(CONFIG_DIR)/bd.properties
	@echo "bd.jdbc.user=$(BD_JDBC_USER)" >> $(CONFIG_DIR)/bd.properties
	@echo "bd.jdbc.password=$(BD_JDBC_PASSWORD)" >> $(CONFIG_DIR)/bd.properties
	@echo "bd.rmi.name=$(BD_RMI_NAME)" >> $(CONFIG_DIR)/bd.properties
	@echo "bd.rmi.port=$(BD_RMI_PORT)" >> $(CONFIG_DIR)/bd.properties
	@echo "central.host=$(CENTRAL_HOST)" >> $(CONFIG_DIR)/bd.properties
	@echo "central.rmi.port=$(CENTRAL_RMI_PORT)" >> $(CONFIG_DIR)/bd.properties

	@# Config Proxy
	@echo "proxy.use.iut.proxy=$(PROXY_USE_IUT_PROXY)" > $(CONFIG_DIR)/proxy.properties
	@echo "proxy.iut.host=$(PROXY_IUT_HOST)" >> $(CONFIG_DIR)/proxy.properties
	@echo "proxy.iut.port=$(PROXY_IUT_PORT)" >> $(CONFIG_DIR)/proxy.properties
	@echo "central.host=$(CENTRAL_HOST)" >> $(CONFIG_DIR)/proxy.properties
	@echo "central.rmi.port=$(CENTRAL_RMI_PORT)" >> $(CONFIG_DIR)/proxy.properties

start: ## Démarrer tous les services
	@echo "Démarrage des services..."
	@$(MAKE) -s start-service SERVICE=central JAR=service-central/target/service-central-1.0-SNAPSHOT.jar
	@sleep 3
	@$(MAKE) -s start-service SERVICE=bd JAR=service-bd/target/service-bd-1.0-SNAPSHOT.jar
	@$(MAKE) -s start-service SERVICE=proxy JAR=service-proxy/target/service-proxy-1.0-SNAPSHOT.jar
	@echo "✓ Services démarrés"
	@$(MAKE) -s show-frontend-url

show-frontend-url:
	@if [ "$(CENTRAL_HTTPS_ENABLED)" = "true" ]; then \
		echo "Frontend accessible: https://$(CENTRAL_HOST):$(CENTRAL_HTTPS_PORT)"; \
		echo "⚠️  Accepter le certificat auto-signé dans le navigateur"; \
	else \
		echo "Frontend accessible: http://$(CENTRAL_HOST):$(CENTRAL_HTTP_PORT)"; \
	fi

start-service:
	@if [ -f $(PIDS_DIR)/$(SERVICE).pid ] && kill -0 $$(cat $(PIDS_DIR)/$(SERVICE).pid) 2>/dev/null; then \
		echo "$(SERVICE) déjà démarré"; \
	else \
		nohup java -Xmx512m -Djava.rmi.server.hostname=$(CENTRAL_HOST) \
			-cp "$(JAR):$(SERVICE)/target/dependency/*:common/target/common-1.0-SNAPSHOT.jar" \
			Main $(CONFIG_DIR)/$(SERVICE).properties \
			> $(LOGS_DIR)/$(SERVICE).log 2>&1 & \
		echo $$! > $(PIDS_DIR)/$(SERVICE).pid; \
		echo "✓ $(SERVICE) démarré"; \
	fi

stop: ## Arrêter tous les services
	@echo "Arrêt des services..."
	@for service in proxy bd central; do \
		if [ -f $(PIDS_DIR)/$$service.pid ]; then \
			PID=$$(cat $(PIDS_DIR)/$$service.pid 2>/dev/null || echo ""); \
			if [ -n "$$PID" ]; then \
				kill $$PID 2>/dev/null || true; \
				sleep 1; \
			fi; \
			rm -f $(PIDS_DIR)/$$service.pid; \
			echo "✓ $$service arrêté"; \
		fi; \
	done

restart: stop start ## Redémarrer tous les services

# Services individuels
start-central: ## Démarrer le service central
	@$(MAKE) -s start-service SERVICE=central JAR=service-central/target/service-central-1.0-SNAPSHOT.jar

stop-central: ## Arrêter le service central
	@$(MAKE) -s stop-service SERVICE=central

start-bd: ## Démarrer le service BD
	@$(MAKE) -s start-service SERVICE=bd JAR=service-bd/target/service-bd-1.0-SNAPSHOT.jar

stop-bd: ## Arrêter le service BD
	@$(MAKE) -s stop-service SERVICE=bd

start-proxy: ## Démarrer le service proxy
	@$(MAKE) -s start-service SERVICE=proxy JAR=service-proxy/target/service-proxy-1.0-SNAPSHOT.jar

stop-proxy: ## Arrêter le service proxy
	@$(MAKE) -s stop-service SERVICE=proxy

stop-service:
	@if [ -f $(PIDS_DIR)/$(SERVICE).pid ]; then \
		PID=$$(cat $(PIDS_DIR)/$(SERVICE).pid 2>/dev/null || echo ""); \
		if [ -n "$$PID" ]; then \
			kill $$PID 2>/dev/null || true; \
			sleep 1; \
			echo "✓ $(SERVICE) arrêté"; \
		fi; \
		rm -f $(PIDS_DIR)/$(SERVICE).pid; \
	else \
		echo "$(SERVICE) non démarré"; \
	fi

status: ## Statut des services
	@echo "Statut des services:"
	@for service in central bd proxy; do \
		if [ -f $(PIDS_DIR)/$$service.pid ]; then \
			PID=$$(cat $(PIDS_DIR)/$$service.pid 2>/dev/null || echo ""); \
			if [ -n "$$PID" ] && kill -0 $$PID 2>/dev/null; then \
				echo "  $$service: ACTIF (PID: $$PID)"; \
			else \
				echo "  $$service: CRASHÉ"; \
			fi; \
		else \
			echo "  $$service: ARRÊTÉ"; \
		fi; \
	done

logs: ## Voir les logs récents
	@echo "=== Logs récents ==="
	@for service in central bd proxy; do \
		if [ -f $(LOGS_DIR)/$$service.log ]; then \
			echo "--- $$service ---"; \
			tail -5 $(LOGS_DIR)/$$service.log; \
			echo ""; \
		fi; \
	done

logs-central: ## Suivre les logs du service central
	@tail -f $(LOGS_DIR)/central.log 2>/dev/null || echo "Service central non démarré"

logs-bd: ## Suivre les logs du service BD
	@tail -f $(LOGS_DIR)/bd.log 2>/dev/null || echo "Service BD non démarré"

logs-proxy: ## Suivre les logs du service proxy
	@tail -f $(LOGS_DIR)/proxy.log 2>/dev/null || echo "Service proxy non démarré"

clean: stop ## Nettoyer
	@echo "Nettoyage..."
	@rm -rf $(BUILD_DIR)
	@mvn clean -q
	@echo "✓ Nettoyage terminé"

# Commandes rapides pour différents environnements
deploy-dev: build-dev start  ## Build dev + Start
deploy-prod: build-prod start  ## Build prod + Start
deploy-iut: build-iut start  ## Build IUT + Start
deploy-docker: build-docker start  ## Build Docker + Start

test-api: ## Test de l'API
	@if [ "$(CENTRAL_HTTPS_ENABLED)" = "true" ]; then \
		curl -s -k "https://$(CENTRAL_HOST):$(CENTRAL_HTTPS_PORT)/services/etat" > /dev/null && echo "API OK" || echo "API KO"; \
	else \
		curl -s "http://$(CENTRAL_HOST):$(CENTRAL_HTTP_PORT)/services/etat" > /dev/null && echo "API OK" || echo "API KO"; \
	fi

generate-cert: ## Générer le certificat HTTPS
	@if [ -f $(CENTRAL_KEYSTORE_PATH) ]; then \
		echo "⚠️  Le certificat existe déjà ($(CENTRAL_KEYSTORE_PATH))"; \
		echo "Supprimez-le si vous voulez le régénérer"; \
	else \
		echo "Génération du certificat HTTPS..."; \
		keytool -genkeypair -alias nancy -keyalg RSA -keysize 2048 \
			-storetype PKCS12 -keystore $(CENTRAL_KEYSTORE_PATH) \
			-storepass $(SSL_KEYSTORE_PASSWORD) -keypass $(SSL_KEYSTORE_PASSWORD) \
			-dname "CN=$(CENTRAL_HOST), OU=SAE, O=Nancy, L=Nancy, ST=Lorraine, C=FR" \
			-validity 365 -ext SAN=dns:localhost,dns:$(CENTRAL_HOST),ip:127.0.0.1; \
		echo "✓ Certificat généré : $(CENTRAL_KEYSTORE_PATH)"; \
		echo "  Mot de passe keystore : $(SSL_KEYSTORE_PASSWORD)"; \
	fi

# Vérifier la configuration du frontend après build
check-frontend-config: ## Vérifier la configuration du frontend
	@echo "Configuration frontend actuelle :"
	@if [ -f frontend/target/nancy-frontend/js/config/constants.js ]; then \
		grep "API_BASE_URL" frontend/target/nancy-frontend/js/config/constants.js; \
	else \
		echo "⚠️  Fichier de configuration frontend non trouvé - Lancez 'make build' d'abord"; \
	fi

deploy-webetu: ## Déployer le frontend sur webetu
	@echo "Déploiement du frontend sur webetu..."
	@$(MAKE) -s check-webetu-config
	@$(MAKE) -s prepare-frontend-archive
	@$(MAKE) -s upload-frontend
	@$(MAKE) -s extract-frontend-webetu
	@echo "✓ Frontend déployé sur https://webetu.iutnc.univ-lorraine.fr/~$(WEBETU_USER)/NancySpot/"

deploy-webetu-iut: build-iut deploy-webetu ## Build IUT + déployer sur webetu

check-webetu-config:
	@if [ -z "$(WEBETU_USER)" ] || [ "$(WEBETU_USER)" = "votre_login_webetu" ]; then \
		echo "ERREUR: Variable WEBETU_USER non configurée"; \
		echo "Configurez votre login dans le fichier .env :"; \
		echo "  WEBETU_USER=votre_login"; \
		echo "Ou utilisez: make deploy-webetu WEBETU_USER=votre_login"; \
		exit 1; \
	fi
	@if [ ! -d "$(FRONTEND_BUILD_DIR)" ]; then \
		echo "ERREUR: Frontend non compilé"; \
		echo "Lancez d'abord: make build-iut"; \
		exit 1; \
	fi
	@echo "Configuration webetu :"
	@echo "  Utilisateur: $(WEBETU_USER)"
	@echo "  Répertoire: ~/$(WEBETU_PATH)"
	@echo "  Frontend: $(FRONTEND_BUILD_DIR)"

prepare-frontend-archive:
	@echo "Préparation de l'archive frontend..."
	@rm -f $(BUILD_DIR)/nancy-frontend.tar.gz
	@mkdir -p $(BUILD_DIR)
	@cd $(FRONTEND_BUILD_DIR) && tar -czf ../../../$(BUILD_DIR)/nancy-frontend.tar.gz .
	@echo "✓ Archive créée: $(BUILD_DIR)/nancy-frontend.tar.gz"

upload-frontend:
	@echo "Upload vers webetu ($(WEBETU_USER)@$(WEBETU_HOST))..."
	@scp $(BUILD_DIR)/nancy-frontend.tar.gz $(WEBETU_USER)@$(WEBETU_HOST):
	@echo "✓ Archive uploadée"

extract-frontend-webetu:
	@echo "Décompression sur webetu..."
	@ssh $(WEBETU_USER)@$(WEBETU_HOST) '\
		mkdir -p $(WEBETU_PATH) && \
		cd $(WEBETU_PATH) && \
		tar -xzf ~/nancy-frontend.tar.gz && \
		rm ~/nancy-frontend.tar.gz && \
		echo "Frontend décompressé dans ~/$(WEBETU_PATH)/"'
	@echo "✓ Frontend déployé"

clean-webetu: ## Nettoyer le frontend sur webetu
	@echo "Nettoyage du frontend sur webetu..."
	@if [ -z "$(WEBETU_USER)" ] || [ "$(WEBETU_USER)" = "votre_login_webetu" ]; then \
		echo "ERREUR: Variable WEBETU_USER non configurée dans .env"; \
		exit 1; \
	fi
	@ssh $(WEBETU_USER)@$(WEBETU_HOST) '\
		if [ -d $(WEBETU_PATH) ]; then \
			rm -rf $(WEBETU_PATH)/*; \
			echo "Dossier NancySpot nettoyé"; \
		else \
			echo "Dossier $(WEBETU_PATH) inexistant"; \
		fi'

deploy-webetu-clean: clean-webetu deploy-webetu ## Nettoyer + déployer sur webetu

# Test du frontend déployé
test-webetu: ## Tester le frontend sur webetu
	@if [ -z "$(WEBETU_USER)" ] || [ "$(WEBETU_USER)" = "votre_login_webetu" ]; then \
		echo "ERREUR: Variable WEBETU_USER non configurée dans .env"; \
		exit 1; \
	fi
	@echo "Test du frontend sur webetu..."
	@curl -s -I "https://webetu.iutnc.univ-lorraine.fr/~$(WEBETU_USER)/NancySpot/" | head -1 || echo "Site non accessible"
	@echo "URL: https://webetu.iutnc.univ-lorraine.fr/~$(WEBETU_USER)/NancySpot/"

# Commande pour voir les logs de déploiement webetu
logs-webetu: ## Voir les fichiers sur webetu
	@if [ -z "$(WEBETU_USER)" ] || [ "$(WEBETU_USER)" = "votre_login_webetu" ]; then \
		echo "ERREUR: Variable WEBETU_USER non configurée dans .env"; \
		exit 1; \
	fi
	@ssh $(WEBETU_USER)@$(WEBETU_HOST) 'ls -la $(WEBETU_PATH)/'