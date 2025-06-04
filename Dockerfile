FROM openjdk:25-ea-21-slim-bookworm

WORKDIR /app

COPY target/Levita-Monitoring-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]