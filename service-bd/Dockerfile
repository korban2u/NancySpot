FROM openjdk:17-jdk-slim

WORKDIR /app

COPY service-bd/target/service-bd-1.0-SNAPSHOT.jar /app/nancy-service-bd.jar

# copie des dépendances
COPY service-bd/target/dependency/*.jar /app/lib/
COPY common/target/common-1.0-SNAPSHOT.jar /app/lib/


COPY docker/scripts/generate-config-bd.sh /app/
RUN chmod +x /app/generate-config-bd.sh

# variables environnement par def
ENV JAVA_OPTS="-Xmx512m -Djava.rmi.server.hostname=service-bd"

# script du démarrage
CMD ["/bin/bash", "-c", "./generate-config-bd.sh && java $JAVA_OPTS -cp '/app/lib/*:/app/nancy-service-bd.jar' Main config.properties"]