# Multi-stage Dockerfile for API Gateway
# Build stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

# Copy Maven wrapper and project files
COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY src src

# Build the application and skip tests
RUN chmod +x mvnw && \
    ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre AS runner
WORKDIR /app

# Copy the packaged jar from builder
COPY --from=builder /workspace/target/*.jar app.jar

# Expose the gateway port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
