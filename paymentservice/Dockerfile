# Multi-stage build for Payment Service
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /workspace

# Pre-fetch dependencies for shared core module to satisfy local builds
COPY core/pom.xml core/pom.xml
RUN mvn -f core/pom.xml dependency:go-offline -B
COPY core/src core/src
RUN mvn -f core/pom.xml clean install -DskipTests

# Build the payment service
WORKDIR /app
COPY paymentservice/pom.xml pom.xml
RUN mvn dependency:go-offline -B
COPY paymentservice/src src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8010

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8010/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]