FROM openjdk:17-jdk

WORKDIR /app

COPY jar/Authorization-0.0.1-SNAPSHOT.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]