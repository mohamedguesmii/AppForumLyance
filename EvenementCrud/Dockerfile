# Dockerfile backend
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/EvenementCrud-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
