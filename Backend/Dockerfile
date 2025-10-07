# Use a JDK 21 base image
FROM eclipse-temurin:21-jdk-jammy
ARG JAR_FILE=target/notification-service-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
EXPOSE 1002
ENTRYPOINT ["java","-jar","/app.jar"]
