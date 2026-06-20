FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY target/agento-api-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
