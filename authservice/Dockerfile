# ====== Build stage ======
FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /workspace

# Copy only the module to leverage Docker layer caching
COPY pom.xml ./pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -B -DskipTests dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -B -DskipTests package

# ====== Runtime stage ======
FROM eclipse-temurin:21-jre

WORKDIR /app

ENV JAVA_OPTS=""

# Copy built jar
COPY --from=build /workspace/target/*.jar /app/app.jar

# Default port
EXPOSE 8005

# Healthcheck optional (expects actuator if enabled)
# HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8005/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]


