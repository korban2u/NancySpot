#!/bin/bash

# Script de génération de configuration pour Service Central

echo "=== Génération de la configuration Service Central ==="

# Créer le fichier config.properties à partir des variables d'environnement
cat > config.properties << EOF
# Configuration Service Central - Généré automatiquement
# $(date)

# Configuration RMI
central.rmi.port=${CENTRAL_RMI_PORT:-1098}
central.host=${CENTRAL_HOST:-service-central}

# Configuration HTTP
central.http.port=${CENTRAL_HTTP_PORT:-8080}

# Configuration HTTPS
central.https.enabled=${CENTRAL_HTTPS_ENABLED:-true}
central.https.port=${CENTRAL_HTTPS_PORT:-8443}
central.keystore.path=${CENTRAL_KEYSTORE_PATH:-nancy-keystore.jks}
central.keystore.password=${CENTRAL_KEYSTORE_PASSWORD:-password123}

# Configuration SSL
ssl.keystore.password=${SSL_KEYSTORE_PASSWORD:-nancy2024}
EOF

echo "Configuration générée:"
cat config.properties

echo "=== Service Central prêt à démarrer ==="