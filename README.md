# College Stationery Management System — Microservices Capstone

The **College Stationery Management System** is a full-stack, production-ready microservices application designed to automate and simplify the request, tracking, and management of college stationery items. The system supports two user roles:
*   **STUDENT**: Browse the stationery catalog, add items to a request cart, submit requests, and track request status.
*   **ADMIN**: Manage stationery items (add, update, delete, view stock), monitor pending requests, and approve or reject requests with comment justifications.

---

## Architecture Diagram

The system comprises service discovery, centralized routing, an API gateway with role-based JWT validation, separate MySQL databases for service isolation, and a React frontend.

```
                  +-------------------------+
                  |  React SPA (Port 3000)  |
                  +------------+------------+
                               |
                               | HTTP (JWT Bearer Token)
                               ▼
                  +------------+------------+      +-------------------------+
                  |  API Gateway (Port 8084)| <==> |  Eureka Server (8761)   |
                  +------------+------------+      +-------------------------+
                               |
            +------------------+------------------+
            |                  |                  |
            ▼                  ▼                  ▼
      +-----------+      +-----------+      +-----------+
      |   Auth    |      | Inventory |      |  Request  |
      |  Service  |      |  Service  |      |  Service  |
      | Port 8081 |      | Port 8082 |      | Port 8083 |
      +-----+-----+      +-----+-----+      +-----+-----+
            |                  |                  |
            |                  | <================+  OpenFeign Call
            |                  |                    (Stock deduction / name check)
            ▼                  ▼                  ▼
      +-----------+      +-----------+      +-----------+
      |  auth_db  |      | invent_db |      |request_db |
      | (MySQL)   |      |  (MySQL)  |      |  (MySQL)  |
      +-----------+      +-----------+      +-----------+
```

---

## Tech Stack

*   **Backend (Java)**: Spring Boot 3.5.15, Spring Cloud 2025.0.3 (Northfields), Spring Security + JWT, Spring Data JPA, Netflix Eureka, OpenFeign.
*   **Frontend (JavaScript)**: React 18, Vite 8, Tailwind CSS v4, React Router 6, Axios, React Hook Form, React Toastify.
*   **DevOps & Database**: Docker, Docker Compose, Jenkins CI/CD, MySQL 8.0, Nginx (for serving frontend production builds).

---

## Port Allocation

| Component | Port Mapping | Description |
|---|---|---|
| **Vite / React Dev** | `3000` | Local React Development Server |
| **Nginx (Frontend)** | `3000:80` | Frontend served in Docker |
| **API Gateway** | `8084` | Gateway routing all API traffic |
| **Eureka Discovery** | `8761` | Service Registry Dashboard |
| **Config Server** | `8888` | Centralized Configuration Server |
| **Auth Service** | `8081` | Authentication & User Management |
| **Inventory Service** | `8082` | Stationery Item CRUD & Stock management |
| **Request Service** | `8083` | Stationery Request & Approval workflow |
| **MySQL Database** | `3306` (host) / `3307` (compose) | Host native database / Docker Compose database |

---

## Environment Variables

| Variable | Target Service | Default / Example Value |
|---|---|---|
| `SPRING_DATASOURCE_URL` | Microservices | `jdbc:mysql://mysql:3306/auth_db?createDatabaseIfNotExist=true` |
| `SPRING_DATASOURCE_USERNAME`| Microservices | `root` |
| `SPRING_DATASOURCE_PASSWORD`| Microservices | `Alien` |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`| Microservices | `http://eureka-server:8761/eureka/` |
| `JWT_SECRET` | Auth, Gateway | `mySuper256BitSecretKeyForJWTSigningAndValidation!` |

---

## Quickstart — Running with Docker Compose

To build and run the entire ecosystem (all 6 backend services, database, and the frontend) inside Docker:

1.  **Build parent modules and compile JARs**:
    ```bash
    mvn clean package -DskipTests
    ```
2.  **Launch the containerized application**:
    ```bash
    docker compose -f ci-cd/docker-compose.yml up -d --build
    ```
3.  **Explore**:
    *   **Frontend SPA**: `http://localhost:3000`
    *   **Eureka Registry**: `http://localhost:8761`

---

## Interactive API Documentation (Swagger UI)

Swagger OpenAPI documentation is configured for all backend microservices. When the services are running, you can access their interactive specs at:

*   **Auth Service API Docs**: `http://localhost:8081/swagger-ui.html`
*   **Inventory Service API Docs**: `http://localhost:8082/swagger-ui.html`
*   **Request Service API Docs**: `http://localhost:8083/swagger-ui.html`

---

## Unit Testing & Verification

Unit tests use JUnit 5 and Mockito. Database and OpenFeign network calls are mocked, allowing verification without running external servers.

*   To execute all backend tests:
    ```bash
    mvn test
    ```
*   **Total Tests**: 19 tests across all service layers, covering registration, credentials validation, stock thresholds, and request approval logic.
