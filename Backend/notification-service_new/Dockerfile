# Build stage (use Maven image to avoid wrapper issues on Linux)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy only this module to leverage Docker layer caching in a mono-repo
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

COPY src/ src/
RUN mvn -q -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy built jar
COPY --from=build /workspace/target/*.jar app.jar

# Expose service port (matches server.port in application.properties)
EXPOSE 8083

# Default envs
ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
