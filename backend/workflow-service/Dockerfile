# Multi-stage Dockerfile for Workflow Service
# Build stage
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Copy Maven wrapper and configuration
COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY src src

# Package application
RUN chmod +x mvnw \
    && ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Copy packaged jar
COPY --from=build /workspace/target/*.jar app.jar

# Expose service port
EXPOSE 8083

# Start service
ENTRYPOINT ["java", "-jar", "app.jar"]
