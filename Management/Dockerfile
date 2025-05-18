# Используем базовый образ с Java
FROM openjdk:21-jdk-slim


WORKDIR /app


COPY build/libs/JavaServiceManagment-1.0-SNAPSHOT.jar app.jar


EXPOSE 8081


ENTRYPOINT ["java", "-jar", "app.jar"]