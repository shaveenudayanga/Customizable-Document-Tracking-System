# Customizable Document Tracking System

## Problem Statement

**How can we eliminate the bureaucratic nightmare of physical documents moving slowly from desk to desk, reducing waiting times and transforming frustrating government experiences into efficient digital workflows?**

### The Reality We're Solving

In countless organizations worldwide, particularly in traditional government offices and bureaucratic institutions, a familiar struggle persists: **physical documents crawling through manual processes**, creating bottlenecks that affect millions of people daily.

**The Daily Struggle:**

* **Endless Waiting:** Citizens spend hours in queues, often making multiple visits to government offices for a single service
* **Lost in Transit:** Documents frequently get misplaced, damaged, or "stuck" on someone's desk for days or weeks
* **No Visibility:** Nobody knows where a document is at any given time; not the citizen, not the staff, sometimes not even the department
* **Manual Chaos:** Physical files move from table to table, department to department, with no systematic tracking or accountability
* **Storage Crisis:** Organizations desperately scramble to find physical space to store growing mountains of paperwork
* **Time Drain:** What should take hours takes days; what should take days takes weeks

**Real Impact:** Research shows that manual document processing causes 40-60% of time to be spent just waiting for the next step. Processing times vary dramatically, from 6 days in efficient systems to 74 days in severely constrained environments. When organizations digitize these processes, waiting times drop by 16-96 minutes on average.

## Our Solution

The **Customizable Document Tracking System** transforms this broken reality by creating **digital pathways that mirror and improve real-world document flows**. Organizations can:

* **Map Custom Workflows:** Define exactly how documents should move through your unique organizational structure
* **Track in Real-Time:** See instantly where every document is, who has it, and what happens next
* **Enable Mobile Handovers:** Staff can transfer and verify documents by scanning QR codes or entering secure hash codes
* **Eliminate Paper Chaos:** Digital workflows replace physical document shuffling with organized, trackable processes
* **Provide Transparency:** Citizens and staff can monitor progress without phone calls or office visits
* **Ensure Accountability:** Complete audit trails show every action, handover, and delay

## Why This Matters

This system addresses the **bureaucratic inefficiencies that plague developing nations and traditional institutions worldwide**. By digitizing document workflows, we tackle the root causes of administrative delays that:

* Force citizens to take time off work for multiple government visits
* Create frustration and loss of trust in public institutions
* Waste staff time on manual tracking instead of productive work
* Generate compliance and transparency challenges for organizations

**Our mission:** Transform the age-old problem of "where is my document?" into transparent, efficient, and accountable digital processes that serve both organizations and the people who depend on them.

## Technology Stack

* **Backend:** Spring Boot (Java) - robust, enterprise-ready microservices
* **Database:** PostgreSQL - reliable, ACID-compliant with JSON flexibility
* **Architecture:** Microservices with hybrid SQL approach
* **Security:** End-to-end encryption, role-based access, comprehensive audit trails
* **Mobile Integration:** QR code scanning, web portals for verification
* **DevOps:** Docker containerization, CI/CD pipelines, Kubernetes-ready

## Key Features

* **Visual Pipeline Builder:** Create custom document workflows without coding
* **Real-Time Dashboard:** Monitor all document statuses and bottlenecks
* **Mobile Verification:** Secure handovers via QR codes or hash verification
* **Multi-Level Organization:** Support for departments, categories, and complex hierarchies
* **Comprehensive Auditing:** Track every action for compliance and transparency
* **Event-Driven Notification System:** Automated alerts via email, SMS, and web push
* **API-First Design:** Easy integration with existing systems

## Notification System Integration

The system uses a dedicated **notification-service** that handles all notification delivery through an event-driven architecture using RabbitMQ.

### Key Documents:
- **[NOTIFICATION_INTEGRATION_GUIDE.md](./backend/NOTIFICATION_INTEGRATION_GUIDE.md)** - Comprehensive integration guide
- **[NOTIFICATION_INTEGRATION_SUMMARY.md](./NOTIFICATION_INTEGRATION_SUMMARY.md)** - Summary of changes made
- **[QUICK_START_NOTIFICATIONS.md](./QUICK_START_NOTIFICATIONS.md)** - Quick start guide for developers

### Integration Status:
- ✅ **Workflow Service**: Fully integrated with RabbitMQ event publishing
- ✅ **User Service**: Notification code removed (centralized to notification-service)
- 📝 **Document Service**: Example code provided, ready for integration
- 📝 **Tracking Service**: Ready for integration if needed

### Services:
- **notification-service_new** (port 1004): Centralized notification handling
- **RabbitMQ**: Message broker for event-driven communication

See the integration guides for detailed setup instructions.

## License

This project is licensed under the MIT License. See [`LICENSE`](./LICENSE) for details.

---

**Breaking the cycle of bureaucratic delays, one digital workflow at a time.**

## Architecture & Operations

See the high-level multi-tenant architecture, workflows, and operations in `docs/architecture/multi-tenant-overview.md`.

## User Service (Authentication & User Management)

The `user-service` microservice handles registration, authentication (JWT + refresh tokens), and administrative user management.

Note on IDs and relationships:
- `user-service` uses UUID as the canonical user identifier and owns its database schema.
- Other services should store user references as UUID strings and must not create cross-service database foreign keys.
- Cross-service integrity is enforced via APIs/events, not DB-level FKs. See docs/adr/0001-user-ids-and-cross-service-references.md.

### Quick Start (Docker Compose)

```
cd backend/user-service
docker compose build
docker compose up -d
```

Service will be available at `http://localhost:8080` (default admin endpoints protected).

### API Documentation

Once the service is running locally you can explore the interactive OpenAPI docs at:

* Swagger UI: `http://localhost:8081/swagger-ui/index.html`
* Raw OpenAPI spec: `http://localhost:8081/v3/api-docs`

### API Endpoints

Public:
* `POST /api/auth/register` – register user
* `POST /api/auth/login` – authenticate and receive access + refresh tokens
* `POST /api/auth/refresh` – refresh access token

Authenticated:
* `GET /api/users/me` – current user profile

Admin Only:
* `GET /api/admin/users` – list users
* `PATCH /api/admin/users/{id}/role` – change role `{ "role": "ADMIN" }`
* `PATCH /api/admin/users/{id}/active` – activate/deactivate `{ "active": false }`
* `DELETE /api/admin/users/{id}` – delete user

### Environment Variables (override defaults)

* `POSTGRES_USER`, `POSTGRES_PASSWORD` – database credentials
* `JWT_SECRET` – strong 256-bit secret (required for production)
* `SPRING_PROFILES_ACTIVE=prod` – activates docker profile (already set in compose)

### Running Tests

```
mvn test -f backend/user-service/pom.xml
```

### Build Jar (local dev)

```
mvn -f backend/user-service/pom.xml clean package
java -jar backend/user-service/target/user-service-0.0.1-SNAPSHOT.jar
```

### Tokens

Access tokens expire (default 30m). Refresh tokens (default 7d) can request new access tokens. Include header:

`Authorization: Bearer <access_token>`

### Notes

* Update `application.yml` production block or environment variables for deployment.
* Replace sample JWT secret in any non-development environment.
* Add monitoring endpoints via Spring Actuator (already included) if needed.

