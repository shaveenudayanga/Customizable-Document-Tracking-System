# Multi-stage Dockerfile for Tracking Service
# Build stage
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Copy Maven wrapper and project metadata
COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY src src

# Build application
RUN chmod +x mvnw \
    && ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Copy built artifact
COPY --from=build /workspace/target/*.jar app.jar

# Expose service port
EXPOSE 8084

# Run service
ENTRYPOINT ["java", "-jar", "app.jar"]
