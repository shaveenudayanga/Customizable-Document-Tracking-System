# Multi-stage Dockerfile for Document Service
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

# Expose the application port
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
