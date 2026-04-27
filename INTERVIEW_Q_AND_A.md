# SmartApp - Hospital Queue Management System
## Interview Questions & Answers

**Level:** Senior Full-Stack Developer / Tech Lead  
**Duration:** 60-90 minutes  
**Topics:** Architecture, Security, Real-time Systems, Concurrency, Performance

---

## TABLE OF CONTENTS

1. [System Design & Architecture](#1-system-design--architecture)
2. [Backend - Spring Boot & Security](#2-backend---spring-boot--security)
3. [Frontend - Angular & RxJS](#3-frontend---angular--rxjs)
4. [Database & Concurrency](#4-database--concurrency)
5. [Real-Time Features (WebSocket)](#5-real-time-features-websocket)
6. [API Design & Integration](#6-api-design--integration)
7. [Security Deep Dive](#7-security-deep-dive)
8. [Performance & Scalability](#8-performance--scalability)
9. [Problem-Solving Scenarios](#9-problem-solving-scenarios)
10. [Behavioral & Situational](#10-behavioral--situational)

---

## 1. SYSTEM DESIGN & ARCHITECTURE

### Q1: Explain the overall architecture of SmartApp. Why was this architecture chosen?

**A:**

SmartApp uses a **Clean/Layered Architecture** with clear separation of concerns:

```
┌─────────────────────────────┐
│  Angular SPA (Frontend)     │
├─────────────────────────────┤
│  REST API + WebSocket       │
│  (Spring Boot Backend)      │
├─────────────────────────────┤
│  Service Layer (Business)   │
│  Repository Layer (Data)    │
├─────────────────────────────┤
│  MySQL Database             │
└─────────────────────────────┘
```

**Why this architecture:**

1. **Stateless Backend** - No server sessions needed; each request is independent
2. **Horizontal Scalability** - Can add multiple backend instances behind a load balancer
3. **Frontend Independence** - Angular SPA works offline and can be deployed separately
4. **Clear Responsibility** - Controllers handle HTTP, Services handle logic, Repositories handle data
5. **JWT Authentication** - No session management overhead; token travels with each request
6. **Real-time Capability** - WebSocket layer can be added without breaking REST APIs

**Trade-offs:**
- JWT tokens are larger than session cookies
- No server-side cache for real-time features
- Requires more client-side logic

---

### Q2: What are the key components and their responsibilities?

**A:**

| Component | Responsibilities |
|---|---|
| **SecurityConfig** | JWT filter, CORS, stateless sessions, role-based access |
| **JwtService** | Token generation, validation, expiration |
| **QueueService** | Queue ordering logic (priority + FIFO), token generation |
| **Controllers** | Route mapping, request validation, response formatting |
| **Repositories** | Database queries, pessimistic locking for concurrency |
| **Entities** | Data models with relationships and constraints |
| **DTOs** | Request/response objects (decouples API from entities) |
| **Angular Guards** | Route protection (AuthGuard, RoleGuard) |
| **Interceptors** | Automatic JWT injection, error handling |
| **WebSocketService** | Real-time connection lifecycle, subscriptions |

---

### Q3: How does the system handle multi-role access? Describe the access control model.

**A:**

SmartApp uses **Role-Based Access Control (RBAC)** with multiple enforcement layers:

**Layer 1: Spring Security Annotations**
```java
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasRole('RECEPTIONIST')")
@PreAuthorize("hasRole('DOCTOR')")
```

**Layer 2: Route Guards (Frontend)**
```typescript
{ path: 'admin', canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN'] } }
{ path: 'doctor', canActivate: [AuthGuard, RoleGuard], data: { roles: ['DOCTOR'] } }
```

**Layer 3: Service-Level Business Logic**
```java
// Enforce that only receptionist can assign EMERGENCY priority
if (!isReceptionist()) {
    priority = Priority.NORMAL;  // Force NORMAL for patients
}
```

**Role Permissions Matrix:**

| Action | Admin | Receptionist | Doctor | Patient |
|---|:---:|:---:|:---:|:---:|
| Create users | ✅ | — | — | — |
| Manage departments | ✅ | — | — | — |
| View all tokens | ✅ | ✅ | — | — |
| Generate tokens | — | ✅ | — | ✅ (NORMAL only) |
| Update priority | — | ✅ | — | — |
| Call next patient | — | — | ✅ | — |
| Track own token | — | — | — | ✅ |

---

## 2. BACKEND - SPRING BOOT & SECURITY

### Q4: Explain how JWT authentication works in this system. Why stateless?

**A:**

**JWT Flow:**

```
1. User logs in → POST /api/auth/login
                ↓
2. Backend validates email + password (BCrypt)
                ↓
3. Server generates JWT = Header.Payload.Signature
                ↓
4. Client stores JWT in localStorage
                ↓
5. Client sends in every request: Authorization: Bearer <JWT>
                ↓
6. JwtAuthFilter extracts & validates JWT
                ↓
7. If valid → SecurityContext populated, request proceeds
   If invalid/expired → 401 Unauthorized
```

**JWT Payload Example:**
```json
{
  "sub": "admin@hospital.com",
  "role": "ADMIN",
  "iat": 1713790000,
  "exp": 1713876400
}
```

**Why Stateless:**

| Stateless (JWT) | Stateful (Sessions) |
|---|---|
| No server memory needed | Server stores session data |
| Horizontal scalability ✅ | Requires sticky sessions ❌ |
| Distributed across servers ✅ | Hard to distribute |
| Larger payload (JWT) | Smaller payload (cookie) |
| Cannot revoke immediately | Can invalidate server-side |

**For hospital queue system:**
- Multiple backend instances needed for load handling
- JWT allows seamless scaling without session affinity
- Each API call is independent (no session state)

---

### Q5: What happens when a JWT token expires? How is this handled?

**A:**

**Expiration TTL:** 24 hours (86400000 ms) set in `application.properties`

**Validation in JwtService:**
```java
public boolean validateToken(String token) {
    try {
        Jws<Claims> claims = Jwts.parserBuilder()
            .setSigningKey(SignatureAlgorithm.HS256.getJcaName())
            .build()
            .parseClaimsJws(token);
        
        // Expiration automatically checked by JJWT library
        return true;
    } catch (ExpiredJwtException e) {
        // Token is expired
        return false;
    }
}
```

**Frontend Handling:**

1. **JwtInterceptor catches 401:**
```typescript
catchError((error) => {
    if (error.status === 401) {
        this.authService.logout();
        router.navigate(['/login']);
    }
    return throwError(error);
})
```

2. **User is logged out and redirected to login page**

**Issues & Workarounds:**

| Issue | Current | Better |
|---|---|---|
| User logged out mid-task | No warning | Add token expiration warning before expiry |
| No refresh token | Must re-login after 24h | Implement refresh token rotation |
| Token cannot be revoked | Token always valid until expiry | Add token blacklist + logout endpoint |

**Improvement Recommendation:**
Implement **refresh token pattern**:
- Short-lived access token (15 min)
- Long-lived refresh token (7 days, stored securely)
- Client auto-refreshes before expiry

---

### Q6: Explain the queue ordering logic. How does priority + FIFO work?

**A:**

**Ordering Algorithm:**

```java
public List<TokenResponse> getQueueForDoctor(Long doctorId) {
    return tokenRepository.findByDoctorIdAndStatusIn(doctorId, ACTIVE_STATUSES)
        .stream()
        .sorted(Comparator
            .comparing(Token::getPriority)           // Primary: Priority
            .thenComparing(Token::getCreatedAt))     // Secondary: FIFO
        .map(this::toResponse)
        .collect(toList());
}
```

**Example Queue:**

```
Time    | Patient | Priority | Status
--------|---------|----------|----------
10:00   | Alice   | EMERGENCY| WAITING   ← Position 1 (highest priority)
10:05   | Bob     | EMERGENCY| WAITING   ← Position 2 (same priority, earlier)
10:10   | Charlie | URGENT   | WAITING   ← Position 3
10:15   | Diana   | NORMAL   | WAITING   ← Position 4
10:20   | Eve     | NORMAL   | WAITING   ← Position 5
```

**Priority Order:**
- **EMERGENCY** (order=1) → Highest priority
- **URGENT** (order=2) → Medium priority
- **NORMAL** (order=3) → Lowest priority

**FIFO Within Same Priority:**
- Older tokens (earlier `createdAt`) move forward
- Prevents starvation of patients

**Special Case: In-Progress Token**
```java
public List<TokenResponse> getQueueForDoctor(Long doctorId) {
    List<Token> tokens = fetchTokens();
    
    // Move IN_PROGRESS to position 0
    Token inProgress = tokens.stream()
        .filter(t -> t.getStatus() == IN_PROGRESS)
        .findFirst()
        .orElse(null);
    
    if (inProgress != null) {
        tokens.remove(inProgress);
        tokens.add(0, inProgress);
    }
    
    return tokens.stream().map(toResponse).collect(toList());
}
```

---

### Q7: How is the transaction management implemented? Why transactions are crucial here.

**A:**

**Transaction Boundaries:**

```java
@Service
public class QueueService {
    
    @Transactional
    public TokenResponse generateTokenNumber(Doctor doctor) {
        // Entire method runs in single transaction
        Doctor doctorLocked = findByIdForUpdate(doctor.getId());
        
        // Check if counter needs reset
        if (isDifferentDay(doctorLocked.getLastTokenDate())) {
            doctorLocked.setTokenCounter(0);
        }
        
        // Increment counter atomically
        doctorLocked.setTokenCounter(doctorLocked.getTokenCounter() + 1);
        save(doctorLocked);  // Flush to DB immediately
        
        return buildToken(doctorLocked);
    }
    
    @Transactional
    public void callNextPatient(Long doctorId) {
        // Mark current IN_PROGRESS as COMPLETED
        Token current = findCurrentToken(doctorId);
        current.setStatus(COMPLETED);
        save(current);
        
        // Move next WAITING to IN_PROGRESS
        Token next = getNextWaitingToken(doctorId);
        next.setStatus(IN_PROGRESS);
        save(next);
        
        // Broadcast update (sent after transaction completes)
        broadcastQueueUpdate(doctorId);
    }
}
```

**Why Transactions Matter:**

1. **Atomicity** - Multiple DB operations succeed or fail together
2. **Consistency** - Database always in valid state
3. **Isolation** - Concurrent requests don't interfere
4. **Durability** - Data persists after commit

**Example: Without Transactions**

```
Doctor calls "Next Patient":
1. Current token marked COMPLETED ✅
2. Error occurs! 💥
3. Next token never marked IN_PROGRESS ❌
→ Queue is corrupted!
```

**With Transactions:**
```
1. Current token marked COMPLETED
2. Error occurs! 💥
3. ROLLBACK → Everything reverted
4. Queue remains consistent ✅
```

---

### Q8: Explain the pessimistic locking mechanism. Why not optimistic locking?

**A:**

**Pessimistic Locking Implementation:**

```java
// DoctorRepository.java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT d FROM Doctor d WHERE d.id = :id")
Optional<Doctor> findByIdForUpdate(@Param("id") Long id);
```

**How It Works:**

```
Thread 1                          Thread 2
---------                         ---------
SELECT * FROM doctor             
  WHERE id=5 FOR UPDATE;          (blocks here)
  
Lock acquired ✅
  
Increment token_counter           (waiting for lock...)
Save doctor
Commit transaction
Release lock ✅                   

                                 SELECT * FROM doctor
                                   WHERE id=5 FOR UPDATE;
                                 
                                 Lock acquired ✅
                                 Increment token_counter
                                 Save
                                 Commit
                                 Release lock
```

**Why Not Optimistic Locking:**

| Pessimistic | Optimistic |
|---|---|
| **When:** Low-medium contention | High-medium contention |
| **Mechanism:** Database row lock | Version column + retry |
| **Conflicts:** Prevented (blocked) | Detected (retry needed) |
| **For Queue:** ✅ Preferred | ❌ Not ideal |
| **Token Gen:** Safe guarantee | Race condition risk |

**Optimistic Locking Example (NOT used here):**

```java
@Entity
public class Doctor {
    @Version
    private Long version;  // Version column
}

// Concurrent requests:
Thread 1: UPDATE doctor SET version=2 WHERE id=5 AND version=1
Thread 2: UPDATE doctor SET version=2 WHERE id=5 AND version=1
         ↑ One thread fails, must retry

// If retry fails again → exponential backoff needed
```

**For hospital queues:**
- Cannot afford token collisions
- Pessimistic locking guarantees correctness
- Trade-off: Serializes concurrent token generation (acceptable for queue system)

---

## 3. FRONTEND - ANGULAR & RXJS

### Q9: How are routes protected in Angular? Describe the guard implementation.

**A:**

**Two-Layer Guard System:**

**Layer 1: AuthGuard (Login Check)**

```typescript
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
    constructor(private auth: AuthService, private router: Router) {}
    
    canActivate(): boolean {
        if (this.auth.isLoggedIn()) {
            return true;
        }
        this.router.navigate(['/auth/login']);
        return false;
    }
}
```

**Layer 2: RoleGuard (Role Check)**

```typescript
@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {
    constructor(private auth: AuthService, private router: Router) {}
    
    canActivate(route: ActivatedRouteSnapshot): boolean {
        const requiredRoles = route.data['roles'] as string[];
        const userRole = this.auth.getRole();
        
        if (requiredRoles && requiredRoles.includes(userRole)) {
            return true;
        }
        
        // Redirect based on user's actual role
        this.router.navigate([`/${userRole.toLowerCase()}`]);
        return false;
    }
}
```

**Route Configuration:**

```typescript
const routes: Routes = [
    { path: '', redirectTo: '/auth/login', pathMatch: 'full' },
    { path: 'auth/login', component: LoginComponent },  // No guard (public)
    
    {
        path: 'admin',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['ADMIN'] },
        loadChildren: () => import('./modules/admin/admin.module')
            .then(m => m.AdminModule)
    },
    
    {
        path: 'doctor',
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['DOCTOR'] },
        loadChildren: () => import('./modules/doctor/doctor.module')
            .then(m => m.DoctorModule)
    },
    
    {
        path: 'patient',
        loadChildren: () => import('./modules/patient/patient.module')
            .then(m => m.PatientModule)
        // No guard - patient module is public
    }
];
```

**Guard Execution Order:**

```
Route Access Request
    ↓
1. AuthGuard.canActivate()
    ├─ Is user logged in? 
    ├─ YES → Continue
    └─ NO → Redirect to /login
    ↓
2. RoleGuard.canActivate()
    ├─ Does user have required role?
    ├─ YES → Load module
    └─ NO → Redirect to user's dashboard
    ↓
Component Loaded
```

**localStorage-Based Session:**

```typescript
// AuthService maintains session
export class AuthService {
    private currentUser$ = new BehaviorSubject<User | null>(null);
    
    constructor() {
        // Restore session from localStorage on app init
        const stored = localStorage.getItem('hq_user');
        if (stored) {
            this.currentUser$.next(JSON.parse(stored));
        }
    }
    
    isLoggedIn(): boolean {
        return !!this.getToken() && !!this.currentUser$.value;
    }
    
    logout() {
        localStorage.removeItem('hq_token');
        localStorage.removeItem('hq_user');
        this.currentUser$.next(null);
        this.router.navigate(['/auth/login']);
    }
}
```

---

### Q10: Explain how JWT tokens are injected into requests. What happens on 401?

**A:**

**JwtInterceptor Implementation:**

```typescript
@Injectable()
export class JwtInterceptor implements HttpInterceptor {
    constructor(private auth: AuthService) {}
    
    intercept(
        req: HttpRequest<any>,
        next: HttpHandler
    ): Observable<HttpEvent<any>> {
        // Clone request and add auth header
        const token = this.auth.getToken();
        
        if (token) {
            req = req.clone({
                setHeaders: {
                    Authorization: `Bearer ${token}`
                }
            });
        }
        
        // Continue request and handle errors
        return next.handle(req).pipe(
            catchError((error: HttpErrorResponse) => {
                if (error.status === 401) {
                    // Token expired or invalid
                    this.auth.logout();
                    return throwError(() => error);
                }
                return throwError(() => error);
            })
        );
    }
}
```

**Header Injection Example:**

```
WITHOUT Interceptor:
POST /api/doctor/queue
Content-Type: application/json

WITH Interceptor:
POST /api/doctor/queue
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**401 Error Handling Flow:**

```
Backend returns 401
    ↓
JwtInterceptor catches 401
    ↓
authService.logout()
    ├─ Clear localStorage
    ├─ Reset currentUser$ subject
    ├─ Emit to all subscribers
    └─ Navigate to /login
    ↓
All active HTTP operations cancelled
    ↓
User sees login screen
```

**Why `request.clone()`:**

```typescript
// ❌ WRONG - Modifies original request object
req.headers = req.headers.set('Authorization', `Bearer ${token}`);

// ✅ CORRECT - Creates immutable new request
req = req.clone({
    setHeaders: { Authorization: `Bearer ${token}` }
});
```

Angular's HttpRequest is immutable for thread safety and predictability.

---

### Q11: How does lazy loading improve performance? Show the configuration.

**A:**

**Lazy Loading Concept:**

```
Without Lazy Loading:          With Lazy Loading:
──────────────────────         ─────────────────
Initial bundle: 2.5 MB         Initial bundle: 500 KB
Load time: 5 seconds          Load time: 1 second

User navigates to /admin       User navigates to /admin
                               ↓
                               Fetch admin.module.js (600 KB)
                               Load time: 2 seconds
                               Total: 3 seconds
```

**Configuration:**

```typescript
// app-routing.module.ts
const routes: Routes = [
    {
        path: 'admin',
        loadChildren: () => import('./modules/admin/admin.module')
            .then(m => m.AdminModule),
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['ADMIN'] }
    },
    
    {
        path: 'doctor',
        loadChildren: () => import('./modules/doctor/doctor.module')
            .then(m => m.DoctorModule),
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['DOCTOR'] }
    },
    
    {
        path: 'receptionist',
        loadChildren: () => import('./modules/receptionist/receptionist.module')
            .then(m => m.ReceptionistModule),
        canActivate: [AuthGuard, RoleGuard],
        data: { roles: ['RECEPTIONIST'] }
    },
    
    {
        path: 'patient',
        loadChildren: () => import('./modules/patient/patient.module')
            .then(m => m.PatientModule)
        // Patient module is public - no guard
    }
];
```

**Module Declaration:**

```typescript
// admin/admin.module.ts
@NgModule({
    declarations: [
        AdminLayoutComponent,
        DashboardComponent,
        UsersComponent,
        DepartmentsComponent,
        DoctorsComponent
    ],
    imports: [
        CommonModule,
        AdminRoutingModule
    ]
})
export class AdminModule { }

// admin/admin-routing.module.ts
const routes: Routes = [
    {
        path: '',
        component: AdminLayoutComponent,
        children: [
            { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
            { path: 'dashboard', component: DashboardComponent },
            { path: 'users', component: UsersComponent },
            { path: 'departments', component: DepartmentsComponent },
            { path: 'doctors', component: DoctorsComponent }
        ]
    }
];
```

**Build Output:**

```
dist/
├── main.js           (500 KB - core app)
├── admin-module.js   (600 KB - loaded only for /admin)
├── doctor-module.js  (400 KB - loaded only for /doctor)
├── receptionist...js (450 KB - loaded only for /receptionist)
└── patient-module.js (350 KB - loaded only for /patient)
```

**Benefits:**

| Metric | Without Lazy Loading | With Lazy Loading |
|---|---|---|
| Initial Load | 2.5 MB | 500 KB |
| First Paint | 5s | 1s |
| TTI (Time to Interactive) | 6s | 1.5s |
| Memory Usage | High initially | Grows as features used |
| Admin Feature Load | Already in bundle | 2s on-demand |

---

## 4. DATABASE & CONCURRENCY

### Q12: Design a database schema. Explain foreign keys and relationships.

**A:**

**Entity Relationship Diagram:**

```
┌──────────────┐
│    Users     │
├──────────────┤
│ id (PK)      │
│ email (UQ)   │
│ password     │
│ role         │
│ active       │
│ created_at   │
└──────────────┘
        │
        ├─ (1-1) ──────┐ Doctors
        │               │
        │        ┌──────┴──────────┐
        │        │   Doctors      │
        │        ├────────────────┤
        │        │ id (PK)        │
        │        │ user_id (FK→U)│
        │        │ dept_id (FK)   │
        │        └────────┬───────┘
        │                  │
        │                  ├─ (N-1)
        │                  │
        │        ┌─────────▼──────────┐
        │        │  Departments       │
        │        ├────────────────────┤
        │        │ id (PK)            │
        │        │ name (UQ)          │
        │        │ prefix (UQ)        │
        │        └────────────────────┘
        │
        └─────────────Patients
                ┌──────────────────┐
                │    Patients      │
                ├──────────────────┤
                │ id (PK)          │
                │ phone (UQ)       │
                │ name             │
                │ email            │
                │ age              │
                │ gender           │
                └──────┬───────────┘
                       │
                       ├─ (1-N)
                       │
                ┌──────▼────────┐
                │    Tokens     │
                ├───────────────┤
                │ id (PK)       │
                │ token_num(UQ) │
                │ patient_id(FK)│
                │ doctor_id (FK)│
                │ priority      │
                │ status        │
                │ created_at    │
                └───────────────┘
```

**Table Definitions:**

```sql
-- Users: All user accounts (Admin, Receptionist, Doctor)
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'RECEPTIONIST', 'DOCTOR') NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role)
);

-- Departments: Medical departments
CREATE TABLE departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) UNIQUE NOT NULL,
    prefix VARCHAR(10) UNIQUE NOT NULL,
    description VARCHAR(255),
    INDEX idx_prefix (prefix)
);

-- Doctors: Doctor profiles
CREATE TABLE doctors (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE NOT NULL,
    department_id BIGINT NOT NULL,
    specialization VARCHAR(100),
    active BOOLEAN DEFAULT TRUE,
    token_counter INT DEFAULT 0,
    last_token_date DATE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT,
    INDEX idx_department (department_id)
);

-- Patients: Patient information
CREATE TABLE patients (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    age INT,
    gender VARCHAR(20),
    registration_mode ENUM('SELF', 'RECEPTIONIST'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_phone (phone)
);

-- Tokens: Queue tokens
CREATE TABLE tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token_number VARCHAR(50) UNIQUE NOT NULL,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    priority ENUM('EMERGENCY', 'URGENT', 'NORMAL') DEFAULT 'NORMAL',
    status ENUM('WAITING', 'IN_PROGRESS', 'COMPLETED') DEFAULT 'WAITING',
    registration_mode ENUM('SELF', 'RECEPTIONIST'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE RESTRICT,
    INDEX idx_token_number (token_number),
    INDEX idx_doctor_status (doctor_id, status),
    INDEX idx_created_at (created_at)
);
```

**Foreign Key Constraints:**

| FK | References | ON DELETE | Reason |
|---|---|---|---|
| doctors.user_id → users | CASCADE | If user deleted, doctor profile deleted |
| doctors.dept_id → departments | RESTRICT | Cannot delete dept if doctors exist |
| tokens.patient_id → patients | CASCADE | If patient deleted, tokens deleted |
| tokens.doctor_id → doctors | RESTRICT | Cannot delete doctor if tokens exist |

---

### Q13: What concurrency issues could occur in the queue system? How are they mitigated?

**A:**

**Potential Scenarios:**

**Scenario 1: Duplicate Token Numbers**

```
Doctor A generates token CARD-20260422-001
Time 09:00:00
┌─────────────────────────────────────────┐
│ Thread 1              Thread 2           │
│ (Request 1)          (Request 2)         │
├─────────────────────────────────────────┤
│ SELECT token_counter │                   │
│ FROM doctor          │                   │
│ WHERE id=5           │                   │
│ Result: 0            │                   │
│                      │ SELECT token_...  │
│                      │ Result: 0 ❌      │
│ counter = 0 + 1 = 1  │                   │
│ UPDATE counter=1     │                   │
│ COMMIT               │ counter = 0 + 1   │
│                      │ UPDATE counter=1  │
│                      │ COMMIT            │
│ Token: 001 ✅        │ Token: 001 ❌❌   │
└─────────────────────────────────────────┘
Result: DUPLICATE token numbers!
```

**Mitigation: Pessimistic Locking**

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT d FROM Doctor d WHERE d.id = :id")
Optional<Doctor> findByIdForUpdate(@Param("id") Long id);
```

```
Thread 1                  Thread 2
(holds lock)             (waits for lock)
─────────────────────────────────────
SELECT...FOR UPDATE ✅    SELECT...FOR UPDATE ⏳
counter = 0 + 1 = 1     (blocked, waiting...)
UPDATE counter=1
COMMIT
Release lock ✅        
                        SELECT...FOR UPDATE ✅
                        counter = 1 + 1 = 2
                        UPDATE counter=2
                        COMMIT

Result: 001, 002 ✅ (No duplicates)
```

**Scenario 2: Race Condition in callNextPatient()**

```
Current State:
Token1 (IN_PROGRESS) - Patient Alice
Token2 (WAITING) - Patient Bob

Two simultaneous requests to callNextPatient():
┌─────────────────────────────────────┐
│ Request 1         Request 2          │
├─────────────────────────────────────┤
│ Fetch Token1      Fetch Token1       │
│ Update→COMPLETED  Update→COMPLETED   │
│ Save              (overwrites!)      │
│                                      │
│ Fetch Token2                         │
│ Update→IN_PROGRESS                   │
│                                      │
│ Save              Save (overwrites!) │
│ Update Token2→IN  (same token)       │
└─────────────────────────────────────┘

Result: Token1 completed ok, but Token2 IN_PROGRESS twice!
```

**Mitigation: @Transactional + Pessimistic Locking**

```java
@Transactional
public void callNextPatient(Long doctorId) {
    // Lock doctor row
    Doctor doctor = findByIdForUpdate(doctorId);
    
    // Atomic operations
    Token current = findCurrentInProgress(doctorId);
    current.setStatus(COMPLETED);
    
    Token next = getNextWaitingToken(doctorId);
    next.setStatus(IN_PROGRESS);
    
    save(current);
    save(next);
    
    // Rollback if any error occurs
}
```

**Scenario 3: Priority Update During Queue Fetch**

```
Doctor fetches queue → gets 10 tokens
Meanwhile, receptionist updates priority of token #5
Doctor sees stale queue → displays wrong order to patient

Fixed by: WebSocket broadcast of priority changes
Doctor's queue auto-updates in real-time
```

---

### Q14: Design and explain an index strategy. What indexes exist and why?

**A:**

**Index Strategy:**

```sql
-- 1. Primary Key Indexes (auto-created)
ALTER TABLE users ADD PRIMARY KEY (id);
ALTER TABLE tokens ADD PRIMARY KEY (id);

-- 2. Unique Constraints (prevent duplicates)
ALTER TABLE users ADD UNIQUE INDEX idx_email (email);
ALTER TABLE patients ADD UNIQUE INDEX idx_phone (phone);
ALTER TABLE tokens ADD UNIQUE INDEX idx_token_number (token_number);

-- 3. Foreign Key Indexes (for joins)
ALTER TABLE doctors ADD INDEX idx_user_id (user_id);
ALTER TABLE doctors ADD INDEX idx_department (department_id);
ALTER TABLE tokens ADD INDEX idx_patient_id (patient_id);
ALTER TABLE tokens ADD INDEX idx_doctor_id (doctor_id);

-- 4. Query Optimization Indexes
ALTER TABLE tokens ADD INDEX idx_doctor_status (doctor_id, status);
ALTER TABLE tokens ADD INDEX idx_created_at (created_at);
ALTER TABLE users ADD INDEX idx_role (role);
ALTER TABLE patients ADD INDEX idx_registration_mode (registration_mode);

-- 5. Composite Index for Queue Retrieval
ALTER TABLE tokens ADD INDEX idx_doctor_priority_created 
    (doctor_id, priority, created_at);
```

**Index Usage Chart:**

| Query | Index Used | Why |
|---|---|---|
| `SELECT * FROM users WHERE email = ?` | idx_email | Unique lookup |
| `SELECT * FROM tokens WHERE doctor_id = ? AND status IN (...)` | idx_doctor_status | Queue fetch |
| `SELECT * FROM tokens ORDER BY created_at` | idx_created_at | FIFO sorting |
| `SELECT * FROM doctors WHERE dept_id = ?` | idx_department | Department search |
| `SELECT * FROM users WHERE role = ?` | idx_role | List users by role |

**Query Execution Examples:**

```
Query: SELECT * FROM tokens WHERE doctor_id=5 ORDER BY priority, created_at

WITHOUT Index:
→ Full table scan (1M+ rows)
→ Sort in memory
→ Performance: 500ms+

WITH Composite Index (doctor_id, priority, created_at):
→ Seek to doctor_id=5
→ Traverse sorted by priority
→ Descend through created_at
→ Performance: 5ms ✅
```

---

## 5. REAL-TIME FEATURES (WEBSOCKET)

### Q15: How does real-time queue updates work? Explain WebSocket architecture.

**A:**

**WebSocket Connection Flow:**

```
Browser (Patient)
    │
    ├─ connects to ws://localhost:8081/ws (via SockJS)
    │
    ├─ STOMP handshake
    │
    ├─ SUBSCRIBE /topic/token/CARD-20260422-005
    │    └─ Server acknowledges
    │
    ├─ Display: "You are #3 in queue"
    │
    └─ Doctor processes patient
         │
         └─ Backend publishes to /topic/token/CARD-20260422-005
              │
              └─ Browser receives: "Status changed to IN_PROGRESS"
```

**Backend Publishing:**

```java
@Transactional
public void callNextPatient(Long doctorId) {
    Token current = getCurrentToken(doctorId);
    current.setStatus(COMPLETED);
    
    Token next = getNextWaitingToken(doctorId);
    next.setStatus(IN_PROGRESS);
    
    save(current);
    save(next);
    
    // Publish to all subscribers
    messagingTemplate.convertAndSend(
        "/topic/queue/" + doctorId,
        buildQueueResponse()  // Entire queue
    );
    
    messagingTemplate.convertAndSend(
        "/topic/token/" + next.getTokenNumber(),
        tokenResponse  // Specific token update
    );
}
```

**Frontend Subscription:**

```typescript
// Patient tracking component
export class PatientTrackComponent implements OnInit, OnDestroy {
    queuePosition$ = new BehaviorSubject(null);
    
    constructor(
        private websocket: WebSocketService,
        private patient: PatientService
    ) {}
    
    ngOnInit() {
        // Subscribe to token updates
        this.websocket.subscribeToToken<TokenResponse>(this.tokenNumber)
            .subscribe(
                (update) => {
                    this.queuePosition$.next(update.position);
                    this.status$.next(update.status);
                },
                (error) => {
                    console.warn('WebSocket failed, fallback to polling');
                    this.startPoling();
                }
            );
    }
    
    // Fallback to polling if WebSocket unavailable
    private startPolling() {
        interval(15000)  // Poll every 15 seconds
            .pipe(
                switchMap(() => 
                    this.patient.getQueuePosition(this.tokenNumber)
                )
            )
            .subscribe(position => {
                this.queuePosition$.next(position);
            });
    }
    
    ngOnDestroy() {
        this.websocket.unsubscribe();
    }
}
```

**WebSocket Broadcast Triggers:**

| Event | Channel | Payload |
|---|---|---|
| New token generated | `/topic/queue/{doctorId}` | Updated queue list |
| Doctor calls next | `/topic/queue/{doctorId}` | Queue reordered |
| Priority updated | `/topic/queue/{doctorId}` | Queue reordered |
| Token status changed | `/topic/token/{tokenNumber}` | New status + position |
| Receptionist removes token | `/topic/queue/{doctorId}` | Token removed from view |

---

### Q16: What's the fallback mechanism if WebSocket fails? Explain resilience.

**A:**

**Fallback Strategy (Graceful Degradation):**

```typescript
export class WebSocketService {
    private stompClient: Client;
    private subscriptions = new Map();
    private pendingSubscriptions: any[] = [];
    private reconnectDelay = 5000;  // 5 seconds
    
    connect(): Promise<void> {
        return new Promise((resolve, reject) => {
            const socket = new SockJS(this.wsUrl);
            
            this.stompClient = Stomp.over(socket);
            
            this.stompClient.connect(
                {},
                (frame) => {
                    // Process pending subscriptions
                    this.pendingSubscriptions.forEach(sub => {
                        this.executeSubscription(sub);
                    });
                    this.pendingSubscriptions = [];
                    resolve();
                },
                (error) => {
                    console.error('WebSocket connection failed');
                    reject(error);
                    this.scheduleReconnect();
                }
            );
        });
    }
    
    subscribeToToken<T>(tokenNumber: string): Observable<T> {
        return new Observable((observer) => {
            const destination = `/topic/token/${tokenNumber}`;
            
            if (this.stompClient?.connected) {
                const subscription = this.stompClient.subscribe(
                    destination,
                    (message) => {
                        try {
                            const data = JSON.parse(message.body);
                            observer.next(data);
                        } catch (e) {
                            observer.error(e);
                        }
                    },
                    (error) => {
                        observer.error(error);
                    }
                );
                
                this.subscriptions.set(destination, subscription);
                
                return () => {
                    subscription.unsubscribe();
                    this.subscriptions.delete(destination);
                };
            } else {
                // Queue subscription for when connection is ready
                this.pendingSubscriptions.push({
                    destination,
                    observer,
                    type: 'token'
                });
                
                // Fallback to polling immediately
                return this.startPollingFallback(tokenNumber, observer);
            }
        });
    }
    
    private startPollingFallback(
        tokenNumber: string,
        observer: Subscriber<any>
    ): Subscription {
        return interval(15000)  // Poll every 15 seconds
            .pipe(
                switchMap(() => this.patientService.getQueuePosition(tokenNumber)),
                catchError(() => {
                    observer.error(new Error('Polling failed'));
                    return throwError(() => new Error('Polling failed'));
                })
            )
            .subscribe(
                (data) => observer.next(data),
                (error) => observer.error(error)
            );
    }
    
    private scheduleReconnect() {
        setTimeout(() => {
            console.log('Attempting WebSocket reconnection...');
            this.connect().catch(() => this.scheduleReconnect());
        }, this.reconnectDelay);
    }
}
```

**Resilience Layers:**

```
Layer 1: Initial Connection
    ├─ Try WebSocket
    └─ If fails → Layer 2

Layer 2: Polling Fallback
    ├─ HTTP polling every 15 seconds
    └─ If fails → Layer 3

Layer 3: Manual Refresh
    ├─ User can click "Refresh"
    └─ If fails → Show error message with retry
```

**User Experience:**

| Scenario | Behavior |
|---|---|
| WebSocket works | Real-time updates (instant) |
| WebSocket failed | Automatic fallback to polling (15s delay) |
| Polling fails | "Offline" badge, manual refresh button |
| Reconnected | Auto-removes offline badge |

---

## 6. API DESIGN & INTEGRATION

### Q17: Design a patient self-service token generation API. Show request/response.

**A:**

**Endpoint Design:**

```
Endpoint: POST /api/patient/generate-token
Visibility: Public (no authentication)
Rate Limit: 1 request per phone number per 5 minutes
Response: 200 OK with token details
```

**Request Body:**

```json
{
    "patientName": "John Doe",
    "phoneNumber": "9876543210",
    "departmentId": 1,
    "doctorId": 5
}
```

**Response (200 OK):**

```json
{
    "success": true,
    "data": {
        "tokenId": 42,
        "tokenNumber": "CARD-20260422-001",
        "status": "WAITING",
        "priority": "NORMAL",
        "generatedAt": "2026-04-22T10:30:00Z",
        "estimatedWaitTime": "45 minutes"
    },
    "message": "Token generated successfully"
}
```

**Error Responses:**

```json
// 400 - Invalid department or doctor
{
    "success": false,
    "error": "INVALID_DOCTOR",
    "message": "Selected doctor not available in this department"
}

// 429 - Rate limit exceeded
{
    "success": false,
    "error": "RATE_LIMIT_EXCEEDED",
    "message": "Maximum 1 token per phone per 5 minutes. Retry in 2 min"
}

// 500 - Token generation failed
{
    "success": false,
    "error": "TOKEN_GENERATION_FAILED",
    "message": "Failed to generate token. Please try again"
}
```

**Backend Implementation:**

```java
@PostMapping("/api/patient/generate-token")
public ResponseEntity<?> generateToken(
    @RequestBody GenerateTokenRequest request
) {
    // 1. Validate request
    if (!isValidPhoneNumber(request.getPhoneNumber())) {
        return badRequest("Invalid phone number");
    }
    
    // 2. Check rate limit
    if (hasExceededRateLimit(request.getPhoneNumber())) {
        return status(429).body(
            errorResponse("RATE_LIMIT_EXCEEDED", "Too many requests")
        );
    }
    
    // 3. Find or create patient
    Patient patient = patientService.findOrCreatePatient(
        request.getPatientName(),
        request.getPhoneNumber()
    );
    
    // 4. Generate token (always NORMAL priority for self-service)
    TokenResponse token = queueService.generateTokenOnline(
        patient.getId(),
        request.getDoctorId()
    );
    
    // 5. Broadcast to receptionist dashboard
    messagingTemplate.convertAndSend(
        "/topic/queue/" + request.getDoctorId(),
        getUpdatedQueue(request.getDoctorId())
    );
    
    return ok(successResponse(token));
}
```

---

### Q18: What are potential API security issues? How to mitigate?

**A:**

**Security Issues & Mitigations:**

| Issue | Risk | Mitigation |
|---|---|---|
| **SQL Injection** | Attacker modifies query | Use parameterized queries (Spring Data JPA) |
| **XSS** | Malicious script injected | Sanitize inputs, escape output in Angular |
| **CSRF** | Forge requests | Stateless JWT (no cookies needed) |
| **Brute Force** | Crack passwords | Rate limiting on login endpoint |
| **Token Theft** | Steal JWT from localStorage | Use httpOnly cookies (more secure) |
| **CORS Misconfiguration** | Allow all origins | Whitelist specific origins |
| **Sensitive Data in Response** | Leak user info | DTO filtering (don't return password hash) |
| **Unencrypted Passwords** | Plain-text storage | BCrypt hashing |

**Specific Examples:**

**1. SQL Injection Prevention:**

```java
// ❌ VULNERABLE
String query = "SELECT * FROM users WHERE email='" + email + "'";
// Attacker input: ' OR '1'='1
// Becomes: SELECT * FROM users WHERE email='' OR '1'='1'

// ✅ SAFE - Parameterized query
@Query("SELECT u FROM User u WHERE u.email = :email")
Optional<User> findByEmail(@Param("email") String email);
```

**2. CORS Configuration:**

```java
@Configuration
public class SecurityConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // ❌ VULNERABLE
        config.setAllowedOrigins(Arrays.asList("*"));  // Allow ALL origins!
        
        // ✅ SAFE - Specific origins only
        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:4200",
            "https://hospital.example.com"
        ));
        
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

**3. Rate Limiting for Login:**

```java
@Service
public class RateLimitService {
    private Map<String, LocalDateTime> loginAttempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_DURATION = 15;  // minutes
    
    public boolean isLoginAllowed(String email) {
        LocalDateTime lastAttempt = loginAttempts.get(email);
        
        if (lastAttempt == null) {
            return true;
        }
        
        LocalDateTime lockoutEnd = lastAttempt.plusMinutes(LOCK_DURATION);
        if (LocalDateTime.now().isBefore(lockoutEnd)) {
            return false;  // Still locked
        }
        
        loginAttempts.remove(email);
        return true;
    }
    
    public void recordFailedAttempt(String email) {
        loginAttempts.put(email, LocalDateTime.now());
    }
}
```

**4. DTO Filtering (Don't leak password hash):**

```java
// ❌ WRONG - Returns entire User entity
@GetMapping("/api/users/{id}")
public User getUser(@PathVariable Long id) {
    return userRepository.findById(id).get();  // Exposes password hash!
}

// ✅ CORRECT - Returns DTO without sensitive fields
@GetMapping("/api/users/{id}")
public UserResponse getUser(@PathVariable Long id) {
    User user = userRepository.findById(id).get();
    return new UserResponse(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getRole()
        // password NOT included
    );
}
```

**5. Use HttpOnly Cookies Instead of localStorage:**

Current implementation uses `localStorage` (vulnerable to XSS):
```typescript
localStorage.setItem('hq_token', token);  // ❌ XSS can steal this
```

Better approach (httpOnly cookie):
```java
// Backend sets httpOnly cookie
response.addCookie(new HttpCookie.Builder("Authorization")
    .value(token)
    .path("/")
    .httpOnly(true)      // Cannot be accessed via JS
    .secure(true)        // HTTPS only
    .sameSite("Strict")  // CSRF protection
    .build());
```

---

## 7. SECURITY DEEP DIVE

### Q19: Explain the password hashing strategy. Why BCrypt and not MD5/SHA?

**A:**

**Password Hashing Comparison:**

| Method | Algorithm | Strength | Speed | Salted |
|---|---|---|---|---|
| **MD5/SHA1** | Cryptographic hash | ❌ Weak | ⚡ Fast | Optional |
| **SHA-256** | Cryptographic hash | ⚠️ Medium | ⚡ Fast | Optional |
| **bcrypt** | Key derivation | ✅ Strong | 🐢 Slow | Yes |
| **scrypt** | Key derivation | ✅ Very Strong | 🐢 Very Slow | Yes |
| **argon2** | KDF | ✅ Strongest | 🐢 Very Slow | Yes |

**Why NOT MD5/SHA:**

```
1. Dictionary Attack Risk:
   password = "admin123"
   md5("admin123") = "0192023a7bbd73250516f069df18b500"
   
   Attacker pre-computes all common passwords:
   "0192023a7bbd73250516f069df18b500" → "admin123" (lookup!)

2. No Computational Cost:
   MD5("password") = instant (nanoseconds)
   Attacker tries: 1 billion hashes/second
   
   BCrypt with cost=12:
   Attacker tries: 100 hashes/second (1 million times slower!)

3. No Salt by Default:
   Same password → Same hash across systems
   password = "password"
   Hash = "5f4dcc3b5aa765d61d8327deb882cf99" (everywhere!)
```

**BCrypt Implementation:**

```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Strength 12 = 2^12 = 4096 iterations
        // ~100ms to hash one password (slows down attackers)
        return new BCryptPasswordEncoder(12);
    }
}
```

**Password Hashing Flow:**

```java
// During registration
String plainPassword = "Admin@123";
String hash = passwordEncoder.encode(plainPassword);
// Result: $2a$12$R9h/cIPyNuWLlk/dHHrKMuIlHPp.4QABqrqW2BF3nWMPWr3bZPEFS

// During login
String providedPassword = "Admin@123";
boolean matches = passwordEncoder.matches(providedPassword, hash);
// ✅ Returns true

// Even if attacker gets hash:
// $2a$12$R9h/cIPyNuWLlk/dHHrKMuIlHPp.4QABqrqW2BF3nWMPWr3bZPEFS
// Cannot reverse it (one-way function)
// Must brute-force: BCrypt slows this down to impractical levels
```

**BCrypt Hash Structure:**

```
$2a$12$R9h/cIPyNuWLlk/dHHrKMuIlHPp.4QABqrqW2BF3nWMPWr3bZPEFS
 ^^^ ^^  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 |   |   |
 |   |   └─ Hash (22 characters)
 |   └─ Cost factor (12 = 2^12 iterations = 4096 rounds)
 └─ Version (2a = current standard)

Cost=12 → ~100ms per hash
Cost=10 → ~10ms per hash
Cost=14 → ~1second per hash
```

---

### Q20: How to prevent common vulnerabilities? (OWASP Top 10)

**A:**

**OWASP Top 10 & Mitigations:**

| Vulnerability | Prevention | Status in SmartApp |
|---|---|---|
| **SQL Injection** | Parameterized queries | ✅ Uses Spring Data JPA |
| **Broken Authentication** | Strong JWT + BCrypt | ✅ Implemented |
| **Sensitive Data Exposure** | HTTPS + secure cookies | ⚠️ TODO: Use httpOnly |
| **XML External Entities (XXE)** | Disable XML parsing | ✅ REST API only |
| **Broken Access Control** | Role-based guards | ✅ @PreAuthorize + Guards |
| **Security Misconfiguration** | Secure defaults | ⚠️ CORS needs review |
| **XSS (Cross-Site Scripting)** | Input validation + escaping | ✅ Angular sanitizes by default |
| **Insecure Deserialization** | Validate input | ✅ Uses DTOs |
| **OWASP:C3 - Injection** | Input validation | ✅ Parameterized + validation |
| **Broken Cryptography** | Use TLS + strong ciphers | ⚠️ TODO: Enable HTTPS |

**Specific Recommendations:**

**1. HTTPS Configuration:**

```yaml
# application.properties
server.ssl.key-store-type=PKCS12
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=changeit
server.ssl.key-alias=tomcat
server.port=8443  # HTTPS port
```

**2. Security Headers:**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .headers()
            .contentSecurityPolicy("default-src 'self'")
            .and()
            .xssProtection()
            .and()
            .frameOptions().deny()
            .and()
            .httpStrictTransportSecurity();
        
        return http.build();
    }
}
```

**3. Input Validation (Backend):**

```java
@Entity
public class Patient {
    @NotBlank(message = "Name is required")
    @Length(min=2, max=100, message="Name must be 2-100 chars")
    private String name;
    
    @NotBlank
    @Pattern(regexp="^[0-9]{10}$", message="Phone must be 10 digits")
    private String phone;
    
    @Email
    private String email;
}

@PostMapping("/api/patient/register")
public ResponseEntity<?> register(@Valid @RequestBody RegisterPatientRequest req) {
    // Automatically validates @NotBlank, @Email, @Pattern, etc.
    // If invalid → 400 Bad Request with field errors
}
```

**4. Frontend XSS Prevention:**

```typescript
// ❌ VULNERABLE
element.innerHTML = userInput;  // If <script>alert('hacked')</script>

// ✅ SAFE - Angular sanitizes by default
element.textContent = userInput;

// ✅ SAFE - Even with HTML binding
<div [innerHTML]="userInput | sanitizeHtml"></div>

@Pipe({ name: 'sanitizeHtml' })
export class SanitizeHtmlPipe implements PipeTransform {
    constructor(private sanitizer: DomSanitizer) {}
    
    transform(value: string) {
        return this.sanitizer.sanitize(SecurityContext.HTML, value);
    }
}
```

---

## 8. PERFORMANCE & SCALABILITY

### Q21: Identify performance bottlenecks. How to optimize?

**A:**

**Current Bottlenecks:**

```
1. Queue Sorting
   ├─ Every getQueue() call sorts in-memory
   ├─ O(n log n) for each request
   ├─ Issue: For 1000 tokens, ~10,000 comparisons per request
   └─ Fix: Use @OrderBy JPA annotation (sort in DB)

2. WebSocket Broadcasting
   ├─ Sends entire queue on every update
   ├─ Example: 100 tokens * 500 bytes = 50 KB per update
   ├─ Issue: 100 doctors * 50 KB = 5 MB/minute broadcast
   └─ Fix: Send delta (only changed tokens)

3. No Pagination
   ├─ Returns all active tokens at once
   ├─ Issue: 10,000 active tokens = 5 MB response
   └─ Fix: Implement pagination with limit/offset

4. UserDetails Lookup on Every Request
   ├─ JwtAuthFilter loads user from DB on each request
   ├─ Issue: If 100 req/sec → 100 DB queries/sec
   └─ Fix: Cache UserDetails or use JWT claims only
```

**Optimization Strategies:**

**1. Database-Level Sorting:**

```java
// ❌ BEFORE - In-memory sort
List<Token> tokens = tokenRepository.findByDoctorId(doctorId);
tokens.sort(Comparator
    .comparing(Token::getPriority)
    .thenComparing(Token::getCreatedAt));

// ✅ AFTER - Database sort
@Entity
public class Token {
    @OrderBy("priority ASC, createdAt ASC")
    private List<Token> tokens;
}

@Query("SELECT t FROM Token t WHERE t.doctor.id = :doctorId " +
       "ORDER BY t.priority ASC, t.createdAt ASC")
List<Token> findByDoctorIdOrdered(@Param("doctorId") Long doctorId);

// Benefit: DB returns pre-sorted, no Java sorting needed
// Performance: 1000 tokens → 100ms → 10ms
```

**2. WebSocket Delta Updates:**

```java
// ❌ BEFORE - Send entire queue
@Transactional
public void callNextPatient(Long doctorId) {
    // ... update logic ...
    
    List<TokenResponse> fullQueue = getEntireQueue(doctorId);
    messagingTemplate.convertAndSend(
        "/topic/queue/" + doctorId,
        fullQueue  // 50 KB of data
    );
}

// ✅ AFTER - Send only changes
public class QueueDeltaUpdate {
    private List<TokenResponse> added;
    private List<TokenResponse> removed;
    private List<TokenResponse> updated;
}

@Transactional
public void callNextPatient(Long doctorId) {
    Token completedToken = getCurrentToken(doctorId);
    Token nextToken = getNextWaitingToken(doctorId);
    
    QueueDeltaUpdate delta = new QueueDeltaUpdate(
        added = [nextToken],      // 500 bytes
        removed = [],
        updated = [completedToken] // 500 bytes
    );
    
    messagingTemplate.convertAndSend(
        "/topic/queue/" + doctorId,
        delta  // 1 KB instead of 50 KB!
    );
}
```

**3. Pagination for Large Datasets:**

```java
// API with pagination
@GetMapping("/api/receptionist/tokens")
public Page<TokenResponse> getActiveTokens(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
) {
    Pageable pageable = PageRequest.of(page, size, 
        Sort.by("priority").ascending()
            .and(Sort.by("createdAt").ascending()));
    
    Page<Token> tokens = tokenRepository.findByStatus(WAITING, pageable);
    return tokens.map(this::toResponse);
}
```

Request: `GET /api/receptionist/tokens?page=0&size=20`

Response:
```json
{
    "content": [... 20 tokens ...],
    "totalElements": 10000,
    "totalPages": 500,
    "currentPage": 0,
    "hasNext": true
}
```

**4. Cache UserDetails:**

```java
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Cacheable(value = "users", key = "#email")
    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email).get();
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            convertToAuthorities(user.getRole())
        );
    }
}

// First request: DB query (200ms)
// Subsequent requests: Cache hit (5ms)
// Cache expires after 10 minutes
```

---

### Q22: How to scale this system for 10,000 concurrent users?

**A:**

**Scalability Architecture:**

```
Load Balancer (AWS ALB)
        │
    ┌───┼───┬────────┐
    │   │   │        │
    ▼   ▼   ▼        ▼
 API-1 API-2 API-3 API-4  (Spring Boot instances)
    │   │   │        │
    └───┼───┴────────┘
        │
    [Database Cluster]
    ├─ Master (write)
    ├─ Slave 1 (read-only)
    └─ Slave 2 (read-only)
        │
    [Redis Cache]
    ├─ UserDetails cache
    ├─ Active tokens cache
    └─ Session co-ordination
        │
    [WebSocket Broker]
    ├─ RabbitMQ / Apache Kafka
    └─ Distributes messages across instances
```

**Scalability Strategies:**

| Component | Strategy | Details |
|---|---|---|
| **Backend** | Stateless instances | Add more Spring Boot servers; no session affinity |
| **Database** | Read replicas | Master-slave setup; queries read from slaves |
| **Cache** | Redis | Cache user details, active tokens, reduce DB hits |
| **Message Broker** | RabbitMQ/Kafka | Distribute WebSocket messages across instances |
| **WebSocket** | Sticky sessions | Route user to same server for persistent connection |
| **Frontend** | CDN | Serve Angular app from edge servers globally |

**Example: 10,000 Concurrent Users**

```
Current Setup (1 Backend):
├─ Max concurrent connections: ~500
├─ QPS (Queries per second): 100
└─ Result: Overloaded, users experience timeouts ❌

Scaled Setup (4 Backends):
├─ Max concurrent connections: 500 * 4 = 2,000
├─ QPS: 100 * 4 = 400
├─ With caching: Reduces DB to 150 QPS
└─ Result: Handles 10,000 users smoothly ✅
```

**WebSocket Distribution with RabbitMQ:**

```java
@Configuration
public class RabbitMqConfig {
    @Bean
    public Topic queueTopic() {
        return new Topic("hospital-queue-topic");
    }
}

@Service
public class QueueService {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void broadcastQueueUpdate(Long doctorId, QueueResponse queue) {
        // Publish to message broker (all instances receive it)
        rabbitTemplate.convertAndSend(
            "hospital-queue-topic",
            "queue.update." + doctorId,
            queue
        );
    }
}

// Each instance:
@RabbitListener(queues = "queue-update-queue")
public void receiveQueueUpdate(QueueResponse queue) {
    // Forward to subscribed clients
    messagingTemplate.convertAndSend(
        "/topic/queue/" + queue.getDoctorId(),
        queue
    );
}
```

---

## 9. PROBLEM-SOLVING SCENARIOS

### Q23: A patient reports their queue position never updates. Debug this.

**A:**

**Debugging Checklist:**

```
Step 1: Verify Token Exists
├─ GET /api/patient/token/{tokenNumber}
└─ Check response: Is token found? Has correct status?

Step 2: Check WebSocket Connection
├─ Browser DevTools → Network → WS
├─ Look for ws://localhost:8081/ws
├─ Status: "101 Switching Protocols" (good) or error?
└─ If error: WebSocket connection failed!

Step 3: Check Subscription
├─ Browser Console:
│   window.stompClient.subscriptions
├─ Should have entry: /topic/token/CARD-20260422-001
└─ If missing: Subscription failed!

Step 4: Verify Backend Publishing
├─ Add logs in callNextPatient():
│   log.info("Publishing to /topic/token/" + tokenNumber);
├─ Tail backend logs: tail -f backend.log
└─ Is "Publishing..." message appearing?

Step 5: Check Message Broker
├─ Is listener method being called?
├─ Add breakpoint in messageHandler
└─ Check payload is correctly formatted
```

**Likely Causes & Fixes:**

```
❌ CAUSE 1: WebSocket connection failed
   └─ FIX: Check browser console for connection errors
      │    Verify firewall allows WebSocket
      │    Check CORS configuration

❌ CAUSE 2: Wrong token number format
   └─ FIX: Verify token_number value
      │    Should be: CARD-20260422-001
      │    Not: 1 or CARD-2602-001

❌ CAUSE 3: Subscription not happening
   └─ FIX: Check ngOnInit() calls subscribe
      │    Verify component not destroyed early
      │    Check async pipe usage

❌ CAUSE 4: Backend not publishing
   └─ FIX: Add logs to callNextPatient()
      │    Check messagingTemplate is autowired
      │    Verify @EnableWebSocketMessageBroker configured

❌ CAUSE 5: Message lost in routing
   └─ FIX: Check destination path matches exactly
      │    Topic: /topic/token/CARD-20260422-001
      │    Subscribe: /topic/token/CARD-20260422-001
      │    Even one char mismatch breaks it!
```

**Complete Debug Code:**

```typescript
// Component - Add detailed logging
export class PatientTrackComponent implements OnInit {
    ngOnInit() {
        console.log('Component Init');
        console.log('Token Number:', this.tokenNumber);
        
        this.websocket.subscribeToToken<TokenResponse>(this.tokenNumber)
            .subscribe(
                (update) => {
                    console.log('✅ WebSocket Update:', update);
                    this.queuePosition = update.position;
                    this.status = update.status;
                },
                (error) => {
                    console.error('❌ WebSocket Error:', error);
                    console.log('Falling back to polling...');
                    this.startPolling();
                }
            );
    }
}

// Backend - Add detailed logging
@Transactional
public void callNextPatient(Long doctorId) {
    log.info("🔵 callNextPatient() called for doctor: {}", doctorId);
    
    Token current = findCurrentInProgress(doctorId);
    log.info("🔵 Current token: {}", current.getTokenNumber());
    
    current.setStatus(COMPLETED);
    save(current);
    log.info("🟢 Marked as COMPLETED: {}", current.getTokenNumber());
    
    Token next = getNextWaitingToken(doctorId);
    log.info("🔵 Next token: {}", next.getTokenNumber());
    
    next.setStatus(IN_PROGRESS);
    save(next);
    log.info("🟢 Marked as IN_PROGRESS: {}", next.getTokenNumber());
    
    // Broadcast
    String destination = "/topic/token/" + next.getTokenNumber();
    log.info("📢 Broadcasting to: {}", destination);
    
    messagingTemplate.convertAndSend(destination, tokenResponse);
    log.info("📢 Broadcast sent successfully");
}
```

---

### Q24: Two receptionists generate tokens simultaneously and get duplicate numbers. Fix this.

**A:**

**Root Cause:**

The token counter isn't protected from concurrent access:

```
Receptionist 1              Receptionist 2
─────────────────           ─────────────────
SELECT token_counter        
FROM doctors                
WHERE id=5
Result: 0                   
                            SELECT token_counter
                            WHERE id=5
                            Result: 0 ❌ (stale!)
                            
counter = 0 + 1 = 1         counter = 0 + 1 = 1
UPDATE doctors SET
  token_counter=1
                            UPDATE doctors SET
                              token_counter=1 ❌ (same value!)

Generate: TOKEN-001 ✅      Generate: TOKEN-001 ❌ DUPLICATE!
```

**Solution: Pessimistic Locking**

```java
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    
    // ✅ CORRECT - Locks the row before reading
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Doctor d WHERE d.id = :id")
    Optional<Doctor> findByIdForUpdate(@Param("id") Long id);
}

@Service
@Transactional
public class QueueService {
    
    public String generateTokenNumber(Doctor doctor) {
        // This lock is held for the entire method
        Doctor doctorLocked = doctorRepository.findByIdForUpdate(doctor.getId())
            .orElseThrow();
        
        // Database row is locked here
        //  ├─ Other threads wait
        
        // Reset counter if new day
        LocalDate today = LocalDate.now();
        if (!doctorLocked.getLastTokenDate().equals(today)) {
            doctorLocked.setTokenCounter(0);
            doctorLocked.setLastTokenDate(today);
        }
        
        // Increment atomically
        doctorLocked.setTokenCounter(doctorLocked.getTokenCounter() + 1);
        doctorRepository.save(doctorLocked);  // Flush immediately
        
        // Lock released here
        
        return doctor.getDepartment().getPrefix() + 
               "-" + today.format(DateTimeFormatter.BASIC_ISO_DATE) +
               "-" + String.format("%03d", doctorLocked.getTokenCounter());
    }
}
```

**Verification:**

```
With Pessimistic Locking:

Receptionist 1              Receptionist 2
─────────────────────       ──────────────────
SELECT...FOR UPDATE         SELECT...FOR UPDATE ⏳
(acquires lock)             (waits here)
                            
counter = 0 + 1 = 1         (still waiting...)
UPDATE counter=1
COMMIT
Release lock ✅
                            SELECT...FOR UPDATE ✅
                            (lock released, continues)
                            
Generate: TOKEN-001 ✅      counter = 1 + 1 = 2
                            UPDATE counter=2
                            Generate: TOKEN-002 ✅
                            
Result: Unique tokens!
```

**Testing:**

```java
@SpringBootTest
public class ConcurrencyTest {
    
    @Test
    public void testConcurrentTokenGeneration() throws InterruptedException {
        Doctor doctor = doctorRepository.save(new Doctor(...)  );
        Set<String> tokens = ConcurrentHashMap.newKeySet();
        
        // Spawn 100 threads
        ExecutorService executor = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                String token = queueService.generateTokenNumber(doctor);
                tokens.add(token);
            });
        }
        
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        // All 100 tokens should be unique
        assertEquals(100, tokens.size());  // ✅ Passes
    }
}
```

---

### Q25: System crashes during peak hours (9 AM). Root cause & solution?

**A:**

**Potential Causes (Ranked by Likelihood):**

```
1. 🔴 Database Connection Pool Exhaustion (70%)
   └─ Too many open connections, new requests timeout

2. 🔴 Out of Memory (OOM) (15%)
   └─ WebSocket messages accumulate, heap fills up

3. 🟡 Slow Database Queries (10%)
   └─ Queue sorting on unindexed columns, full table scans

4. 🟡 WebSocket Broadcasting Overload (3%)
   └─ Too many clients, message broker overwhelmed

5. 🟢 Network/Firewall Issue (2%)
   └─ Connection limits reached
```

**Diagnosis Steps:**

```bash
# 1. Check database connections
mysql> SHOW PROCESSLIST;
# Look for: "Too many connections" error

# 2. Check JVM memory
jstat -gc -h10 $(pgrep -f HospitalQueueApplication)
# Look for: GC overhead high, heap near limit

# 3. Check database query performance
EXPLAIN SELECT * FROM tokens 
        WHERE doctor_id=5 
        ORDER BY priority, created_at;
# Look for: "Full Table Scan" (bad)

# 4. Check backend logs
tail -f backend.log | grep -i "error\|timeout\|exception"
```

**Solution: Database Connection Pool Configuration**

```properties
# application.properties

# Connection Pool Size
spring.datasource.hikari.maximum-pool-size=30  # Default 10 (too small!)
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000  # 30 seconds

# Query Timeout
spring.datasource.hikari.max-lifetime=1800000  # 30 minutes

# Connection Validation
spring.datasource.hikari.connection-test-query=SELECT 1
spring.datasource.hikari.health-check-duration=30000
```

```java
@Bean
public DataSource dataSource() {
    HikariConfig config = new HikariConfig();
    config.setMaximumPoolSize(30);  // Up from default 10
    config.setMinimumIdle(5);
    config.setConnectionTimeout(30000);
    return new HikariDataSource(config);
}
```

**Solution: Memory Optimization**

```bash
# JVM startup parameters
java -Xms512m -Xmx2048m \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -jar backend.jar
```

```properties
# application.properties - WebSocket message caching
spring.websocket.message-cache-size=100  # Limit in-memory messages
```

**Solution: Query Optimization**

```java
// Add composite index
ALTER TABLE tokens 
ADD INDEX idx_doctor_priority_created 
(doctor_id, priority, created_at);

// Use indexed query
@Query("SELECT t FROM Token t WHERE t.doctor.id = :doctorId " +
       "ORDER BY t.priority ASC, t.createdAt ASC")
List<Token> findByDoctorIdOrdered(@Param("doctorId") Long doctorId);
```

**Solution: Load Balancing**

```
Before (Single Server):
Client → API Server → Database
         (bottleneck!)

After (Load Balanced):
Client → Load Balancer
              │
           ┌──┼──┬──┐
           ▼  ▼  ▼  ▼
      API-1 API-2 API-3 API-4
              │
              ▼
           Database
```

**Monitoring Setup:**

```yaml
# application.yml - Enable metrics
management:
  endpoints:
    web:
      exposure:
        include: metrics, health, prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

Visit: `http://localhost:8081/actuator/metrics` to monitor:
- `http.server.requests` (request count, latency)
- `process.memory` (heap usage)
- `hikaricp.connections` (DB pool usage)

---

## 10. BEHAVIORAL & SITUATIONAL

### Q26: How would you handle a database migration with zero downtime?

**A:**

**Blue-Green Deployment Strategy:**

```
Phase 1: Prepare (Blue = Old, Green = New)
├─ Setup new database with old schema
├─ Deploy backend instances to Green
├─ Run data migration scripts
└─ Verify Green database

Phase 2: Cutover (Switch traffic)
├─ Update load balancer → route to Green
├─ Old Blue database kept as fallback
└─ Monitor for issues

Phase 3: Cleanup (Remove Blue after validation)
├─ Wait 1 hour (ensure no issues)
├─ Archive Blue database
└─ Done
```

**Example Migration (Add column without downtime):**

```sql
-- Step 1: Add new column with default value (non-blocking)
ALTER TABLE patients ADD COLUMN updated_at TIMESTAMP 
DEFAULT CURRENT_TIMESTAMP;

-- Step 2: Backfill existing rows (batched to avoid locks)
UPDATE patients SET updated_at = CURRENT_TIMESTAMP 
WHERE updated_at IS NULL LIMIT 10000;
-- Repeat until all rows updated

-- Step 3: Update application code (assumes column exists)
-- No downtime because column exists with default value

-- Step 4: Make column NOT NULL (optional)
ALTER TABLE patients MODIFY COLUMN updated_at TIMESTAMP NOT NULL;
```

**Flyway/Liquibase Versioning:**

```java
// V1_0_0__InitialSchema.sql (old)
CREATE TABLE tokens (...);

// V1_0_1__AddUpdatedAtColumn.sql (new migration)
ALTER TABLE patients ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
```

On deploy: Flyway automatically runs new migrations, stops if error occurs.

---

### Q27: A team member broke production. How to investigate and recover?

**A:**

**Immediate Response (First 5 minutes):**

```
1. 🚨 Declare Incident
   └─ Post in Slack: "INCIDENT: Queue system down, ETA 30 min"

2. 🔄 Rollback (if possible)
   └─ git revert <bad-commit>
   └─ redeploy to production
   └─ Most issues resolved in 5 minutes

3. 📊 Investigate Root Cause
   └─ tail -f production.log
   └─ check database connections
   └─ check error rates in monitoring
```

**Investigation (5-30 minutes):**

```
From logs, identify:
├─ What changed? (git diff HEAD~1 HEAD)
├─ When did it break? (timestamp)
├─ Who deployed? (git log --oneline)
├─ What errors? (stack trace)
└─ Impact? (affected features)

Common causes:
1. Typo in code (syntax error)
2. Forgot migration (missing column)
3. Wrong config (database password)
4. Resource limit exceeded (memory, connections)
5. API breaking change (old client incompatible)
```

**Recovery Process:**

```
If simple fix (< 15 min):
├─ Fix the issue
├─ Run tests locally
├─ Deploy fixed version
└─ Verify in production

If complex issue (> 15 min):
├─ Rollback to last known good
├─ Once production stable, investigate offline
├─ Fix on feature branch
├─ Deploy with extra testing
└─ Post-mortem meeting afterward
```

**Post-Mortem Template:**

```
Title: Hospital Queue System Down - April 22, 9:30 AM

Timeline:
- 09:30 - Deploy backend v2.1.0
- 09:31 - Alert: API returns 500 errors
- 09:32 - Rollback to v2.0.9
- 09:35 - System stable

Root Cause:
Migration script not executed for new 'status_logs' table
Code assumed table exists → NULL error

What went wrong:
- Forgot to add migration to release notes
- CI/CD didn't validate table exists

How to prevent:
✅ Add unit test that checks table schema
✅ Pre-deployment checklist: migrations verified
✅ Staging environment ≠ production (catch issues early)
✅ Code review should catch schema changes

Action items:
1. [DONE] Fix migration script
2. [Task] Add schema validation test
3. [Task] Update deploy checklist
4. [Review] Code review improvements
```

---

### Q28: How do you handle large data migrations (e.g., 1 million patient records)?

**A:**

**Strategy (Batched Processing):**

```
Problem: UPDATE 1M rows = table lock, blocks all operations

Solution: Batch process in chunks
```

```sql
-- ❌ WRONG - Locks entire table
UPDATE patients SET registration_mode = 'RECEPTIONIST' 
WHERE registration_mode IS NULL;

-- ✅ CORRECT - Batch 1000 rows at a time
DELIMITER //
CREATE PROCEDURE BatchUpdatePatients()
BEGIN
  DECLARE done INT DEFAULT FALSE;
  DECLARE offset_val INT DEFAULT 0;
  
  WHILE NOT done DO
    UPDATE patients 
    SET registration_mode = 'RECEPTIONIST'
    WHERE registration_mode IS NULL
    LIMIT 1000;
    
    IF ROW_COUNT () = 0 THEN
      SET done = TRUE;
    END IF;
    
    SET offset_val = offset_val + 1000;
    
    -- Wait between batches (let other queries run)
    SELECT SLEEP(0.1);
  END WHILE;
END //

CALL BatchUpdatePatients();
```

**Implementation (Backend):**

```java
@Service
public class DataMigrationService {
    private static final int BATCH_SIZE = 1000;
    
    @Transactional
    public void migratePatientRecords(String oldStatus, String newStatus) {
        int offset = 0;
        boolean hasMore = true;
        
        while (hasMore) {
            // Fetch batch
            Pageable pageable = PageRequest.of(
                offset / BATCH_SIZE, 
                BATCH_SIZE
            );
            Page<Patient> batch = patientRepository
                .findByRegistrationMode(oldStatus, pageable);
            
            // Update batch
            List<Patient> updated = batch.getContent()
                .stream()
                .peek(p -> p.setRegistrationMode(newStatus))
                .collect(Collectors.toList());
            
            patientRepository.saveAll(updated);
            patientRepository.flush();  // Commit immediately
            
            log.info("Migrated {} of {} records", 
                offset + BATCH_SIZE, 
                batch.getTotalElements());
            
            hasMore = batch.hasNext();
            offset += BATCH_SIZE;
            
            // Sleep to avoid overload
            Thread.sleep(100);
        }
    }
}
```

**Monitoring Progress:**

```sql
-- Check progress
SELECT COUNT(*) FROM patients 
WHERE registration_mode = 'RECEPTIONIST';

-- Check speed
SHOW PROCESSLIST;  -- See current query
SHOW STATUS LIKE 'Threads_connected';  -- Connection count
```

---

### Q29: Describe your testing strategy for this system.

**A:**

**Testing Pyramid:**

```
        ▲
       / \
      /   \  UI/E2E Tests (10%)
     /     \ ├─ Full user workflows
    /-------\ Selenium/Cypress
   /         \
  /           \
 /             \ Integration Tests (30%)
/───────────────\ ├─ Controller + Service + DB
│               │ ├─ @SpringBootTest
│               │ └─ Test database
├───────────────┤
│               │ Unit Tests (60%)
│               │ ├─ Service logic
│               │ ├─ Utility functions
│               │ └─ @Mock dependencies
└───────────────┘
```

**Unit Tests (QueueService):**

```java
@ExtendWith(MockitoExtension.class)
public class QueueServiceTest {
    
    @Mock private TokenRepository tokenRepository;
    @InjectMocks private QueueService queueService;
    
    @Test
    public void testQueueOrderingByPriority() {
        // Arrange
        List<Token> tokens = Arrays.asList(
            new Token(priority=NORMAL, createdAt=10:00),
            new Token(priority=EMERGENCY, createdAt=10:05),
            new Token(priority=URGENT, createdAt=10:10)
        );
        when(tokenRepository.findByDoctorId(1L))
            .thenReturn(tokens);
        
        // Act
        List<Token> ordered = queueService.getQueueForDoctor(1L);
        
        // Assert
        assertEquals(EMERGENCY, ordered.get(0).getPriority());
        assertEquals(URGENT, ordered.get(1).getPriority());
        assertEquals(NORMAL, ordered.get(2).getPriority());
    }
    
    @Test
    public void testConcurrentTokenGeneration() throws InterruptedException {
        Doctor doctor = new Doctor(id=1, tokenCounter=0, dept=CARDIOLOGY);
        when(doctorRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(doctor));
        
        ExecutorService executor = Executors.newFixedThreadPool(100);
        Set<String> tokens = ConcurrentHashMap.newKeySet();
        
        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                String token = queueService.generateTokenNumber(doctor);
                tokens.add(token);
            });
        }
        
        executor.awaitTermination(10, TimeUnit.SECONDS);
        assertEquals(100, tokens.size());  // All unique
    }
}
```

**Integration Tests (Controller):**

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class DoctorControllerTest {
    
    @Autowired private MockMvc mockMvc;
    @Autowired private DoctorService doctorService;
    @Autowired private TokenRepository tokenRepository;
    
    @Test
    public void testCallNextPatient() throws Exception {
        // Setup test data
        Doctor doctor = doctorRepository.save(new Doctor(...));
        Patient patient = patientRepository.save(new Patient(...));
        Token token = new Token(doctor, patient, status=WAITING);
        tokenRepository.save(token);
        
        // Act
        mockMvc.perform(
            post("/api/doctor/queue/next")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(APPLICATION_JSON)
        )
        .andExpect(status().isOk());
        
        // Assert
        Token updated = tokenRepository.findById(token.getId()).get();
        assertEquals(IN_PROGRESS, updated.getStatus());
    }
}
```

**WebSocket Integration Test:**

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class WebSocketTest {
    
    @LocalServerPort int port;
    private StompSession session;
    
    @Test
    public void testQueueBroadcast() throws Exception {
        StompHeaders headers = new StompHeaders();
        session = stompClient.connect(
            "ws://localhost:" + port + "/ws",
            new StompSessionHandlerAdapter() {},
            headers
        ).get(10, TimeUnit.SECONDS);
        
        List<QueueResponse> received = new ArrayList<>();
        
        session.subscribe("/topic/queue/1", new DefaultStompFrameHandler() {
            public void handleFrame(StompFrame frame) {
                QueueResponse queue = objectMapper.readValue(
                    frame.getPayload(),
                    QueueResponse.class
                );
                received.add(queue);
            }
        });
        
        // Trigger event
        doctorService.callNextPatient(1L);
        
        // Verify broadcast received
        Thread.sleep(1000);
        assertTrue(received.size() > 0);
    }
}
```

**Frontend Tests (Angular):**

```typescript
describe('PatientTrackComponent', () => {
    let component: PatientTrackComponent;
    let fixture: ComponentFixture<PatientTrackComponent>;
    let patientService: jasmine.SpyObj<PatientService>;
    let websocketService: jasmine.SpyObj<WebSocketService>;
    
    beforeEach(() => {
        const patientSpy = jasmine.createSpyObj('PatientService', 
            ['getQueuePosition']);
        const wsSpy = jasmine.createSpyObj('WebSocketService',
            ['subscribeToToken']);
        
        TestBed.configureTestingModule({
            declarations: [PatientTrackComponent],
            providers: [
                { provide: PatientService, useValue: patientSpy },
                { provide: WebSocketService, useValue: wsSpy }
            ]
        });
        
        patientService = TestBed.inject(PatientService) as jasmine.SpyObj<PatientService>;
        websocketService = TestBed.inject(WebSocketService) as jasmine.SpyObj<WebSocketService>;
    });
    
    it('should display queue position from WebSocket', (done) => {
        const mockToken = { position: 3, status: 'WAITING' };
        websocketService.subscribeToToken.and.returnValue(
            of(mockToken)
        );
        
        fixture = TestBed.createComponent(PatientTrackComponent);
        component = fixture.componentInstance;
        component.tokenNumber = 'CARD-20260422-001';
        
        fixture.detectChanges();
        
        fixture.whenStable().then(() => {
            expect(component.queuePosition).toBe(3);
            expect(component.status).toBe('WAITING');
            done();
        });
    });
});
```

---

### Q30: Tell me about a technical decision you'd make differently. Why?

**A:**

**Current Decision: localStorage for JWT Storage**

```typescript
// Current implementation
localStorage.setItem('hq_token', jwt);
localStorage.setItem('hq_user', userJson);
```

**Problem:**

```
1. XSS Vulnerability
   ├─ Any <script> tag in app can steal token
   ├─ localStorage.getItem('hq_token') accessible from JS
   └─ attacker can: send token to their server

2. Not Removed on Browser Close
   ├─ localStorage persists forever
   ├─ If device stolen, token still valid
   └─ Even if user logs out, browser cache might save it

3. No CSRF Protection
   ├─ JWT sent in header (more secure than cookies)
   ├─ But localStorage makes it easier for CSRF
```

**Better Approach: httpOnly Cookies**

```java
// Backend: Set httpOnly cookie
@PostMapping("/api/auth/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    User user = authenticate(request);
    String jwt = jwtService.generateToken(user);
    
    // Create httpOnly, Secure, SameSite cookie
    ResponseCookie cookie = ResponseCookie
        .from("Authorization", jwt)
        .httpOnly(true)            // Cannot be accessed from JS!
        .secure(true)              // HTTPS only
        .path("/")
        .maxAge(Duration.ofHours(24))
        .sameSite("Strict")        // CSRF protection
        .build();
    
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    return ok(new LoginResponse(user));
}
```

```typescript
// Frontend: No manual JWT handling needed!
// Browser automatically sends cookie with every request

// GET /api/doctor/queue
// (Browser sends: Cookie: Authorization=eyJhbGc...)

// On 401: Browser removes cookie,
// Angular redirects to login
```

**Advantages of httpOnly Cookies:**

| Aspect | localStorage | httpOnly Cookie |
|---|---|---|
| **XSS Vulnerability** | ❌ Exposed to JS | ✅ Protected |
| **Browser Close** | ❌ Persists | ✅ Can auto-expire |
| **CSRF Protection** | ⚠️ Medium | ✅ SameSite flag |
| **Ease of Use** | ✅ Simple | ⚠️ More config |
| **Mobile** | ✅ Works | ✅ Works |
| **Cross-Domain** | ❌ blocked | ⚠️ Restricted |

**Another Decision: Pessimistic Locking → Distributed Lock (for scale)**

Currently, pessimistic locking serializes token generation:

```
Thread 1: ████████████ (holds lock 100ms)
Thread 2:              ████████████ (waits 100ms)
Thread 3:                           ████████████ (waits 200ms)

Throughput: 10 tokens/second
```

For 10,000 concurrent users, distributed lock is better:

```java
@Service
public class QueueService {
    @Autowired private RedisTemplate<String, String> redis;
    
    public String generateTokenNumber(Doctor doctor) {
        String lockKey = "token_lock_" + doctor.getId();
        String lockValue = UUID.randomUUID().toString();
        
        try {
            // Try to acquire lock for 5 seconds
            while (!redis.opsForValue()
                .setIfAbsent(lockKey, lockValue, 5, TimeUnit.SECONDS)) {
                Thread.sleep(10);  // Retry after 10ms
            }
            
            // Lock acquired!
            Doctor fresh = doctorRepository.findById(doctor.getId()).get();
            fresh.setTokenCounter(fresh.getTokenCounter() + 1);
            doctorRepository.save(fresh);
            
            return generateToken(fresh);
            
        } finally {
            // Release lock
            redis.delete(lockKey);
        }
    }
}
```

**Trade-offs:**

| Approach | Latency | Throughput | Complexity | Cost |
|---|---|---|---|---|
| Pessimistic Locking | 100ms | 10 tokens/s | Low | None |
| Redis Distributed Lock | 50ms | 20 tokens/s | Medium | Redis server |
| Optimistic Locking + Retry | 10ms | 100 tokens/s | High | CPU (retries) |

---

## SUMMARY

**Key Takeaways for This Project:**

1. **Architecture**: Clean, layered, stateless (horizontal scaling)
2. **Security**: JWT + Spring Security + BCrypt + role-based access
3. **Concurrency**: Pessimistic locking for token generation
4. **Real-time**: WebSocket (STOMP/SockJS) with HTTP polling fallback
5. **Database**: Well-designed schema, composite indexes, foreign key constraints
6. **Frontend**: Lazy loading, guards, interceptors, RxJS observables
7. **Performance**: Database sorting, pagination, caching (potential)
8. **Scalability**: Load balancing, read replicas, message broker distribution

---

*Interview Questions & Answers for SmartApp Hospital Queue Management System*  
*For: Senior Full-Stack Developer / Tech Lead Level*  
*Prepared: 2026-04-22*
