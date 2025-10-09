# Build stage (use Maven image to avoid wrapper issues on Linux)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy only this module to leverage Docker layer caching in a mono-repo
COPY pom.xml ./

COPY src/ src/
# Retry Maven package a few times to mitigate transient network issues; skip Spotless and test compilation
RUN bash -c 'for i in 1 2 3; do mvn -B -q -DskipTests -Dmaven.test.skip=true -Dspotless.skip=true package && exit 0 || echo "Maven build failed, retry #$i" && sleep 5; done; exit 1'

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy built jar
COPY --from=build /workspace/target/*.jar app.jar

# Expose service port (matches server.port in application.properties)
EXPOSE 1004

# Default envs
ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
