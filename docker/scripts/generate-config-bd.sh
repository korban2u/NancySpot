#!/bin/bash

# Script de génération de configuration pour Service BD

echo "=== Génération de la configuration Service BD ==="

# Vérifier que les variables obligatoires sont définies
if [ -z "$BD_JDBC_URL" ] || [ -z "$BD_JDBC_USER" ] || [ -z "$BD_JDBC_PASSWORD" ]; then
    echo "ERREUR: Variables de base de données manquantes!"
    echo "Vérifiez BD_JDBC_URL, BD_JDBC_USER, BD_JDBC_PASSWORD"
    exit 1
fi

# Créer le fichier config.properties à partir des variables d'environnement
cat > config.properties << EOF
# Configuration Service BD - Généré automatiquement
# $(date)

# Configuration Base de Données
bd.jdbc.url=${BD_JDBC_URL}
bd.jdbc.user=${BD_JDBC_USER}
bd.jdbc.password=${BD_JDBC_PASSWORD}
bd.rmi.name=${BD_RMI_NAME:-ServiceBD}
bd.rmi.port=${BD_RMI_PORT:-1099}

# Configuration Service Central
central.host=${CENTRAL_HOST:-service-central}
central.rmi.port=${CENTRAL_RMI_PORT:-1098}
EOF

echo "Configuration générée (sans mot de passe):"
sed 's/bd.jdbc.password=.*/bd.jdbc.password=***/' config.properties

echo "=== Service BD prêt à démarrer ==="