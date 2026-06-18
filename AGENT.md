# AGENT.md — Stationery Management System
> **For AI Agents:** This file is your complete implementation guide. Read it top to bottom before writing a single line of code. Everything you need — architecture, file structure, configs, day-wise tasks, acceptance criteria — is here.

---

## 0. Quick Mental Model

```
Browser (React on :3000)
     ↓ HTTP
API Gateway (:8080)  ←→  Eureka Server (:8761)  ←→  Config Server (:8888)
     ↓ routes by path prefix
  ┌──────────────────────────────────┐
  │  Auth (:8081) │ Inventory (:8082) │ Request (:8083)  │
  │  auth_db      │ inventory_db      │ request_db        │
  └──────────────────────────────────┘
All services → MySQL (:3306)
Jenkins (:8085) builds & deploys via Docker Compose
```

**One rule:** The frontend ONLY talks to the Gateway. Never directly to a microservice.

---

## 1. Project Identity

| Field | Value |
|-------|-------|
| Name | College Stationery Management System |
| Type | Capstone — Microservices Full-Stack |
| Duration | 5-Day Sprint |
| Users | ADMIN (manage inventory, approve requests) · STUDENT (browse, request items) |
| Entry Point | `http://localhost:8080` (Gateway) |

---

## 2. Complete Tech Stack

### Backend (Java)
| Technology | Version | Role |
|-----------|---------|------|
| Java | 17 | Language |
| Spring Boot | 3.2.5 | Framework |
| Spring Cloud | 2023.0.1 (Leyton) | Microservice infrastructure |
| Spring Security + JWT | 3.x / jjwt 0.12.x | Auth & RBAC |
| Spring Data JPA / Hibernate | bundled | ORM |
| Spring Cloud Netflix Eureka | 2023.0.1 | Service discovery |
| Spring Cloud Gateway | 2023.0.1 | API Gateway (reactive) |
| Spring Cloud Config | 2023.0.1 | Centralized config |
| Spring Cloud OpenFeign | 2023.0.1 | Inter-service HTTP calls |
| MySQL Connector/J | 8.x | JDBC driver |
| Lombok | latest | Boilerplate reduction |
| springdoc-openapi | 2.x | Swagger UI |
| JUnit 5 + Mockito | bundled | Unit testing |
| JaCoCo | 0.8.x | Coverage reporting (≥70% target) |
| Maven | 3.9+ | Build tool |

### Frontend (JavaScript)
| Package | Version | Role |
|--------|---------|------|
| React + react-dom | 18.x | UI framework |
| react-router-dom | 6.x | Client-side routing |
| axios | 1.x | HTTP client |
| jwt-decode | 4.x | Decode JWT on client |
| react-hook-form | 7.x | Form handling |
| react-toastify | 10.x | Notifications |
| tailwindcss | 3.x | Styling (preferred over MUI) |

### DevOps
| Tool | Version | Role |
|------|---------|------|
| Docker | latest | Container runtime |
| Docker Compose | v2 | Local orchestration |
| Jenkins | LTS | CI/CD |
| Nginx | alpine | Serve React static build |
| Git + GitHub | — | Version control |

---

## 3. Port Allocation (Never Conflict These)

| Service | Port |
|---------|------|
| Eureka Server | 8761 |
| Config Server | 8888 |
| API Gateway | 8080 |
| Auth Service | 8081 |
| Inventory Service | 8082 |
| Request Service | 8083 |
| React (dev) | 3000 |
| React (Docker/Nginx) | 80 |
| MySQL | 3306 |
| Jenkins | 8085 |

---

## 4. Maven Multi-Module Structure

```
stationery-management-system/          ← root
├── pom.xml                            ← Parent POM (BOM, modules list)
├── eureka-server/
├── config-server/
├── api-gateway/
├── auth-service/
├── inventory-service/
├── request-service/
├── frontend/
└── ci-cd/
    ├── Jenkinsfile
    └── docker-compose.yml
```

### Parent POM Must Declare:
```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.2.5</version>
</parent>

<properties>
  <java.version>17</java.version>
  <spring-cloud.version>2023.0.1</spring-cloud.version>
</properties>

<modules>
  <module>eureka-server</module>
  <module>config-server</module>
  <module>api-gateway</module>
  <module>auth-service</module>
  <module>inventory-service</module>
  <module>request-service</module>
</modules>

<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-dependencies</artifactId>
      <version>${spring-cloud.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

---

## 5. Database Schema (Three Separate DBs)

### auth_db · Table: `users`
```sql
CREATE TABLE users (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  email        VARCHAR(255) UNIQUE NOT NULL,
  password     VARCHAR(255) NOT NULL,           -- BCrypt hashed
  full_name    VARCHAR(150) NOT NULL,
  role         ENUM('ADMIN','STUDENT') NOT NULL DEFAULT 'STUDENT',
  created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### inventory_db · Table: `stationery_items`
```sql
CREATE TABLE stationery_items (
  id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
  name               VARCHAR(200) NOT NULL,
  category           ENUM('PAPER','PEN','PENCIL','NOTEBOOK','ERASER','OTHER') NOT NULL,
  unit               VARCHAR(50),
  available_quantity INT NOT NULL DEFAULT 0,
  minimum_quantity   INT,                        -- low-stock threshold
  created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at         TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### request_db · Tables: `stationery_requests` + `request_items`
```sql
CREATE TABLE stationery_requests (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id     BIGINT NOT NULL,
  student_email  VARCHAR(255),                   -- denormalized for display
  status         ENUM('PENDING','APPROVED','REJECTED','FULFILLED') DEFAULT 'PENDING',
  admin_comment  TEXT,
  request_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE request_items (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  request_id  BIGINT NOT NULL REFERENCES stationery_requests(id),
  item_id     BIGINT NOT NULL,                   -- cross-service ref, no FK constraint
  item_name   VARCHAR(200),                      -- denormalized snapshot
  quantity    INT NOT NULL CHECK (quantity > 0)
);
```

> **Agent Note:** No cross-DB foreign keys. `item_id` in `request_items` references `stationery_items.id` logically but is validated at runtime via Feign call, not via DB constraint.

---

## 6. Complete File Tree (Every File to Create)

```
auth-service/src/main/java/com/sms/auth/
├── AuthServiceApplication.java
├── controller/AuthController.java
├── service/AuthService.java (interface)
├── service/AuthServiceImpl.java
├── model/User.java
├── model/dto/RegisterRequest.java
├── model/dto/LoginRequest.java
├── model/dto/AuthResponse.java
├── repository/UserRepository.java
├── security/JwtUtil.java
├── security/JwtAuthFilter.java
├── security/SecurityConfig.java
└── exception/GlobalExceptionHandler.java

inventory-service/src/main/java/com/sms/inventory/
├── InventoryServiceApplication.java
├── controller/StationeryController.java
├── service/StationeryService.java (interface)
├── service/StationeryServiceImpl.java
├── model/StationeryItem.java
├── model/dto/ItemRequest.java
├── model/dto/ItemResponse.java
├── repository/StationeryRepository.java
└── exception/GlobalExceptionHandler.java

request-service/src/main/java/com/sms/request/
├── RequestServiceApplication.java
├── controller/RequestController.java
├── service/RequestService.java (interface)
├── service/RequestServiceImpl.java
├── model/StationeryRequest.java
├── model/RequestItem.java
├── model/dto/SubmitRequestDTO.java
├── model/dto/RequestResponseDTO.java
├── repository/RequestRepository.java
├── repository/RequestItemRepository.java
├── feign/InventoryFeignClient.java
└── exception/GlobalExceptionHandler.java

api-gateway/src/main/java/com/sms/gateway/
├── GatewayApplication.java
├── config/RouteConfig.java
├── config/CorsConfig.java
└── config/JwtAuthFilter.java

frontend/src/
├── App.jsx
├── index.js
├── api/axiosConfig.js
├── context/AuthContext.jsx
├── pages/LoginPage.jsx
├── pages/RegisterPage.jsx
├── pages/DashboardPage.jsx
├── pages/CatalogPage.jsx
├── pages/MyRequestsPage.jsx
├── pages/admin/ManageInventoryPage.jsx
├── pages/admin/ManageRequestsPage.jsx
└── components/
    ├── Navbar.jsx
    ├── ProtectedRoute.jsx
    ├── ItemCard.jsx
    └── RequestTable.jsx
```

---

## 7. API Endpoints (All via Gateway at :8080)

### Auth Service → prefix `/api/auth`
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /api/auth/register | Public | Register user → returns JWT |
| POST | /api/auth/login | Public | Login → returns JWT |
| GET | /api/auth/validate | Bearer token | Validate token |
| GET | /api/auth/me | Bearer token | Get current user profile |

### Inventory Service → prefix `/api/inventory`
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | /api/inventory | Bearer | List items (paginated, 20/page) |
| GET | /api/inventory/{id} | Bearer | Get single item |
| GET | /api/inventory/search?q= | Bearer | Search by name |
| GET | /api/inventory/low-stock | ADMIN | Items below minimum_quantity |
| POST | /api/inventory | ADMIN | Add new item |
| PUT | /api/inventory/{id} | ADMIN | Update item |
| DELETE | /api/inventory/{id} | ADMIN | Delete item |
| PUT | /api/inventory/{id}/deduct?qty= | Internal (Feign) | Deduct stock on approval |

### Request Service → prefix `/api/requests`
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /api/requests | STUDENT | Submit new request |
| GET | /api/requests/my | STUDENT | My requests |
| GET | /api/requests | ADMIN | All requests |
| GET | /api/requests/{id} | Bearer | Single request detail |
| GET | /api/requests/status/{status} | ADMIN | Filter by status |
| PUT | /api/requests/{id}/approve | ADMIN | Approve + deduct stock |
| PUT | /api/requests/{id}/reject | ADMIN | Reject with comment |

---

## 8. Key Code Patterns (Copy These Exactly)

### JwtUtil.java — must implement:
- `String generateToken(UserDetails user)` — includes email + role as claims
- `boolean validateToken(String token, UserDetails user)`
- `String extractEmail(String token)`
- `String extractRole(String token)`
- Secret key min 256-bit, loaded from config server env var `JWT_SECRET`

### SecurityConfig.java pattern:
```java
http
  .csrf(csrf -> csrf.disable())
  .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))
  .authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/inventory").hasRole("ADMIN")
    .requestMatchers(HttpMethod.PUT, "/api/inventory/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.DELETE, "/api/inventory/**").hasRole("ADMIN")
    .anyRequest().authenticated())
  .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
  .build();
```

### Feign Client (request-service):
```java
@FeignClient(name = "inventory-service")  // must match spring.application.name exactly
public interface InventoryFeignClient {
    @GetMapping("/api/inventory/{id}")
    ItemResponse getItemById(@PathVariable Long id);

    @PutMapping("/api/inventory/{id}/deduct")
    void deductQuantity(@PathVariable Long id, @RequestParam int qty);
}
```

### Gateway Route Config (application.yml):
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/auth/**
        - id: inventory-service
          uri: lb://inventory-service
          predicates:
            - Path=/api/inventory/**
        - id: request-service
          uri: lb://request-service
          predicates:
            - Path=/api/requests/**
```

### axiosConfig.js (frontend):
```javascript
import axios from 'axios';
const api = axios.create({ baseURL: 'http://localhost:8080' });
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
export default api;
```

### Dockerfile (every microservice — multi-stage):
```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Jenkinsfile (place in project root):
```groovy
pipeline {
  agent any
  stages {
    stage('Checkout') { steps { git branch: 'main', url: 'YOUR_REPO_URL' } }
    stage('Build')    { steps { sh 'mvn clean package -DskipTests' } }
    stage('Test')     { steps { sh 'mvn test' } }
    stage('Docker Build') { steps { sh 'docker compose build' } }
    stage('Deploy')   { steps { sh 'docker compose up -d' } }
  }
}
```

---

## 9. application.yml Templates

### Eureka Server
```yaml
server:
  port: 8761
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
  instance:
    lease-renewal-interval-in-seconds: 5   # faster registration in dev
```

### Auth Service
```yaml
server:
  port: 8081
spring:
  application:
    name: auth-service
  datasource:
    url: jdbc:mysql://localhost:3306/auth_db  # use 'mysql' instead of 'localhost' in Docker
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

> Apply same pattern for inventory-service (port 8082, inventory_db) and request-service (port 8083, request_db).

### docker-compose.yml (abbreviated; agent must expand):
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment: { MYSQL_ROOT_PASSWORD: root }
    ports: ["3306:3306"]
    volumes: ["mysql-data:/var/lib/mysql"]
  eureka-server:
    build: ./eureka-server
    ports: ["8761:8761"]
  config-server:
    build: ./config-server
    ports: ["8888:8888"]
    depends_on: [eureka-server]
  api-gateway:
    build: ./api-gateway
    ports: ["8080:8080"]
    depends_on: [eureka-server]
  auth-service:
    build: ./auth-service
    depends_on: [mysql, eureka-server]
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/auth_db
  inventory-service:
    build: ./inventory-service
    depends_on: [mysql, eureka-server]
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/inventory_db
  request-service:
    build: ./request-service
    depends_on: [mysql, eureka-server]
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/request_db
  frontend:
    build: ./frontend
    ports: ["3000:80"]
    depends_on: [api-gateway]
volumes:
  mysql-data:
```

---

## 10. Day-by-Day Implementation Plan

> Each day has **Tasks**, **Acceptance Criteria**, and **Do Not Move On Until** checkpoints.

---

### DAY 1 — Infrastructure Backbone + Auth Service

**Goal:** Services register with Eureka. Register + Login endpoints return valid JWTs.

#### Morning (2–3 hours): Project Skeleton & Infrastructure

**Tasks:**
1. Create root directory `stationery-management-system/`
2. Create parent `pom.xml` with all 6 modules + Spring Cloud BOM
3. Scaffold `eureka-server`:
   - Add dependency: `spring-cloud-starter-netflix-eureka-server`
   - `@EnableEurekaServer` on main class
   - `application.yml`: port 8761, disable self-registration
4. Scaffold `config-server`:
   - Add dependency: `spring-cloud-config-server`
   - `@EnableConfigServer` on main class
   - Port 8888, point to a local git folder for configs (or use native profile)
5. Scaffold `api-gateway`:
   - Add dependency: `spring-cloud-starter-gateway`
   - Port 8080
   - Add route config in `application.yml` (3 routes: auth, inventory, request)
6. Start Eureka and verify dashboard at `http://localhost:8761`

**Acceptance Criteria:**
- [ ] `mvn clean install` at root succeeds (all modules compile)
- [ ] Eureka dashboard loads at `http://localhost:8761`
- [ ] Gateway starts without error on port 8080

---

#### Afternoon (3–4 hours): Auth Service

**Tasks:**
1. Scaffold `auth-service`, add dependencies: web, data-jpa, security, validation, eureka-client, lombok, mysql-connector, springdoc
2. Add jjwt dependencies (api + impl + jackson, version 0.12.x)
3. Create `User.java` entity with all fields from schema (Section 5 above)
4. Create `UserRepository` extending `JpaRepository<User, Long>` with `findByEmail(String email)`
5. Create `JwtUtil.java` with generate/validate/extract methods
6. Create `JwtAuthFilter.java` extending `OncePerRequestFilter`
7. Create `SecurityConfig.java` — permit `/api/auth/**`, stateless session
8. Create `AuthService` interface + `AuthServiceImpl`:
   - `register()`: check email uniqueness → BCrypt password → save → return JWT
   - `login()`: find by email → validate password → return JWT
9. Create `AuthController` with `POST /api/auth/register` and `POST /api/auth/login`
10. Add Eureka client config in `application.yml`
11. Test with Postman: register → login → decode JWT → verify role claim exists

**Acceptance Criteria:**
- [ ] Auth service appears in Eureka dashboard
- [ ] `POST /api/auth/register` returns 200 with token
- [ ] `POST /api/auth/login` returns 200 with token
- [ ] JWT contains `email` and `role` claims
- [ ] Duplicate email returns 409 (handled by GlobalExceptionHandler)

**❌ Do Not Move On Until:** JWT decode shows correct role. Eureka shows `AUTH-SERVICE` registered.

---

### DAY 2 — Inventory Service + Request Service + Feign

**Goal:** Full CRUD for items. Students can submit requests. Admins can approve (stock deducts).

#### Morning (3 hours): Inventory Service

**Tasks:**
1. Scaffold `inventory-service` with: web, data-jpa, security, validation, eureka-client, lombok, mysql-connector, springdoc
2. Create `StationeryItem.java` entity with all fields from schema
3. Create `StationeryRepository` with:
   - `findByCategory(String category)`
   - `findByNameContainingIgnoreCase(String name)` (for search)
   - `findByAvailableQuantityLessThan(int threshold)` (for low-stock)
4. Create `StationeryService` interface + `StationeryServiceImpl`:
   - `addItem(ItemRequest)`, `getAllItems(Pageable)`, `updateItem(Long, ItemRequest)`, `deleteItem(Long)`, `getById(Long)`, `deductQuantity(Long, int)`, `searchItems(String)`, `getLowStockItems()`
5. Create `StationeryController` — all endpoints from Section 7
6. Add `@Valid` validation on all DTOs
7. Add `GlobalExceptionHandler` — handle `ItemNotFoundException`, `InsufficientStockException`
8. Add JWT filter (same pattern as auth-service — validate token, set SecurityContext)
9. Register with Eureka

**Acceptance Criteria:**
- [ ] `GET /api/inventory` returns paginated list (via Gateway)
- [ ] Admin token can POST/PUT/DELETE; student token gets 403
- [ ] `GET /api/inventory/search?q=pen` returns filtered results
- [ ] Inventory service visible in Eureka

---

#### Afternoon (3–4 hours): Request Service + Feign

**Tasks:**
1. Scaffold `request-service` with all dependencies + `spring-cloud-starter-openfeign`
2. Add `@EnableFeignClients` to main class
3. Create `StationeryRequest.java` and `RequestItem.java` entities with `@OneToMany` relationship
4. Create both repositories
5. Create `InventoryFeignClient` interface (see Section 8 for exact code)
6. Implement `RequestServiceImpl`:
   - `submitRequest()`: for each item in DTO, call Feign to validate item exists and has stock → save request + items
   - `approveRequest()`: update status → call Feign `deductQuantity` for each item
   - `rejectRequest()`: update status + save admin_comment
   - `getMyRequests(String email)`: filter by student_email
7. Create `RequestController` with all endpoints
8. Add `GlobalExceptionHandler`
9. Test complete flow: login as student → submit request → login as admin → approve → check inventory stock decreased

**Acceptance Criteria:**
- [ ] Student submits request → status is PENDING
- [ ] Admin approves → status is APPROVED, inventory stock decreases
- [ ] Admin rejects with comment → status is REJECTED, comment saved
- [ ] Student cannot access `/api/requests` (admin-only list) → 403

**❌ Do Not Move On Until:** Full request-approve-stock-deduction flow works end-to-end via Postman.

---

### DAY 3 — React Frontend

**Goal:** Complete SPA. All pages functional. Role-based routing works.

#### Morning (3 hours): Setup + Auth Pages

**Tasks:**
1. `npm create vite@latest frontend -- --template react` inside project root
2. `npm install axios react-router-dom@6 jwt-decode react-toastify react-hook-form tailwindcss`
3. Initialize Tailwind (`npx tailwindcss init`)
4. Create `src/api/axiosConfig.js` — baseURL to Gateway, JWT interceptor (see Section 8)
5. Create `src/context/AuthContext.jsx`:
   - State: `{ user, token, role }`
   - Functions: `login(token)` (decode + store), `logout()` (clear localStorage)
   - Export `useAuth()` hook
6. Create `src/components/ProtectedRoute.jsx`:
   - If no token → redirect to `/login`
   - If role mismatch → redirect to `/dashboard`
7. Create `LoginPage.jsx` — form with react-hook-form, call `POST /api/auth/login`, store token via AuthContext
8. Create `RegisterPage.jsx` — include role selector (ADMIN/STUDENT)
9. Configure React Router in `App.jsx`:
   - Public: `/login`, `/register`
   - Protected (any role): `/dashboard`, `/catalog`, `/my-requests`
   - Protected (ADMIN only): `/admin/inventory`, `/admin/requests`

**Acceptance Criteria:**
- [ ] Login with wrong credentials shows toast error
- [ ] After login, redirect to `/dashboard`
- [ ] Accessing `/admin/inventory` as student → redirected to `/dashboard`

---

#### Afternoon (3–4 hours): Feature Pages

**Tasks:**
1. `CatalogPage.jsx`:
   - `GET /api/inventory?page=0&size=20`
   - Category filter dropdown
   - Search bar → `GET /api/inventory/search?q=`
   - Display items as cards (ItemCard.jsx component)
   - "Add to Request" button → collects items + quantities into local state
   - Submit cart → `POST /api/requests`

2. `MyRequestsPage.jsx` (STUDENT):
   - `GET /api/requests/my`
   - Table showing request date, items, status (color-coded), admin comment

3. `ManageInventoryPage.jsx` (ADMIN):
   - Table of all items with Edit/Delete buttons
   - "Add Item" modal form
   - Edit modal pre-fills current values
   - Low-stock items highlighted in red

4. `ManageRequestsPage.jsx` (ADMIN):
   - Pending requests list
   - Expand row to see items requested
   - Approve button (calls `PUT /api/requests/{id}/approve`)
   - Reject button → opens comment input → calls `PUT /api/requests/{id}/reject`

5. `DashboardPage.jsx`:
   - Admin view: total items, pending request count, low-stock count (3 stat cards)
   - Student view: my pending requests count, my approved requests count

6. `Navbar.jsx`:
   - Show different links based on role
   - Logout button clears AuthContext + localStorage

**Acceptance Criteria:**
- [ ] Student can browse catalog, submit request, view status
- [ ] Admin can add/edit/delete inventory items
- [ ] Admin can approve/reject requests
- [ ] All actions show toast success/error
- [ ] Page refresh retains login state (token in localStorage)

**❌ Do Not Move On Until:** Full user flow (register → login → submit request → admin approves → student sees approved) works in browser.

---

### DAY 4 — Docker + Jenkins CI/CD

**Goal:** `docker compose up --build` starts everything. Jenkins pipeline is green.

#### Morning (3 hours): Dockerize All Services

**Tasks:**
1. Write `Dockerfile` in each of the 6 Java service directories using the multi-stage pattern (Section 8)
2. Write `frontend/Dockerfile`:
   ```dockerfile
   FROM node:18-alpine AS build
   WORKDIR /app
   COPY package*.json .
   RUN npm install
   COPY . .
   RUN npm run build

   FROM nginx:alpine
   COPY --from=build /app/dist /usr/share/nginx/html
   COPY nginx.conf /etc/nginx/conf.d/default.conf
   EXPOSE 80
   ```
3. Create `frontend/nginx.conf`:
   ```nginx
   server {
     listen 80;
     root /usr/share/nginx/html;
     index index.html;
     location / { try_files $uri /index.html; }
     location /api { proxy_pass http://api-gateway:8080; }
   }
   ```
4. Create `ci-cd/docker-compose.yml` (full version from Section 9 above)
   - **Important:** in Docker network, use service name `mysql` not `localhost` in JDBC URLs
   - All services depend on `eureka-server`; business services also depend on `mysql`
5. Run `docker compose up --build` from `ci-cd/` directory
6. Wait ~60 seconds for Eureka registration, then test all endpoints

**Acceptance Criteria:**
- [ ] All 8 containers start (mysql, eureka, config, gateway, auth, inventory, request, frontend)
- [ ] Eureka dashboard at `http://localhost:8761` shows 5 registered services
- [ ] Full flow works via `http://localhost:8080`
- [ ] Frontend loads at `http://localhost:3000`

---

#### Afternoon (2–3 hours): Jenkins Pipeline

**Tasks:**
1. Start Jenkins: `docker run -d -p 8085:8080 -v /var/run/docker.sock:/var/run/docker.sock jenkins/jenkins:lts`
2. Install plugins: **Docker Pipeline**, **Maven Integration**, **Git**
3. Configure Maven and JDK in Jenkins Global Tool Configuration
4. Create `Jenkinsfile` in project root (see Section 8)
5. Create Jenkins Pipeline job → "Pipeline from SCM" → point to Git repo
6. Push code to GitHub, trigger build
7. Fix any pipeline failures (common: Maven not found in PATH, Docker socket permission)

**Acceptance Criteria:**
- [ ] All 5 pipeline stages complete green
- [ ] `mvn test` stage runs without failures
- [ ] Docker images built successfully
- [ ] Services start via `docker compose up -d`

**❌ Do Not Move On Until:** Jenkins shows a full green build.

---

### DAY 5 — Testing + Polish + Documentation

**Goal:** ≥70% test coverage. Swagger works. README is complete. Project is presentable.

#### Morning (3 hours): Unit Tests

**Write tests for each service layer:**

**AuthServiceImplTest:**
- `register_success_whenEmailNotTaken`
- `register_throwsException_whenEmailAlreadyExists`
- `login_success_withValidCredentials`
- `login_throwsException_withWrongPassword`

**StationeryServiceImplTest:**
- `addItem_savesAndReturnsItem`
- `getAllItems_returnsPaginatedList`
- `updateItem_throwsNotFound_whenIdInvalid`
- `deductQuantity_throwsException_whenInsufficientStock`

**RequestServiceImplTest:**
- `submitRequest_success_whenItemsAvailable`
- `submitRequest_throwsException_whenItemNotFound` (mock Feign)
- `approveRequest_updatesStatusAndDeductsStock`
- `rejectRequest_savesAdminComment`

**Test Setup Pattern:**
```java
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
  @Mock UserRepository userRepository;
  @Mock PasswordEncoder passwordEncoder;
  @Mock JwtUtil jwtUtil;
  @InjectMocks AuthServiceImpl authService;
  // tests...
}
```

Run: `mvn test` from root. Add JaCoCo plugin to each service pom.xml, run `mvn verify` to see coverage report at `target/site/jacoco/index.html`.

**Acceptance Criteria:**
- [ ] ≥70% line coverage on service layer in all 3 business services
- [ ] All tests pass with `mvn test`
- [ ] No test relies on a real database (all repos are mocked)

---

#### Afternoon (3 hours): Final Polish + Documentation

**Tasks:**
1. Test Swagger UI for each service: `http://localhost:808X/swagger-ui.html`
2. Full end-to-end manual test:
   - Register admin → login → add 3 inventory items
   - Register student → login → browse catalog → submit request for 2 items
   - Login as admin → approve request → verify stock decreased
   - Login as student → see status = APPROVED
3. Write `README.md`:
   - Project overview (2 paragraphs)
   - Architecture diagram (ASCII or link)
   - Prerequisites list
   - How to run locally (step-by-step commands)
   - How to run with Docker Compose
   - Environment variables table
   - API endpoints table (point to Swagger)
   - Known issues / future improvements
4. Clean up: remove unused imports, ensure consistent naming conventions
5. Add Javadoc on all public service methods

**Final Deliverables Checklist:**
- [ ] All 6 backend services start and register with Eureka
- [ ] JWT authentication + RBAC working
- [ ] Inventory CRUD complete with validation
- [ ] Full request lifecycle (submit → approve/reject → stock deduction)
- [ ] Feign inter-service communication working
- [ ] React frontend — all pages functional, role-based routing
- [ ] Docker: all services containerized, compose starts everything
- [ ] Jenkins: pipeline green
- [ ] Unit tests: ≥70% coverage
- [ ] Input validation + GlobalExceptionHandler in all services
- [ ] Pagination on inventory listing
- [ ] Swagger UI accessible for each service
- [ ] README.md complete

---

## 11. Critical Pitfalls — Read Before Writing Code

| Pitfall | Prevention |
|---------|-----------|
| Spring Boot / Spring Cloud version mismatch | Use exactly Boot 3.2.5 + Cloud 2023.0.1 (Leyton). Do NOT mix. Check compatibility at spring.io |
| Eureka 503 on startup | Services take ~30s to register. Add `lease-renewal-interval-in-seconds: 5` for dev. Just wait. |
| CORS errors in browser | Configure CORS **only** in the Gateway (`CorsWebFilter` bean). Do NOT add it in individual services — they will conflict. |
| JWT secret mismatch | Use the SAME secret string in Auth Service and Gateway JWT filter. Store in Config Server or env var. Min 256-bit. |
| Cross-DB JOINs not possible | You CANNOT join `auth_db.users` with `request_db.stationery_requests`. Denormalize `student_email` into requests table. Use Feign for cross-service data. |
| Feign client name mismatch | `@FeignClient(name = "inventory-service")` must exactly match `spring.application.name: inventory-service` in the target service config. |
| Docker `localhost` in JDBC URLs | Inside Docker Compose, use the service name: `jdbc:mysql://mysql:3306/auth_db` NOT `jdbc:mysql://localhost:3306/auth_db` |
| Maven multi-module build order | Run `mvn clean install` from the **root directory** only. Never build a module in isolation before its parent is installed. |
| Frontend Vite vs CRA | If using Vite (recommended), the build output is `dist/` not `build/`. Update `Dockerfile` accordingly. |
| Feign + Security | Feign calls between services may need to pass the JWT token in the header. Add a `RequestInterceptor` bean to forward the token. |

---

## 12. Environment Variables Reference

| Variable | Used In | Example Value |
|----------|---------|---------------|
| `SPRING_DATASOURCE_URL` | All business services | `jdbc:mysql://mysql:3306/auth_db` |
| `SPRING_DATASOURCE_USERNAME` | All business services | `root` |
| `SPRING_DATASOURCE_PASSWORD` | All business services | `root` |
| `JWT_SECRET` | Auth Service, Gateway | `mySuper256BitSecretKeyForJWTSigningAndValidation!` |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | All services | `http://eureka-server:8761/eureka/` |
| `MYSQL_ROOT_PASSWORD` | MySQL container | `root` |

---

## 13. Tooling Recommendations (Replacing Antigravity)

> You mentioned using **Antigravity** — below are purpose-built, widely supported alternatives for each concern:

### For Running This Project Locally
| Need | Recommended Tool | Why |
|------|-----------------|-----|
| Java version management | **SDKMAN** (sdkman.io) | Switch Java versions instantly: `sdk install java 17.0.10-tem` |
| Node version management | **nvm** (github.com/nvm-sh/nvm) | `nvm install 18 && nvm use 18` |
| Local Kubernetes (if Docker Compose feels limiting) | **Minikube** or **Kind** | Run full k8s locally for production-like environment |
| API testing | **Postman** or **Bruno** (open-source) | Bruno is local-first, no account needed |
| DB GUI | **DBeaver** (free) | Connect to all 3 MySQL databases simultaneously |
| IDE | **IntelliJ IDEA Community** | Best Java microservices support, free tier sufficient |

### For CI/CD (Jenkins Alternatives)
| Tool | Best For | Notes |
|------|---------|-------|
| **Jenkins** (as per blueprint) | Full control, self-hosted | Runs in Docker on :8085 |
| **GitHub Actions** | Simpler setup if on GitHub | Free for public repos, no separate server needed |
| **GitLab CI** | If self-hosting Git | Built-in CI, no Jenkins needed |

### For Container Orchestration (Beyond Docker Compose)
| Tool | When to Use |
|------|------------|
| **Docker Compose** | Local development (use this for the capstone) |
| **Minikube + Helm** | If you want to simulate production Kubernetes |
| **Railway / Render** | Deploy the whole stack to cloud cheaply for demo day |

### For Monitoring (Nice to Have on Day 5)
| Tool | Purpose |
|------|---------|
| **Spring Boot Actuator** | Already in dependencies — exposes `/actuator/health` |
| **Prometheus + Grafana** | Scrape Actuator metrics, visualize dashboards |
| **Zipkin** | Distributed tracing across microservices (add `spring-cloud-starter-zipkin`) |

---

## 14. Agent Self-Check Before Each Task

Before implementing any component, answer these:
1. **Which service does this belong to?** (Auth / Inventory / Request / Gateway / Frontend)
2. **Does it touch the database?** If yes, use JPA entity + repository pattern.
3. **Does it call another service?** If yes, use Feign client — never call RestTemplate directly.
4. **Is it secured?** Add role check in SecurityConfig or use `@PreAuthorize("hasRole('ADMIN')")`.
5. **Does it need validation?** Add `@Valid` on controller params + `@NotBlank`/`@Min` on DTO.
6. **Did I handle the error?** Add exception type to GlobalExceptionHandler with proper HTTP status.
7. **Is there a test for it?** Write the test in the same service's test directory.

---

*End of AGENT.md — This file contains everything needed to implement the Stationery Management System from scratch. Follow the day-wise plan in order. Do not skip days.*
