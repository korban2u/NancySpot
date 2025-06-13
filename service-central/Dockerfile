FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# répertoire pour les certificats
RUN mkdir -p /app/certs

COPY service-central/target/service-central-1.0-SNAPSHOT.jar /app/nancy-service-central.jar

# copie des dépendances
COPY service-central/target/dependency/*.jar /app/lib/
COPY common/target/common-1.0-SNAPSHOT.jar /app/lib/

COPY docker/scripts/generate-config-central.sh /app/
RUN chmod +x /app/generate-config-central.sh

# création un keystore si nécessaire
COPY docker/certs/nancy-keystore.jks /app/nancy-keystore.jks

# variables environnement par def
ENV JAVA_OPTS="-Xmx512m -Djava.rmi.server.hostname=service-central"

# on expose les ports
EXPOSE 8080 8443 1098

# script du démarrage
CMD ["/bin/bash", "-c", "./generate-config-central.sh && java $JAVA_OPTS -cp '/app/lib/*:/app/nancy-service-central.jar' Main config.properties"]