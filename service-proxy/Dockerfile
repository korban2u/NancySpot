FROM openjdk:17-jdk-slim

WORKDIR /app

COPY service-proxy/target/service-proxy-1.0-SNAPSHOT.jar /app/nancy-service-proxy.jar

# copie des dépendances
COPY service-proxy/target/dependency/*.jar /app/lib/
COPY common/target/common-1.0-SNAPSHOT.jar /app/lib/

COPY docker/scripts/generate-config-proxy.sh /app/
RUN chmod +x /app/generate-config-proxy.sh

# variables environnement par def
ENV JAVA_OPTS="-Xmx512m -Djava.rmi.server.hostname=service-proxy"

# script du démarrage
CMD ["/bin/bash", "-c", "./generate-config-proxy.sh && java $JAVA_OPTS -cp '/app/lib/*:/app/nancy-service-proxy.jar' Main config.properties"]