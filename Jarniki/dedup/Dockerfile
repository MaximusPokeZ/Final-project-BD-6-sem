FROM openjdk:21-jdk

WORKDIR /app

COPY jar/Deduplication-5.0.0-all.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]