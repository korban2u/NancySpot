#!/bin/bash

# Script de génération de configuration pour Service Proxy

echo "=== Génération de la configuration Service Proxy ==="

# Créer le fichier config.properties à partir des variables d'environnement
cat > config.properties << EOF
# Configuration Service Proxy - Généré automatiquement
# $(date)

# Configuration Proxy
proxy.use.iut.proxy=${PROXY_USE_IUT_PROXY:-false}
proxy.iut.host=${PROXY_IUT_HOST:-proxy.infra.univ-lorraine.fr}
proxy.iut.port=${PROXY_IUT_PORT:-3128}

# Configuration Service Central
central.host=${CENTRAL_HOST:-service-central}
central.rmi.port=${CENTRAL_RMI_PORT:-1098}
EOF

echo "Configuration générée:"
cat config.properties

echo "=== Service Proxy prêt à démarrer ==="