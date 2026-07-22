FROM maven:3.9.9-eclipse-temurin-8 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q package

FROM eclipse-temurin:8-jre
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --user-group --create-home appuser
COPY --from=builder --chown=appuser:appuser /app/target/mall-test-system-0.0.1-SNAPSHOT.jar app.jar
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=10 \
    CMD curl --fail --silent http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
