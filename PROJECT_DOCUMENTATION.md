# Smart Hospital Queue Management System — Project Documentation

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Technology Stack](#2-technology-stack)
3. [System Architecture](#3-system-architecture)
4. [Project Structure](#4-project-structure)
5. [Database Design](#5-database-design)
6. [Backend — Spring Boot](#6-backend--spring-boot)
7. [Frontend — Angular](#7-frontend--angular)
8. [Authentication & Authorization](#8-authentication--authorization)
9. [Real-Time Features (WebSocket)](#9-real-time-features-websocket)
10. [Queue Management Logic](#10-queue-management-logic)
11. [API Reference](#11-api-reference)
12. [Setup & Installation](#12-setup--installation)
13. [Environment Configuration](#13-environment-configuration)
14. [Default Accounts](#14-default-accounts)
15. [Role-Based Workflows](#15-role-based-workflows)

---

## 1. Project Overview

**SmartApp** is a full-stack **Hospital Queue Management System** built to digitize and streamline patient flow in a clinical environment. It replaces manual, paper-based queuing with a real-time, priority-driven digital system accessible to all stakeholders — administrators, receptionists, doctors, and patients.

### Core Capabilities

- **Multi-role access**: Admin, Receptionist, Doctor, and Patient (self-service) portals
- **Priority queue engine**: EMERGENCY > URGENT > NORMAL, with FIFO tie-breaking within the same priority
- **Real-time updates**: WebSocket-powered live queue tracking for doctors and patients
- **Token system**: Auto-generated tokens with department prefix and daily sequence reset
- **Self-service portal**: Patients can register and track their queue position without staff assistance

---

## 2. Technology Stack

| Layer | Technology | Version |
|---|---|---|
| Frontend Framework | Angular | 16.2.0 |
| Frontend Language | TypeScript | 5.1.3 |
| Reactive Streams | RxJS | 7.8.0 |
| WebSocket Client | STOMP.js + SockJS | — |
| Backend Framework | Spring Boot | 3.1.5 |
| Backend Language | Java | 17 |
| Security | Spring Security + JWT (JJWT) | 0.11.5 |
| ORM | Spring Data JPA (Hibernate) | — |
| Real-Time (Server) | Spring WebSocket (STOMP) | — |
| Database | MySQL | 8+ |
| Build Tool (Backend) | Maven | — |
| Build Tool (Frontend) | Angular CLI | 16.2.0 |
| Version Control | Git | — |

---

## 3. System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    BROWSER (Client)                      │
│  ┌───────────────────────────────────────────────────┐  │
│  │              Angular 16 SPA                       │  │
│  │  ┌──────────┐ ┌──────────┐ ┌────────────────────┐│  │
│  │  │  Auth    │ │  Guards  │ │  JWT Interceptor   ││  │
│  │  │  Module  │ │ (Role/   │ │  (Bearer token on  ││  │
│  │  │          │ │  Auth)   │ │   every request)   ││  │
│  │  └──────────┘ └──────────┘ └────────────────────┘│  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐          │  │
│  │  │  Admin   │ │  Doctor  │ │Recept-   │          │  │
│  │  │  Module  │ │  Module  │ │ionist   │          │  │
│  │  └──────────┘ └──────────┘ └──────────┘          │  │
│  │  ┌──────────────────────────────────────────────┐ │  │
│  │  │       Patient Self-Service Module            │ │  │
│  │  └──────────────────────────────────────────────┘ │  │
│  └───────────────────────────────────────────────────┘  │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP/REST + WebSocket (STOMP/SockJS)
                       ▼
┌─────────────────────────────────────────────────────────┐
│              Spring Boot Backend (:8081)                  │
│  ┌──────────────────────────────────────────────────┐   │
│  │              Spring Security Layer               │   │
│  │  JWT Filter → Role-Based Access Control         │   │
│  └──────────────────────┬───────────────────────────┘   │
│                         │                               │
│  ┌──────────┐ ┌────────┐ ┌────────────┐ ┌──────────┐   │
│  │  Auth    │ │ Admin  │ │ Recept-    │ │ Doctor   │   │
│  │Controller│ │Ctrl    │ │ ionist     │ │ Ctrl     │   │
│  └──────────┘ └────────┘ └────────────┘ └──────────┘   │
│  ┌─────────────────────────────────────────────────┐   │
│  │        Patient Controller (Public)               │   │
│  └─────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────┐   │
│  │           Service Layer (Business Logic)         │   │
│  │  QueueService | DoctorService | TokenService    │   │
│  └──────────────────────┬──────────────────────────┘   │
│                         │                               │
│  ┌──────────────────────▼──────────────────────────┐   │
│  │              Spring Data JPA                     │   │
│  │  UserRepo | DoctorRepo | TokenRepo | PatientRepo│   │
│  └──────────────────────┬──────────────────────────┘   │
└──────────────────────────┼──────────────────────────────┘
                           │ JDBC
                           ▼
              ┌────────────────────────┐
              │   MySQL 8 Database     │
              │   (hospital_queue)     │
              └────────────────────────┘
```

### Design Patterns

| Pattern | Where Used | Purpose |
|---|---|---|
| Layered Architecture | Backend | Controllers → Services → Repositories → DB |
| DTO Pattern | Backend | Separate request/response objects from entities |
| Repository Pattern | Backend | Spring Data JPA repositories abstract data access |
| Pessimistic Locking | Backend | `SELECT FOR UPDATE` on Doctor row during token generation |
| Lazy Loading | Frontend | Angular feature modules loaded on demand |
| Interceptor Pattern | Frontend | JWT added to all outgoing HTTP requests |
| Guard Pattern | Frontend | Route access restricted by auth state and role |
| Observable Pattern | Frontend | RxJS for all async operations |

---

## 4. Project Structure

### Root Layout

```
SmartApp - Copy/
├── backend/                    Spring Boot application
├── frontend/                   Angular application
├── API_Documentation.md        Full REST API reference
├── API_Documentation.html      HTML version of API docs
├── API_Documentation.docx      Word version of API docs
├── HOW_TO_RUN.md               Quick-start guide
└── PROJECT_DOCUMENTATION.md    This file
```

### Backend Structure

```
backend/
├── pom.xml
└── src/main/
    ├── java/com/hospital/queue/
    │   ├── HospitalQueueApplication.java
    │   ├── config/
    │   │   ├── SecurityConfig.java         Spring Security + JWT setup
    │   │   ├── WebSocketConfig.java        STOMP WebSocket configuration
    │   │   └── DataInitializer.java        Seeds default admin account
    │   ├── controller/
    │   │   ├── AuthController.java         POST /api/auth/login
    │   │   ├── AdminController.java        /api/admin/**
    │   │   ├── DoctorController.java       /api/doctor/**
    │   │   ├── ReceptionistController.java /api/receptionist/**
    │   │   └── PatientController.java      /api/patient/** and /api/public/**
    │   ├── service/
    │   │   ├── AuthService.java
    │   │   ├── JwtService.java
    │   │   ├── QueueService.java           Core queue ordering logic
    │   │   ├── DoctorService.java
    │   │   ├── ReceptionistService.java
    │   │   ├── PatientService.java
    │   │   └── AdminService.java
    │   ├── entity/
    │   │   ├── User.java
    │   │   ├── Doctor.java
    │   │   ├── Department.java
    │   │   ├── Patient.java
    │   │   └── Token.java
    │   ├── repository/
    │   │   ├── UserRepository.java
    │   │   ├── DoctorRepository.java
    │   │   ├── PatientRepository.java
    │   │   ├── DepartmentRepository.java
    │   │   └── TokenRepository.java
    │   ├── dto/
    │   │   ├── request/                    LoginRequest, CreateUserRequest, etc.
    │   │   └── response/                   LoginResponse, TokenResponse, etc.
    │   ├── security/
    │   │   ├── JwtAuthFilter.java
    │   │   └── UserDetailsServiceImpl.java
    │   ├── enums/
    │   │   ├── Role.java                   ADMIN, RECEPTIONIST, DOCTOR
    │   │   ├── Priority.java               EMERGENCY, URGENT, NORMAL
    │   │   ├── TokenStatus.java            WAITING, IN_PROGRESS, COMPLETED
    │   │   └── RegistrationMode.java       SELF, RECEPTIONIST
    │   ├── exception/
    │   │   ├── BusinessException.java
    │   │   ├── ResourceNotFoundException.java
    │   │   └── GlobalExceptionHandler.java
    │   └── util/
    │       └── SecurityUtils.java
    └── resources/
        └── application.properties
```

### Frontend Structure

```
frontend/
├── package.json
├── angular.json
├── tsconfig.json
└── src/
    ├── index.html
    ├── main.ts
    ├── styles.css
    ├── environments/
    │   └── environment.ts              API_URL and WS_URL constants
    └── app/
        ├── app.module.ts
        ├── app.component.ts
        ├── app-routing.module.ts       Lazy-loaded module routes
        ├── core/
        │   ├── services/
        │   │   ├── auth.service.ts
        │   │   ├── admin.service.ts
        │   │   ├── doctor.service.ts
        │   │   ├── receptionist.service.ts
        │   │   ├── patient.service.ts
        │   │   └── websocket.service.ts
        │   ├── guards/
        │   │   ├── auth.guard.ts
        │   │   └── role.guard.ts
        │   ├── interceptors/
        │   │   └── jwt.interceptor.ts
        │   └── models/
        │       └── index.ts            All TypeScript interfaces
        └── modules/
            ├── auth/
            │   └── login/
            ├── admin/
            │   ├── layout/
            │   ├── dashboard/
            │   ├── users/
            │   ├── departments/
            │   └── doctors/
            ├── doctor/
            │   ├── layout/
            │   └── queue/
            ├── receptionist/
            │   ├── layout/
            │   ├── register-patient/
            │   ├── generate-token/
            │   └── queue-manage/
            └── patient/
                ├── home/
                ├── get-token/
                └── track/
```

---

## 5. Database Design

**Database Name:** `hospital_queue`

### Entity Relationship Overview

```
Department ──< Doctor >── User
                 │
                 └──< Token >── Patient
                                    │
                                 User (optional, for self-registered)
```

### Tables

#### `users`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| name | VARCHAR | Full name |
| email | VARCHAR UNIQUE | Used as login username |
| password | VARCHAR | BCrypt-hashed |
| role | ENUM | ADMIN, RECEPTIONIST, DOCTOR |
| active | BOOLEAN | Account enabled/disabled |
| created_at | TIMESTAMP | Auto-set on insert |

#### `departments`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| name | VARCHAR UNIQUE | Department name |
| prefix | VARCHAR | 2–5 char token prefix (e.g., CARD) |
| description | VARCHAR | Optional description |

#### `doctors`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| user_id | BIGINT FK | References `users.id` |
| department_id | BIGINT FK | References `departments.id` |
| specialization | VARCHAR | Doctor's specialization |
| active | BOOLEAN | Active/inactive status |
| token_counter | INT | Daily sequence counter (reset daily) |
| last_token_date | DATE | Date of last token generation |

#### `patients`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| name | VARCHAR | Patient name |
| phone | VARCHAR | Contact number |
| email | VARCHAR | Optional |
| age | INT | Optional |
| gender | VARCHAR | Optional |
| registration_mode | ENUM | SELF, RECEPTIONIST |
| created_at | TIMESTAMP | Auto-set on insert |

#### `tokens`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| token_number | VARCHAR UNIQUE | e.g., CARD-20260422-001 |
| patient_id | BIGINT FK | References `patients.id` |
| doctor_id | BIGINT FK | References `doctors.id` |
| priority | ENUM | EMERGENCY, URGENT, NORMAL |
| status | ENUM | WAITING, IN_PROGRESS, COMPLETED |
| registration_mode | ENUM | SELF, RECEPTIONIST |
| created_at | TIMESTAMP | Used for FIFO tie-breaking |
| updated_at | TIMESTAMP | Last status change |

### Token Number Format

```
{DEPT_PREFIX}-{YYYYMMDD}-{3-digit sequence}

Example: CARD-20260422-001
          ^^^^  ^^^^^^^^  ^^^
          Dept   Date     Seq#
```

The sequence counter resets to 1 each new calendar day per doctor.

---

## 6. Backend — Spring Boot

### Configuration (`application.properties`)

```properties
server.port=8081

spring.datasource.url=jdbc:mysql://localhost:3306/hospital_queue
spring.datasource.username=root
spring.datasource.password=root

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

jwt.secret=<hex-encoded-secret>
jwt.expiration=86400000

spring.web.cors.allowed-origins=http://localhost:4200
```

### Security Configuration

Spring Security is configured as **stateless** (no sessions). Every request must carry a valid JWT in the `Authorization: Bearer <token>` header.

**Public endpoints (no auth required):**
- `POST /api/auth/login`
- `GET /api/public/**`
- `POST /api/patient/register`
- `POST /api/patient/generate-token`
- `GET /api/patient/token/**`
- `GET /api/patient/queue-position/**`

**Protected endpoints** require valid JWT + matching role via Spring Security annotations (`@PreAuthorize`).

### WebSocket Configuration

- **Endpoint:** `/ws` (SockJS fallback enabled)
- **Broker prefix:** `/topic`
- **App destination prefix:** `/app`

Clients subscribe to topics after connecting:
- `/topic/queue/{doctorId}` — Doctor's real-time queue updates
- `/topic/token/{tokenNumber}` — Patient's real-time token status

### Exception Handling

`GlobalExceptionHandler` catches and returns structured JSON for:
- `ResourceNotFoundException` → 404
- `BusinessException` → 400
- `AccessDeniedException` → 403
- Unhandled exceptions → 500

### Concurrent Token Generation

To prevent duplicate token numbers under concurrent load, `QueueService` uses **pessimistic write locking** on the Doctor entity row (`SELECT ... FOR UPDATE`) before incrementing the token counter.

---

## 7. Frontend — Angular

### Routing

```
/                       → redirects to /login
/login                  → LoginComponent (no auth)
/admin/**               → AdminModule (role: ADMIN)
  /admin/dashboard
  /admin/users
  /admin/departments
  /admin/doctors
/doctor/**              → DoctorModule (role: DOCTOR)
  /doctor/queue
/receptionist/**        → ReceptionistModule (role: RECEPTIONIST)
  /receptionist/register-patient
  /receptionist/generate-token
  /receptionist/queue
/patient                → PatientModule (no auth)
  /patient/get-token
  /patient/track
```

All authenticated routes are protected by `AuthGuard` (checks token existence) and `RoleGuard` (checks user role).

### Core Services

| Service | Responsibility |
|---|---|
| `AuthService` | Login, logout, JWT storage in localStorage |
| `AdminService` | HTTP calls to `/api/admin/**` |
| `DoctorService` | HTTP calls to `/api/doctor/**` |
| `ReceptionistService` | HTTP calls to `/api/receptionist/**` |
| `PatientService` | HTTP calls to `/api/patient/**` and `/api/public/**` |
| `WebSocketService` | STOMP connection lifecycle, subscribe/unsubscribe |

### JWT Storage (localStorage)

| Key | Value |
|---|---|
| `hq_token` | Raw JWT string |
| `hq_user` | JSON-serialized user object (id, name, email, role) |

The `JwtInterceptor` reads `hq_token` and injects `Authorization: Bearer <token>` into every outgoing HTTP request.

### Module Lazy Loading

All feature modules are lazy-loaded via Angular's `loadChildren` syntax, reducing initial bundle size and improving startup performance.

---

## 8. Authentication & Authorization

### Login Flow

```
1. User submits email + password to POST /api/auth/login
2. Backend validates credentials with BCrypt
3. On success: returns JWT (24hr expiry) + user info (id, name, email, role)
4. Frontend stores JWT in localStorage (hq_token) and user in localStorage (hq_user)
5. JwtInterceptor attaches token to all subsequent requests
6. RoleGuard reads role from localStorage to restrict navigation
```

### JWT Token Structure

```
Header:  { "alg": "HS256", "typ": "JWT" }
Payload: { "sub": "user@email.com", "role": "DOCTOR", "iat": ..., "exp": ... }
```

### Roles and Permissions

| Action | ADMIN | RECEPTIONIST | DOCTOR | Patient (Public) |
|---|:---:|:---:|:---:|:---:|
| Create users | ✓ | — | — | — |
| Manage departments | ✓ | — | — | — |
| Manage doctors | ✓ | — | — | — |
| View all active tokens | ✓ | ✓ | — | — |
| Register patients | — | ✓ | — | ✓ (self) |
| Generate tokens | — | ✓ | — | ✓ (NORMAL only) |
| Update token priority | — | ✓ | — | — |
| View personal queue | — | — | ✓ | — |
| Call next patient | — | — | ✓ | — |
| Complete consultation | — | — | ✓ | — |
| Track own token | — | — | — | ✓ |

---

## 9. Real-Time Features (WebSocket)

### Architecture

Spring WebSocket uses STOMP over SockJS. When a browser doesn't support native WebSocket, SockJS automatically falls back to long-polling.

### Connection Flow

```
Frontend (WebSocketService)
  └─→ Connect to ws://localhost:8081/ws via SockJS
      └─→ STOMP handshake
          └─→ Subscribe to /topic/queue/{doctorId}   (Doctor module)
              Subscribe to /topic/token/{tokenNumber} (Patient module)
```

### Server-Side Publishing

The backend publishes updates via `SimpMessagingTemplate`:

```java
// When queue changes (new token, status update, priority change)
messagingTemplate.convertAndSend("/topic/queue/" + doctorId, updatedQueue);

// When token status changes
messagingTemplate.convertAndSend("/topic/token/" + tokenNumber, tokenResponse);
```

### Update Triggers

| Event | Channel Updated |
|---|---|
| New token generated (receptionist or patient) | `/topic/queue/{doctorId}` |
| Doctor calls next patient | `/topic/queue/{doctorId}` + `/topic/token/{tokenNumber}` |
| Doctor marks token complete | `/topic/queue/{doctorId}` + `/topic/token/{tokenNumber}` |
| Receptionist updates priority | `/topic/queue/{doctorId}` |

### Polling Fallback (Patient Tracking)

The patient tracking component subscribes to WebSocket updates. If the WebSocket connection is unavailable, it falls back to HTTP polling every **15 seconds** to `/api/patient/token/{tokenNumber}` and `/api/patient/queue-position/{tokenNumber}`.

---

## 10. Queue Management Logic

### Priority Ordering

Tokens in a doctor's queue are ordered by:
1. **Priority** (ascending): EMERGENCY (1) → URGENT (2) → NORMAL (3)
2. **Created At** (ascending): Earlier tokens first within the same priority (FIFO)

This means an EMERGENCY token registered at 3:00 PM will appear before an URGENT token registered at 2:00 PM.

### Token Status Lifecycle

```
         ┌─────────┐
         │ WAITING │  ← Default when token is generated
         └────┬────┘
              │ Doctor clicks "Call Next"
              ▼
       ┌─────────────┐
       │ IN_PROGRESS │  ← One token max per doctor at a time
       └──────┬──────┘
              │ Doctor marks "Complete"
              ▼
        ┌───────────┐
        │ COMPLETED │  ← Terminal state
        └───────────┘
```

Only one token per doctor can be IN_PROGRESS at any time. Calling the next patient is blocked if there is already an IN_PROGRESS token.

### Priority Assignment Rules

| Source | Allowed Priorities |
|---|---|
| Patient (self-service) | NORMAL only (enforced server-side) |
| Receptionist (offline) | NORMAL, URGENT, EMERGENCY |
| Receptionist (post-generation edit) | NORMAL, URGENT, EMERGENCY |

---

## 11. API Reference

**Base URL:** `http://localhost:8081/api`

All protected endpoints require:
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

---

### Authentication

#### POST `/api/auth/login`
Login and receive a JWT token.

**Request:**
```json
{
  "email": "admin@hospital.com",
  "password": "Admin@123"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGci...",
  "userId": 1,
  "name": "System Admin",
  "email": "admin@hospital.com",
  "role": "ADMIN"
}
```

---

### Admin Endpoints (Role: ADMIN)

#### GET `/api/admin/users`
List all user accounts.

#### POST `/api/admin/users`
Create a new user (Receptionist).
```json
{ "name": "Jane Doe", "email": "jane@hospital.com", "password": "Pass@123", "role": "RECEPTIONIST" }
```

#### PUT `/api/admin/users/{id}/activate`
Activate a user account.

#### PUT `/api/admin/users/{id}/deactivate`
Deactivate a user account.

#### GET `/api/admin/departments`
List all departments.

#### POST `/api/admin/departments`
Create a department.
```json
{ "name": "Cardiology", "prefix": "CARD", "description": "Heart department" }
```

#### PUT `/api/admin/departments/{id}`
Update a department.

#### GET `/api/admin/doctors`
List all doctors.

#### POST `/api/admin/doctors`
Create a doctor (also auto-creates a linked User account).
```json
{
  "name": "Dr. Smith",
  "email": "smith@hospital.com",
  "password": "Pass@123",
  "departmentId": 1,
  "specialization": "Cardiologist"
}
```

#### PUT `/api/admin/doctors/{id}`
Update a doctor's details.

#### GET `/api/admin/tokens/active`
View all active (WAITING or IN_PROGRESS) tokens across all doctors.

---

### Receptionist Endpoints (Role: RECEPTIONIST)

#### POST `/api/receptionist/patients/register`
Register a patient offline.
```json
{ "name": "John Patient", "phone": "9876543210", "email": "john@email.com", "age": 35, "gender": "MALE" }
```

#### GET `/api/receptionist/doctors`
List all active doctors (for token generation).

#### GET `/api/receptionist/doctors/department/{departmentId}`
List doctors by department.

#### POST `/api/receptionist/tokens/generate`
Generate a token for a patient.
```json
{ "patientId": 1, "doctorId": 2, "priority": "URGENT" }
```

#### PUT `/api/receptionist/tokens/{tokenId}/priority`
Update a token's priority.
```json
{ "priority": "EMERGENCY" }
```

#### DELETE `/api/receptionist/tokens/{tokenId}`
Remove a token from the queue.

#### GET `/api/receptionist/tokens/active`
View all currently active tokens.

---

### Doctor Endpoints (Role: DOCTOR)

#### GET `/api/doctor/queue`
Get the authenticated doctor's current queue (ordered by priority + FIFO).

**Response:**
```json
[
  {
    "id": 5,
    "tokenNumber": "CARD-20260422-003",
    "patientName": "John Patient",
    "priority": "EMERGENCY",
    "status": "WAITING",
    "position": 1,
    "createdAt": "2026-04-22T10:30:00"
  }
]
```

#### POST `/api/doctor/tokens/call-next`
Move the next WAITING token to IN_PROGRESS.

#### PUT `/api/doctor/tokens/{tokenId}/status`
Update a token's status.
```json
{ "status": "COMPLETED" }
```

---

### Patient Endpoints (Public — No Auth)

#### POST `/api/patient/register`
Self-register a patient.
```json
{ "name": "Alice", "phone": "9123456789" }
```

#### GET `/api/public/departments`
List all departments.

#### GET `/api/public/doctors`
List all active doctors.

#### GET `/api/public/doctors/department/{departmentId}`
List doctors by department.

#### POST `/api/patient/generate-token`
Generate a token (always NORMAL priority).
```json
{ "patientId": 1, "doctorId": 2 }
```

#### GET `/api/patient/token/{tokenNumber}`
Get token details by token number.

#### GET `/api/patient/queue-position/{tokenNumber}`
Get current queue position.

**Response:**
```json
{
  "tokenNumber": "CARD-20260422-005",
  "status": "WAITING",
  "position": 3,
  "doctorName": "Dr. Smith"
}
```

---

## 12. Setup & Installation

### Prerequisites

| Tool | Version | Purpose |
|---|---|---|
| Java JDK | 17+ | Backend runtime |
| Maven | 3.8+ | Backend build |
| Node.js | 16+ | Frontend tooling |
| Angular CLI | 16.x | Frontend dev server |
| MySQL Server | 8+ | Database |

### Step 1: Database Setup

```sql
CREATE DATABASE hospital_queue;
```

> Hibernate auto-creates tables on first startup via `ddl-auto=update`.

### Step 2: Backend Configuration

Edit `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hospital_queue
spring.datasource.username=<your_mysql_user>
spring.datasource.password=<your_mysql_password>
```

### Step 3: Start the Backend

```bash
cd backend
mvn spring-boot:run
```

The API will be available at `http://localhost:8081`.

### Step 4: Start the Frontend

```bash
cd frontend
npm install
ng serve
```

The app will be available at `http://localhost:4200`.

### Step 5: Verify

Open `http://localhost:4200` and log in with the default admin account (see Section 14).

---

## 13. Environment Configuration

### Backend (`application.properties`)

| Property | Default | Description |
|---|---|---|
| `server.port` | `8081` | API server port |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/hospital_queue` | MySQL connection URL |
| `spring.datasource.username` | `root` | MySQL username |
| `spring.datasource.password` | `root` | MySQL password |
| `spring.jpa.hibernate.ddl-auto` | `update` | Schema auto-update |
| `jwt.expiration` | `86400000` | Token TTL (24 hours in ms) |
| `spring.web.cors.allowed-origins` | `http://localhost:4200` | CORS allowed origin |

### Frontend (`environment.ts`)

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8081/api',
  wsUrl: 'http://localhost:8081/ws'
};
```

---

## 14. Default Accounts

The `DataInitializer` component automatically creates these accounts on first startup if they do not exist:

| Role | Email | Password |
|---|---|---|
| Admin | `admin@hospital.com` | `Admin@123` |

Additional users must be created by the Admin via the Users management screen.

---

## 15. Role-Based Workflows

### Admin Workflow

1. Log in at `/login` with admin credentials
2. Navigate to **Departments** → Create departments with unique prefixes (e.g., CARD, ORTH, PEDI)
3. Navigate to **Doctors** → Create doctor profiles (auto-creates login accounts)
4. Navigate to **Users** → Create receptionist accounts, activate/deactivate as needed
5. Navigate to **Dashboard** → Monitor all active tokens system-wide

### Receptionist Workflow

1. Log in → routed to `/receptionist`
2. **Register Patient** → Enter patient name, phone, optional details
3. **Generate Token** → Select patient → select department → select doctor → set priority → submit
4. **Queue Management** → View all active tokens, update priority if clinical need arises, remove tokens if needed
5. Real-time view updates when doctors progress the queue

### Doctor Workflow

1. Log in → routed to `/doctor/queue`
2. View personal queue ordered by priority (EMERGENCY first, then URGENT, then NORMAL; FIFO within each group)
3. Click **Call Next** → moves top WAITING token to IN_PROGRESS; patient and receptionist see update instantly
4. Consult patient
5. Click **Complete** → marks token COMPLETED; queue advances; patient sees status update
6. Queue view updates in real-time via WebSocket subscription

### Patient Self-Service Workflow

1. Visit `/patient` (no login required)
2. **Get Token** → Enter name and phone → select department → select doctor → submit
3. System generates a NORMAL priority token and displays the token number
4. **Track Token** → Enter token number at `/patient/track`
5. View current queue position and status in real-time
6. Receive live updates as the doctor progresses through the queue
7. See status change to **IN_PROGRESS** when the doctor calls the patient's number

---

*Documentation generated for SmartApp — Smart Hospital Queue Management System*
*Last updated: 2026-04-22*
