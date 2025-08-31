FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
# Copie le JAR sans dépendre du nom exact
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
