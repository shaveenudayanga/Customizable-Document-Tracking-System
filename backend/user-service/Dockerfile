# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
# Copy wrapper & .mvn directory if present for consistency (optional)
COPY mvnw* ./
COPY .mvn .mvn
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package

# Runtime stage (distroless)
FROM gcr.io/distroless/java17
USER 10001:10001
WORKDIR /srv
COPY --from=build /app/target/user-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS=""
ENTRYPOINT ["java","-jar","/srv/app.jar","--spring.profiles.active=prod"]
