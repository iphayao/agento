# ── Build stage ───────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven && \
    mvn -B -q dependency:go-offline && \
    mvn -B -q package -DskipTests

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S agento && adduser -S agento -G agento

WORKDIR /app

COPY --from=builder /app/target/agento-api-*.jar app.jar

# Export storage volume
RUN mkdir -p /data/exports && chown agento:agento /data/exports
VOLUME ["/data/exports"]

USER agento

EXPOSE 8080

ENV EXPORT_STORAGE_PATH=/data/exports

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
