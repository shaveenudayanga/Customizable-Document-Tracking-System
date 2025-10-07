# Notification Service (Spring Boot 3, Java 21)

## Quick run (Docker + docker-compose)
1. Build the jar locally:
   ```bash
   mvn clean package -DskipTests
   ```

2. Start with docker-compose (this will build the image for the service):
   ```bash
   docker-compose up --build
   ```

   The app will be reachable on http://localhost:8081

## Endpoints
POST /api/v1/notifications
GET  /api/v1/notifications/{id}

## Notes
- Replace mail configuration in application.yml with real SMTP credentials.
- Integrate a real SMS provider for production (Twilio, etc.).
- Add authentication/authorization for production use.
