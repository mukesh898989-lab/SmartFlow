# SmartApp - Hospital Queue Management System
## Interview Questions & Answers (Fresher Level)

**Level:** Fresher / Junior Developer (0-2 years experience)  
**Duration:** 30-45 minutes  
**Topics:** Basic concepts, fundamentals, simple problem-solving

---

## TABLE OF CONTENTS

1. [Project Overview & Basics](#1-project-overview--basics)
2. [Frontend - Angular Fundamentals](#2-frontend---angular-fundamentals)
3. [Backend - Spring Boot Basics](#3-backend---spring-boot-basics)
4. [Database & SQL Basics](#4-database--sql-basics)
5. [Authentication & Security (Simple)](#5-authentication--security-simple)
6. [API Concepts](#6-api-concepts)
7. [Real-Time Features (Simple)](#7-real-time-features-simple)
8. [Version Control (Git)](#8-version-control-git)
9. [Testing Basics](#9-testing-basics)
10. [Problem-Solving (Simple Scenarios)](#10-problem-solving-simple-scenarios)

---

## 1. PROJECT OVERVIEW & BASICS

### Q1: What is the SmartApp project? What problem does it solve?

**A:**

SmartApp is a **Hospital Queue Management System** that replaces the old manual paper-based queue system with a digital one.

**The Problem (Before SmartApp):**
- Patients wait in long lines without knowing their turn
- Receptionists manually write down patient names
- Doctors don't know who's next
- Long wait times, lost paperwork, chaos

**The Solution (SmartApp):**
- Patients get a **digital token** (e.g., CARD-20260422-001)
- System automatically orders patients by **priority**
- Patients can **track their position** in real-time
- Doctors see **ordered queue on screen**
- Receptionists manage tokens easily

**Example User Flow:**

```
Patient walks in
    ↓
Registers (name, phone)
    ↓
Selects doctor & department
    ↓
Gets token: CARD-20260422-001
    ↓
Sees: "You are #3 in queue"
    ↓
Doctor calls: "Token 001!"
    ↓
Patient goes to doctor
    ↓
Doctor marks COMPLETE
    ↓
Next patient's token is called
```

---

### Q2: What are the main roles in the system?

**A:**

SmartApp has **4 main user roles**:

| Role | What They Do | Where They Log In |
|---|---|---|
| **Admin** | Create users, manage departments, view all queues | `/admin` dashboard |
| **Receptionist** | Register patients offline, generate tokens, manage queue priority | `/receptionist` portal |
| **Doctor** | View their queue, call next patient, mark as complete | `/doctor` portal |
| **Patient** | Self-register online, generate token, track position | `/patient` (public) |

**Example Workflow by Role:**

```
Admin:
  1. Creates "Cardiology" department
  2. Creates "Dr. Smith" doctor account
  3. Creates "Jane" receptionist account
  4. Views all patients waiting across all doctors

Receptionist (Jane):
  1. Patient "Ali" walks in
  2. Jane registers: name="Ali", phone="9876543210"
  3. Jane generates token: CARD-20260422-001
  4. Ali waits and tracks progress

Doctor (Dr. Smith):
  1. Sees patient "Ali" is next in queue
  2. Calls "Token 001!"
  3. Ali walks in
  4. After consultation, marks "Complete"
  5. Next patient's token appears

Patient (Self-Service):
  1. Visits website
  2. Enters name and phone (no login!)
  3. Selects "Cardiology" & "Dr. Smith"
  4. Gets token: CARD-20260422-001
  5. Tracks position in queue in real-time
```

---

### Q3: What technologies are used in this project?

**A:**

**Frontend (What users see in browser):**
- **Angular 16** - JavaScript framework for building UI
- **TypeScript** - Typed version of JavaScript (safer)
- **RxJS** - Library for handling real-time data streams
- **HTML + CSS** - Markup and styling

**Backend (Server-side logic):**
- **Spring Boot** - Java framework for building REST APIs
- **Java 17** - Programming language
- **MySQL** - Database to store patient, doctor, token data
- **JWT** - Secure authentication tokens

**Real-Time Communication:**
- **WebSocket** - Technology for live updates (instant notifications)
- **STOMP** - Protocol over WebSocket (like mail protocol)

**Simple Analogy:**

```
Frontend = Restaurant Menu (HTML + CSS)
Backend = Kitchen (Java, processes orders)
Database = Storage room (MySQL, stores food)
WebSocket = Phone line (Patient gets instant queue updates)
```

---

### Q4: Can you draw/describe the system flow?

**A:**

**Simple System Flow:**

```
PATIENT SELF-SERVICE FLOW:

Browser (Patient)          Backend (Server)       Database (MySQL)
─────────────────          ────────────────       ────────────────

1. User enters name
   & phone
        │
        ├─→ POST /api/patient/register
                    │
                    ├─→ Check if patient exists
                    ├─→ Create new patient record ──→ INSERT INTO patients
                    │
        ←─── Returns patient ID
        │
2. User selects doctor
   & department
        │
        ├─→ POST /api/patient/generate-token
                    │
                    ├─→ Find doctor
                    ├─→ Generate token number: CARD-20260422-001
                    ├─→ Create token record ───────→ INSERT INTO tokens
                    │
        ←─── Returns token details
        │
3. User gets token
   "You are #3 in queue"
        │
        ├─→ GET /api/patient/queue-position/CARD-20260422-001
                    │
                    ├─→ Count patients ahead ──────→ SELECT COUNT FROM tokens
                    ├─→ Get position = 3
                    │
        ←─── Returns position
        │
4. Real-time updates
   (WebSocket)
        │
        ├─→ SUBSCRIBE /topic/token/CARD-20260422-001
                    │
                    ├─→ Doctor marks patients COMPLETE
                    ├─→ Queue advances
                    ├─→ Broadcast to all subscribers
                    │
        ←─── Position updates: #3 → #2 → #1 → "Your turn!"
```

---

## 2. FRONTEND - ANGULAR FUNDAMENTALS

### Q5: What is Angular? Why use a framework like Angular?

**A:**

**What is Angular?**

Angular is a **JavaScript framework** that helps build interactive websites. It allows you to build dynamic, fast web applications without manually updating the HTML every time data changes.

**Without Angular (Vanilla JavaScript):**

```html
<!-- Manual DOM manipulation -->
<div id="queuePosition">Position: 5</div>

<script>
  // When queue updates, manually change HTML
  document.getElementById('queuePosition').innerHTML = 'Position: 4';
  document.getElementById('queuePosition').innerHTML = 'Position: 3';
  document.getElementById('queuePosition').innerHTML = 'Position: 2';
  // ... lots of manual code!
</script>
```

**With Angular (Automatic):**

```typescript
// Component TypeScript
export class PatientTrackComponent {
  queuePosition = 5;
  
  ngOnInit() {
    // Subscribe to real-time updates
    this.patientService.getQueueUpdates()
      .subscribe(update => {
        this.queuePosition = update.position;  // Auto-updates HTML!
      });
  }
}
```

```html
<!-- HTML - automatically updates when queuePosition changes -->
<div>Position: {{ queuePosition }}</div>
```

**Why Use Angular:**

| Feature | Benefit |
|---|---|
| **Components** | Reusable pieces (don't repeat code) |
| **Data Binding** | HTML auto-updates when data changes |
| **Routing** | Navigate between pages (/admin, /doctor) |
| **Services** | Share data between components easily |
| **Forms** | Handle user input, validation automatically |
| **HTTP Client** | Talk to backend API easily |

---

### Q6: Explain Angular components. What's a component?

**A:**

An **Angular Component** is a reusable piece of a webpage. It has **3 parts**:

1. **TypeScript File** (.ts) - Logic
2. **HTML File** (.html) - Template/View
3. **CSS File** (.css) - Styling

**Example: Patient Queue Position Component**

**Component File (patient-track.component.ts):**

```typescript
import { Component, OnInit } from '@angular/core';
import { PatientService } from '../services/patient.service';

@Component({
  selector: 'app-patient-track',           // HTML tag name
  templateUrl: './patient-track.component.html',
  styleUrls: ['./patient-track.component.css']
})
export class PatientTrackComponent implements OnInit {
  // Data (properties)
  tokenNumber: string = '';
  queuePosition: number = 0;
  patientName: string = '';
  
  // Constructor (injected services)
  constructor(private patientService: PatientService) { }
  
  // Runs when component loads
  ngOnInit(): void {
    this.getQueuePosition();
  }
  
  // Method (function)
  getQueuePosition(): void {
    this.patientService.getQueuePosition(this.tokenNumber)
      .subscribe(data => {
        this.queuePosition = data.position;
        this.patientName = data.patientName;
      });
  }
}
```

**Template File (patient-track.component.html):**

```html
<div class="queue-container">
  <h1>Your Queue Status</h1>
  
  <p>Token: {{ tokenNumber }}</p>
  <p>Name: {{ patientName }}</p>
  <p>Position: {{ queuePosition }}</p>
  
  <button (click)="getQueuePosition()">
    Refresh Queue Position
  </button>
</div>
```

**CSS File (patient-track.component.css):**

```css
.queue-container {
  border: 1px solid #ccc;
  padding: 20px;
  margin: 10px;
}

p {
  font-size: 16px;
  margin: 10px 0;
}
```

**When you use this component:**

```html
<!-- In another page -->
<app-patient-track></app-patient-track>

<!-- Renders as: -->
<div class="queue-container">
  <h1>Your Queue Status</h1>
  <p>Token: CARD-20260422-001</p>
  <p>Name: Ali</p>
  <p>Position: 3</p>
  <button>Refresh Queue Position</button>
</div>
```

---

### Q7: What is routing in Angular? Why do we need it?

**A:**

**Routing** means navigating between different pages/screens in your app WITHOUT refreshing the browser.

**Without Routing (Old Websites):**

```
User clicks "Admin Dashboard"
    ↓
Browser refreshes (page blinks)
    ↓
New page loads from server
    ↓
Takes 2-3 seconds
```

**With Angular Routing (Modern SPA):**

```
User clicks "Admin Dashboard"
    ↓
JavaScript changes what's displayed (instant!)
    ↓
No page refresh (smooth experience)
    ↓
Takes 100ms
```

**How Routing Works in SmartApp:**

```typescript
// app-routing.module.ts
const routes: Routes = [
  {
    path: '',
    redirectTo: '/auth/login',
    pathMatch: 'full'
  },
  
  {
    path: 'auth/login',
    component: LoginComponent    // Login page
  },
  
  {
    path: 'admin',
    component: AdminDashboardComponent   // Admin page
  },
  
  {
    path: 'doctor',
    component: DoctorQueueComponent      // Doctor page
  },
  
  {
    path: 'patient',
    component: PatientHomeComponent      // Patient page
  }
];
```

**In HTML (Navigation):**

```html
<!-- Navigation menu -->
<nav>
  <a routerLink="/admin">Admin Dashboard</a>
  <a routerLink="/doctor">Doctor Queue</a>
  <a routerLink="/patient">Patient Portal</a>
</nav>

<!-- Content changes here based on route -->
<router-outlet></router-outlet>
```

**Route Flow:**

```
URL in browser: http://localhost:4200/admin
                        ↓
    Angular reads route: /admin
                        ↓
    Shows: AdminDashboardComponent
                        ↓
    Browser URL remains: http://localhost:4200/admin
                        ↓
    NO page refresh! (Just swaps component)
```

---

### Q8: Explain services in Angular. Why do we use them?

**A:**

A **Service** is a class that handles **shared logic** and **data**. It's used to:
- Make API calls to backend
- Share data between components
- Reuse logic across app

**Without Services (Bad Practice):**

```typescript
// admin.component.ts
export class AdminComponent {
  users: User[] = [];
  
  ngOnInit() {
    // Make API call here
    this.http.get('http://localhost:8081/api/admin/users')
      .subscribe(data => {
        this.users = data;
      });
  }
}

// receptionist.component.ts
export class ReceptionistComponent {
  users: User[] = [];
  
  ngOnInit() {
    // Same API call duplicated!
    this.http.get('http://localhost:8081/api/admin/users')
      .subscribe(data => {
        this.users = data;
      });
  }
}
```

**Problem:** Code duplication, hard to maintain!

**With Services (Good Practice):**

```typescript
// admin.service.ts (Reusable service)
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  constructor(private http: HttpClient) { }
  
  getUsers() {
    return this.http.get('http://localhost:8081/api/admin/users');
  }
  
  createUser(user: User) {
    return this.http.post('http://localhost:8081/api/admin/users', user);
  }
}

// admin.component.ts (Use service)
export class AdminComponent implements OnInit {
  users: User[] = [];
  
  constructor(private adminService: AdminService) { }
  
  ngOnInit() {
    this.adminService.getUsers()
      .subscribe(data => {
        this.users = data;
      });
  }
}

// receptionist.component.ts (Reuse same service)
export class ReceptionistComponent implements OnInit {
  users: User[] = [];
  
  constructor(private adminService: AdminService) { }
  
  ngOnInit() {
    this.adminService.getUsers()  // No duplication!
      .subscribe(data => {
        this.users = data;
      });
  }
}
```

**Benefits:**

```
✅ No code duplication
✅ Easy to maintain (update in one place)
✅ Consistent API calls
✅ Can be tested separately
```

---

## 3. BACKEND - SPRING BOOT BASICS

### Q9: What is Spring Boot? Why use it for backend?

**A:**

**Spring Boot** is a Java framework that makes building **REST APIs** (backend servers) very easy.

**What's a REST API?**

A REST API is a way for frontend and backend to communicate using HTTP requests:

```
Frontend (Browser)          Backend Server
──────────────────          ──────────────
GET /api/doctor/queue   ──→
        (request)
                        ←── { doctor's queue data }
                            (response)

POST /api/patient/register ──→
     { name, phone }
                        ←── { success: true }
```

**What Spring Boot Does:**

| Task | Without Spring Boot | With Spring Boot |
|---|---|---|
| Create API endpoint | 50 lines of config | 5 lines |
| Handle database | Write SQL manually | Auto-generates |
| Authentication | 200+ lines | @PreAuthorize annotation |
| Error handling | Manual try-catch | Automatic |
| Deploy | Complex setup | One command: java -jar |

**Simple Spring Boot Example:**

```java
@RestController
@RequestMapping("/api")
public class PatientController {
  
  @Autowired
  private PatientService patientService;
  
  // GET /api/patient/token/CARD-20260422-001
  @GetMapping("/patient/token/{tokenNumber}")
  public ResponseEntity<?> getTokenStatus(
      @PathVariable String tokenNumber) {
    
    Token token = patientService.getToken(tokenNumber);
    return ResponseEntity.ok(token);
  }
  
  // POST /api/patient/register
  @PostMapping("/patient/register")
  public ResponseEntity<?> registerPatient(
      @RequestBody PatientRequest request) {
    
    Patient patient = patientService.register(request);
    return ResponseEntity.ok(patient);
  }
}
```

**Run Backend:**

```bash
# That's it! Server starts on http://localhost:8081
java -jar target/SmartApp.jar
```

---

### Q10: What's a REST API endpoint? Give examples.

**A:**

A **REST API endpoint** is a URL that the frontend can call to get data or perform actions on the backend.

**Structure of an Endpoint:**

```
GET /api/patient/token/CARD-20260422-001
 │    │   │      │
 │    │   │      └─ Resource ID
 │    │   └─ Resource name
 │    └─ API prefix
 └─ HTTP method (GET, POST, PUT, DELETE)
```

**HTTP Methods Explained:**

| Method | Purpose | Example |
|---|---|---|
| **GET** | Retrieve data | `GET /api/patient/token/CARD-001` - Get token details |
| **POST** | Create new data | `POST /api/patient/register` - Create new patient |
| **PUT** | Update data | `PUT /api/patient/1` - Update patient info |
| **DELETE** | Delete data | `DELETE /api/token/5` - Remove token |

**SmartApp API Examples:**

**1. Patient Self-Service - Get Token**

```
Request:
POST /api/patient/register
Content-Type: application/json

{
  "name": "Ali",
  "phone": "9876543210"
}

Response (Success):
{
  "patientId": 42,
  "name": "Ali",
  "registeredAt": "2026-04-22T10:30:00"
}

Response (Error):
{
  "error": "Phone number already registered",
  "status": 400
}
```

**2. Doctor - Get Queue**

```
Request:
GET /api/doctor/queue
Authorization: Bearer <JWT_TOKEN>

Response:
[
  {
    "tokenId": 1,
    "tokenNumber": "CARD-20260422-001",
    "patientName": "Ali",
    "priority": "EMERGENCY",
    "position": 1
  },
  {
    "tokenId": 2,
    "tokenNumber": "CARD-20260422-002",
    "patientName": "Bob",
    "priority": "NORMAL",
    "position": 2
  }
]
```

**3. Patient - Track Position**

```
Request:
GET /api/patient/queue-position/CARD-20260422-001

Response:
{
  "tokenNumber": "CARD-20260422-001",
  "position": 3,
  "status": "WAITING",
  "doctorName": "Dr. Smith",
  "estimatedWaitTime": "45 minutes"
}
```

---

### Q11: What is @PreAuthorize in Spring Boot? Why do we need it?

**A:**

**@PreAuthorize** is a security annotation that controls **who can access** an endpoint based on their **role**.

**Example Problem (Without @PreAuthorize):**

```
Admin creates doctor endpoint:
POST /api/admin/doctors

If anyone can access this:
- Hacker can create fake doctors!
- Patient can view admin panel!
- Security problem! 🚨
```

**Solution: Use @PreAuthorize**

```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {
  
  // ✅ Only ADMIN role can access
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/doctors")
  public ResponseEntity<?> createDoctor(@RequestBody DoctorRequest req) {
    return ResponseEntity.ok(doctorService.create(req));
  }
  
  // ✅ Only ADMIN or RECEPTIONIST can access
  @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST')")
  @GetMapping("/tokens")
  public ResponseEntity<?> getTokens() {
    return ResponseEntity.ok(tokenService.getAll());
  }
}
```

**What Happens:**

```
Scenario 1: Admin accesses /api/admin/doctors
├─ Spring checks: Does user have ADMIN role?
├─ YES! ✅
└─ Access granted, endpoint runs

Scenario 2: Patient tries to access /api/admin/doctors
├─ Spring checks: Does user have ADMIN role?
├─ NO! ❌
└─ Access denied, returns 403 Forbidden error
```

**Summary Table:**

| Role | Can Access |
|---|---|
| `/api/admin/**` | ADMIN only |
| `/api/doctor/queue` | DOCTOR only |
| `/api/receptionist/**` | RECEPTIONIST only |
| `/api/patient/**` | Everyone (public) |

---

## 4. DATABASE & SQL BASICS

### Q12: What is a database? What's MySQL?

**A:**

A **Database** is like a digital filing cabinet that stores all data permanently.

**Without Database (Bad):**

```java
List<Patient> patients = new ArrayList<>();  // Just memory

// When app restarts, all data is LOST! 😱
```

**With Database (Good):**

```
App stores data in MySQL (hard drive)
    ↓
Even if app crashes/restarts
    ↓
Data is SAFE and permanent ✅
```

**What's MySQL?**

MySQL is a **Relational Database** - it stores data in **tables** (like Excel spreadsheets).

**Example: Patients Table**

```
┌────┬──────────┬──────────────┬────────────═
│ id │ name     │ phone        │ email      │
├────┼──────────┼──────────────┼────────────┤
│ 1  │ Ali      │ 9876543210   │ ali@em.com │
│ 2  │ Bob      │ 9123456789   │ bob@em.com │
│ 3  │ Charlie  │ 9988776655   │ char@em.co │
└────┴──────────┴──────────────┴────────────┘

Each row = one patient
Each column = patient info
```

**Another Example: Tokens Table**

```
┌────┬─────────────────────┬─────────┬──────────────┐
│ id │ token_number        │ status  │ patient_id   │
├────┼─────────────────────┼─────────┼──────────────┤
│ 1  │ CARD-20260422-001   │ WAITING │ 1 (Ali)      │
│ 2  │ CARD-20260422-002   │ WAITING │ 2 (Bob)      │
│ 3  │ CARD-20260422-003   │ COMPLETED│ 3 (Charlie) │
└────┴─────────────────────┴─────────┴──────────────┘
```

---

### Q13: What is a table? What are rows and columns?

**A:**

**Table** = Collection of related data (like Excel sheet)

**Column** = Property/Attribute (vertical)  
**Row** = One record (horizontal)  
**Cell** = Single value

**Doctors Table Example:**

```
        Column names (what data is stored)
        ↓        ↓              ↓        ↓
    ┌────────────────────────────────────────────┐
    │ id │ name      │ specialization │ dept_id  │
    ├────────────────────────────────────────────┤ ← Row 1
    │ 1  │ Dr. Smith │ Cardiologist   │ 1        │
    ├────────────────────────────────────────────┤ ← Row 2
    │ 2  │ Dr. Jones │ Orthopedic     │ 2        │
    ├────────────────────────────────────────────┤ ← Row 3
    │ 3  │ Dr. Brown │ Pediatrician   │ 3        │
    └────────────────────────────────────────────┘

Cell "Dr. Smith" = intersection of Row 1 and Column 2
```

**Types of Columns:**

| Type | Meaning | Example |
|---|---|---|
| **INT** | Integer/Number | id: 1, 2, 3 |
| **VARCHAR** | Text | name: "Dr. Smith" |
| **TIMESTAMP** | Date & Time | created_at: 2026-04-22 10:30:00 |
| **BOOLEAN** | True/False | active: TRUE |
| **ENUM** | Predefined options | status: WAITING, IN_PROGRESS |

---

### Q14: What are Primary Keys and Foreign Keys?

**A:**

**Primary Key (PK):** Unique identifier for each row - **no duplicates allowed**

```
Doctors Table:
┌────┬───────────┐
│ id │ name      │  ← id is PRIMARY KEY (unique)
├────┼───────────┤
│ 1  │ Dr. Smith │
│ 2  │ Dr. Jones │
│ 3  │ Dr. Brown │
│ 1  │ ??? INVALID! id must be unique ❌
└────┴───────────┘
```

**Foreign Key (FK):** Reference to another table's Primary Key - **creates relationships**

```
Tokens Table:
┌────┬──────────────────┬────────┬─────────────┐
│ id │ token_number     │ status │ doctor_id   │
├────┼──────────────────┼────────┼─────────────┤
│ 1  │ CARD-20260422-001│ WAITING│ 1           │ ← Foreign Key (refers to doctor id 1)
│ 2  │ CARD-20260422-002│ WAITING│ 2           │ ← Foreign Key (refers to doctor id 2)
│ 3  │ CARD-20260422-003│ COMPLETED│ 1         │ ← Foreign Key (refers to doctor id 1)
└────┴──────────────────┴────────┴─────────────┘
       ↑
       Points to Doctors table
```

**Relationship Visualization:**

```
One Doctor has Many Tokens (1 to Many relationship)

Doctors Table:            Tokens Table:
┌────┬───────────┐       ┌────┬──────────┬──────────┐
│ id │ name      │       │ id │ token_no │ doctor_id│
├────┼───────────┤       ├────┼──────────┼──────────┤
│ 1  │ Dr. Smith │◄──────┤ 1  │ CARD-001 │ 1        │
│    │           │       │ 2  │ CARD-002 │ 1        │
│    │           │       │ 3  │ CARD-003 │ 1        │
└────┴───────────┘       └────┴──────────┴──────────┘
       ↑                           ↓
    Many tokens point to one doctor
```

**Why Use Foreign Keys?**

```
Without Foreign Key (Bad):
INSERT INTO tokens (doctor_id) VALUES (999);  // Doctor 999 doesn't exist!
→ Data is invalid!

With Foreign Key (Good):
INSERT INTO tokens (doctor_id) VALUES (999);
→ Database error! Doctor 999 doesn't exist! ✅
```

---

### Q15: Write a simple SELECT query. What does it do?

**A:**

**SELECT** is a SQL command to **retrieve/fetch data** from database.

**Basic Syntax:**

```sql
SELECT column1, column2 FROM table_name;
```

**Simple Examples:**

**1. Get all patients**

```sql
SELECT * FROM patients;

↓ Returns all columns for all patients:
┌────┬──────┬────────────┐
│ id │ name │ phone      │
├────┼──────┼────────────┤
│ 1  │ Ali  │ 9876543210 │
│ 2  │ Bob  │ 9123456789 │
│ 3  │ Charlie │ 998877665│
└────┴──────┴────────────┘
```

**2. Get specific columns only**

```sql
SELECT name, phone FROM patients;

↓ Returns only name and phone:
┌──────────┬────────────┐
│ name     │ phone      │
├──────────┼────────────┤
│ Ali      │ 9876543210 │
│ Bob      │ 9123456789 │
│ Charlie  │ 9988776655 │
└──────────┴────────────┘
```

**3. Get specific patient (WHERE clause)**

```sql
SELECT * FROM patients WHERE id = 1;

↓ Returns only patient with id 1:
┌────┬──────┬────────────┐
│ id │ name │ phone      │
├────┼──────┼────────────┤
│ 1  │ Ali  │ 9876543210 │
└────┴──────┴────────────┘
```

**4. Get tokens ordered by priority**

```sql
SELECT * FROM tokens 
WHERE status = 'WAITING' 
ORDER BY priority ASC;

↓ Returns waiting tokens sorted by priority:
EMERGENCY first → URGENT → NORMAL
```

**5. Count tokens for a doctor**

```sql
SELECT COUNT(*) FROM tokens WHERE doctor_id = 1;

↓ Returns: 5 (doctor 1 has 5 tokens)
```

---

## 5. AUTHENTICATION & SECURITY (SIMPLE)

### Q16: What is authentication? Why do we need it?

**A:**

**Authentication** means verifying **who you are** - proving your identity.

**Real-World Analogy:**

```
Airport security:
   ↓ "Who are you?"
You: "I'm Ali"
   ↓ "Prove it"
You: Show passport
   ↓ Checks passport is real
   ↓ "OK, you're Ali. Welcome!"
   
Same in SmartApp:
   ↓ "Who are you?"
You: "admin@hospital.com"
   ↓ "Prove it"
You: Enter password "Admin@123"
   ↓ App checks password is correct
   ↓ "OK, you're logged in!"
```

**Why Do We Need It?**

```
WITHOUT authentication (Bad):
├─ Anyone can pretend to be admin
├─ Hacker access patient data
├─ Delete doctors
└─ System chaos! 😱

WITH authentication (Safe):
├─ Only admin with correct password can login
├─ Patient can't access doctor portal
├─ Data is protected ✅
```

---

### Q17: What's the difference between Authentication and Authorization?

**A:**

| Authentication | Authorization |
|---|---|
| **Proves WHO you are** | **Proves WHAT you can do** |
| "Are you Ali?" | "Can Ali access /admin?" |
| Uses: password, fingerprint | Uses: roles (Admin, Doctor, etc) |
| Happens first | Happens after authentication |

**Example Flow:**

```
Step 1: Authentication (Login)
├─ User enters: admin@hospital.com + password
├─ Browser checks database
├─ Password matches? YES ✅
├─ User is authenticated (proven to be admin)
└─ Generate login token

Step 2: Authorization (Check permissions)
├─ User tries to access /api/admin/users
├─ System checks: Does this user have ADMIN role?
├─ YES ✅
├─ User is authorized
└─ Access granted!

vs.

Scenario: Patient tries same endpoint
├─ Step 1: Patient provides email + password
├─ Authenticated as patient ✅
├─ Step 2: Tries to access /api/admin/users
├─ System checks: Does patient have ADMIN role?
├─ NO ❌
├─ NOT authorized
└─ Access denied! 403 error
```

---

### Q18: What's a password and why hash it?

**A:**

A **password** is a secret string that only you know.

**Storing Passwords - WRONG Way (Dangerous):**

```
Database:
┌─────┬──────────────────┐
│ id  │ password         │
├─────┼──────────────────┤
│ 1   │ Admin@123        │  ← PLAIN TEXT! 😱
│ 2   │ Secret@pass      │
└─────┴──────────────────┘

If hacker gets database:
├─ Gets all passwords in plain text
├─ Can login as anyone
└─ Disaster!
```

**Storing Passwords - RIGHT Way (Hashing):**

```
Database:
┌─────┬──────────────────────────────────────┐
│ id  │ password (HASHED)                    │
├─────┼──────────────────────────────────────┤
│ 1   │ $2a$12$R9h/cIPyNuWLlk/dHHrKMu...    │  ← HASHED
│ 2   │ $2a$12$slYQmyNdGzin7olVhS5Be...    │
└─────┴──────────────────────────────────────┘

Even if hacker gets database:
├─ Can't reverse hashes to get passwords
├─ Cannot login
└─ Safe! ✅
```

**How Hashing Works:**

```
Hash is ONE-WAY (like burning paper):

Plain password: "Admin@123"
      ↓
   HASH function (can't reverse)
      ↓
Hashed: "$2a$12$R9h/cIPyNuWLlk/dHHrKMu..."

You CAN'T go back:
"$2a$12$R9h/..." → ??? → "Admin@123" (IMPOSSIBLE!)

But you CAN verify:
User enters: "Admin@123"
Hash it: "$2a$12$R9h/..."
Compare with database: "$2a$12$R9h/..."
Match? YES → Password correct! ✅
```

---

### Q19: What is JWT and why use it?

**A:**

**JWT** = JSON Web Token

A JWT is a **secure token** that proves "This user is logged in" without storing sessions on server.

**Simple Analogy:**

```
Traditional (Session-Based):
├─ User logs in
├─ Server stores: "User Ali is logged in"
├─ User gets ticket: "Session ID: 12345"
├─ Every request: "Here's my session 12345"
├─ Server checks: "Is session 12345 valid?"

Modern (JWT Token-Based):
├─ User logs in
├─ Server generates: JWT token
├─ User gets token: "eyJhbGciOiJIUzI1..."
├─ Every request: "Here's my JWT"
├─ Server verifies: "Is JWT valid?" (no database lookup!)
```

**JWT Structure (3 parts):**

```
JWT: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcxMzc5MDAwMH0.xyz...
     └─ Part 1 ─┘ └─ Part 2 ─┘ └─ Part 3 ─┘

Part 1 (Header): {"alg": "HS256", "typ": "JWT"}
Part 2 (Payload): {"sub": "admin@hospital.com", "role": "ADMIN"}
Part 3 (Signature): Verify it's not tampered
```

**JWT in SmartApp:**

```
1. Login
User enters email + password
    ↓
Server verifies credentials
    ↓
Server generates JWT token
    ↓
Returns JWT to frontend

2. Store JWT
Frontend stores in localStorage
    ↓
localStorage.setItem('hq_token', jwt)

3. Use JWT
Frontend sends every request with JWT
    ↓
GET /api/doctor/queue
Authorization: Bearer eyJhbGciOi...
    ↓
Server verifies JWT signature
    ↓
If valid → Access granted
If invalid/expired → 401 Unauthorized

4. Logout
Frontend removes JWT from localStorage
    ↓
localStorage.removeItem('hq_token')
    ↓
No more JWT sent with requests
    ↓
User logged out
```

---

## 6. API CONCEPTS

### Q20: What's an API? What does REST mean?

**A:**

**API** = Application Programming Interface

An API lets two programs **talk to each other** and exchange data.

**Real-World Analogy:**

```
Restaurant API:
You (Customer) ─→ Waiter (API) ─→ Kitchen

1. You ask waiter: "Can I have biryani?"
2. Waiter goes to kitchen
3. Kitchen prepares biryani
4. Waiter brings biryani back

Same with SmartApp:
1. Frontend asks backend: "Get doctor queue?"
2. Backend queries database
3. Backend returns queue data
4. Frontend displays on screen
```

**What's REST?**

REST = **Representational State Transfer**

REST is a **standard way** to design APIs using simple HTTP requests.

**REST Principles:**

| Principle | Meaning |
|---|---|
| **GET** | Fetch/retrieve data |
| **POST** | Create new data |
| **PUT** | Update existing data |
| **DELETE** | Remove data |

**REST API Examples:**

```
GET /api/patients           → Get all patients
GET /api/patients/1         → Get patient #1
POST /api/patients          → Create new patient
PUT /api/patients/1         → Update patient #1
DELETE /api/patients/1      → Delete patient #1

GET /api/doctors/1/tokens   → Get doctor #1's tokens
POST /api/tokens            → Create new token
```

---

### Q21: What's HTTP Status Codes? Name common ones.

**A:**

**HTTP Status Code** = Response code from server about what happened to your request.

**Common Status Codes:**

```
2xx = Success ✅
├─ 200 OK - Request succeeded
├─ 201 Created - Resource created successfully
└─ 204 No Content - Success, no data returned

3xx = Redirect
├─ 301 Moved Permanently
└─ 307 Temporary Redirect

4xx = Client Error (Your fault) 
├─ 400 Bad Request - Malformed request
├─ 401 Unauthorized - Not logged in
├─ 403 Forbidden - Logged in but no permission
├─ 404 Not Found - Resource doesn't exist
└─ 409 Conflict - Duplicate/conflict

5xx = Server Error (Backend's fault)
├─ 500 Internal Server Error
├─ 502 Bad Gateway
└─ 503 Service Unavailable
```

**Examples from SmartApp:**

```
Request: POST /api/patient/register (valid data)
Response: 201 Created ✅
{ "patientId": 42, "name": "Ali" }

---

Request: GET /api/doctor/queue (not logged in)
Response: 401 Unauthorized ❌
{ "error": "Please login first" }

---

Request: POST /api/admin/users (logged in as patient)
Response: 403 Forbidden ❌
{ "error": "You don't have permission" }

---

Request: GET /api/patient/1 (patient 1 doesn't exist)
Response: 404 Not Found ❌
{ "error": "Patient not found" }
```

---

## 7. REAL-TIME FEATURES (SIMPLE)

### Q22: What's real-time? How does WebSocket work simply?

**A:**

**Real-Time** = Instant updates without manual refresh

**Traditional Way (Not Real-Time):**

```
Patient sees: "Position: 5"
...waiting...
Doctor processes patient
...waiting...
Patient manually clicks "Refresh"
...website reloads...
Patient sees: "Position: 4"
```

**Real-Time Way (WebSocket):**

```
Patient sees: "Position: 5"
...waiting...
Doctor processes patient
...instantly...
Patient sees: "Position: 4" (auto-updated!)

NO manual refresh needed! ✅
```

**How WebSocket Works Simply:**

```
Normal HTTP (one-way):
Frontend: "Get queue?"
Backend:  ← (responds once)
Frontend: "Get queue again?"
Backend:  ← (responds again)
(Must ask repeatedly = polling)

WebSocket (two-way connection):
Frontend ─── Connect ─── Backend
    ↑                       ↓
    └─── Open connection ──┘
    
Now backend can send updates:
Backend: "Queue updated: Position 4"
Frontend: ← (receives instantly!)

Backend: "Your turn!"
Frontend: ← (receives instantly!)
```

**SmartApp Example:**

```
Patient opens tracking page
    ↓
Frontend establishes WebSocket connection
    ↓
Frontend subscribes: "Tell me when my token status changes"
    ↓
Patient waits...
    ↓
Doctor marks patient IN_PROGRESS
    ↓
Backend publishes: "Token CARD-001 is now IN_PROGRESS"
    ↓
Patient's screen updates INSTANTLY!
    ↓
No refresh needed ✅
```

---

### Q23: What happens if WebSocket fails?

**A:**

**Fallback Mechanism** = Plan B if real-time fails

**Scenario: WebSocket connection breaks**

```
Patient is tracking queue
    ↓
Internet connection drops briefly
    ↓
WebSocket disconnects
    ↓
What happens?
```

**Solution: Fallback to Polling**

```
WebSocket fails
    ↓
Fallback to HTTP polling
    ↓
Every 15 seconds, frontend asks: "What's my position?"
    ├─ 1st check: Position 3
    ├─ 15 sec later: Position 3
    ├─ 30 sec later: Position 2 (updated!)
    └─ 45 sec later: Position 1
```

**Code Example (Simple):**

```typescript
// Try WebSocket first
try {
  this.websocketService.connect()
    .subscribe(update => {
      // Real-time update
      this.position = update.position;
    });
} catch (error) {
  // WebSocket failed, fallback to polling
  setInterval(() => {
    this.patientService.getPosition()
      .subscribe(data => {
        this.position = data.position;
      });
  }, 15000);  // Every 15 seconds
}
```

**Trade-off:**

```
WebSocket:
├─ Instant updates (milliseconds)
├─ Requires stable connection
└─ If works: Better experience ✅

Polling Fallback:
├─ Updates every 15 seconds (delay)
├─ Works on poor connections
└─ Backup plan when WebSocket fails ✅
```

---

## 8. VERSION CONTROL (GIT)

### Q24: What's Git? Why do we use it?

**A:**

**Git** is a **version control system** - it tracks changes to code over time.

**Without Git (Nightmare):**

```
code_v1.zip
code_v2.zip
code_v2_final.zip
code_v2_final_REAL.zip
code_v2_final_REAL_fixed.zip
code_v3_dont_delete.zip
code_v3_old.zip
code_v4_THIS_ONE.zip
```

Problem: Which is the latest? Who changed what? Total chaos!

**With Git (Organized):**

```
Git tracks every change:

Commit 1: "Added login functionality" (by Ali)
Commit 2: "Fixed queue ordering bug" (by Bob)
Commit 3: "Added real-time updates" (by Charlie)

Each commit has:
├─ What changed (diff)
├─ Who made change
├─ When it was made
├─ Why (commit message)
└─ Can revert if needed

Benefits:
✅ Never lose code
✅ See entire history
✅ Know who changed what
✅ Can undo changes
✅ Collaborate safely
```

---

### Q25: Explain basic Git commands.

**A:**

**Most Common Git Commands:**

```bash
# 1. Clone (Download project)
git clone https://github.com/user/SmartApp.git
# Downloads entire project to your computer

# 2. Status (Check what changed)
git status
# Shows: modified files, new files, untracked files

# 3. Add (Stage changes)
git add .  # Stage all changes
git add src/main/java/Patient.java  # Stage specific file
# Prepares changes to be committed

# 4. Commit (Save changes)
git commit -m "Fixed queue ordering bug"
# Saves changes with a message

# 5. Push (Upload to server)
git push
# Uploads your commits to GitHub

# 6. Pull (Download latest)
git pull
# Downloads latest changes from team

# 7. Log (View history)
git log
# Shows all commits with messages

# 8. Branch (Create separate version)
git branch feature/new-feature
git checkout feature/new-feature
# Work on new feature without affecting main code

# 9. Merge (Combine branches)
git merge feature/new-feature
# Combines your feature branch into main
```

**Simple Workflow:**

```
Day 1:
$ git pull              # Get latest code
$ git checkout -b new-feature  # Create new branch
$ vim PatientService.java      # Edit file
$ git add .             # Stage changes
$ git commit -m "Add patient validation"  # Save
$ git push              # Upload

Day 2 (After review):
$ git merge new-feature # Merge into main
$ git push              # Upload to main
```

---

## 9. TESTING BASICS

### Q26: What's testing? Why test code?

**A:**

**Testing** = Verifying that code works as expected

**Without Testing (Problems):**

```java
public int calculateAge(LocalDate birthDate) {
    return LocalDate.now().getYear() - birthDate.getYear();
}

// Looks correct, but has BUG:
Person born: 1990-12-25
Today: 2026-04-22
Expected age: 35
Actual: 36 (WRONG!) 🐛
```

**With Testing (Catches bugs):**

```java
@Test
public void testCalculateAge() {
    LocalDate birthDate = LocalDate.of(1990, 12, 25);
    int age = calculator.calculateAge(birthDate);
    
    assertEquals(35, age);  // Test fails!
    // You fix the bug before users see it ✅
}
```

**Why Test?**

```
✅ Catch bugs early
✅ Prevent regressions (new changes breaking old code)
✅ Document expected behavior
✅ Confidence when refactoring
✅ Code quality
```

---

### Q27: What are Unit Tests?

**A:**

**Unit Tests** = Test individual methods/functions in isolation

**Example: Testing QueueService**

```java
@Test
public void testGetQueuePosition() {
    // Arrange (Setup)
    Patient patient = new Patient(1, "Ali", "9876543210");
    Token token = new Token(patient, 3);  // Position 3
    
    // Act (Execute)
    int position = queueService.addTokenToQueue(token);
    
    // Assert (Verify)
    assertEquals(3, position);
}

@Test
public void testEmptyQueuePosition() {
    // Empty queue, first token should be position 1
    Token token = new Token(patient, 0);
    
    int position = queueService.addTokenToQueue(token);
    
    assertEquals(1, position);
}
```

**What Unit Tests Do:**

```
Input: token with 5 people ahead
    ↓
Method: addTokenToQueue()
    ↓
Expected Output: position = 6
    ↓
Actual Output: position = 6
    ↓
✅ Test PASSES
```

---

### Q28: What's the difference between Unit Test and Integration Test?

**A:**

| Unit Test | Integration Test |
|---|---|
| Tests 1 function alone | Tests multiple components together |
| Mock dependencies | Use real database |
| Fast (milliseconds) | Slower (seconds) |
| Catches logic errors | Catches integration issues |

**Example:**

**Unit Test (isolate PatientService):**

```java
@Mock
private PatientRepository patientRepository;

@InjectMocks
private PatientService patientService;

@Test
public void testRegisterPatient() {
    // Mock database
    Patient patient = new Patient("Ali", "9876543210");
    when(patientRepository.save(patient))
        .thenReturn(patient);
    
    // Test service in isolation
    Patient result = patientService.register(patient);
    
    assertEquals("Ali", result.getName());
}
```

**Integration Test (test PatientService + real database):**

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class PatientControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Test
    public void testRegisterPatientAPI() {
        // Use REAL database
        String request = "{ \"name\": \"Ali\", \"phone\": \"9876543210\" }";
        
        mockMvc.perform(
            post("/api/patient/register")
            .contentType("application/json")
            .content(request)
        )
        .andExpect(status().isOk());
        
        // Verify in real database
        Patient saved = patientRepository.findByName("Ali");
        assertNotNull(saved);
    }
}
```

---

## 10. PROBLEM-SOLVING (SIMPLE SCENARIOS)

### Q29: A patient reports they didn't receive their token number. What could be wrong?

**A:**

**Possible Causes (in order of likelihood):**

```
1. Page didn't load properly
   └─ "Check browser console for errors"
   └─ "Refresh page and try again"

2. Network error / Internet disconnected
   └─ "Check internet connection"
   └─ "Try again"

3. Backend server not running
   └─ "Check if server is working"
   └─ Command: curl http://localhost:8081/api/health

4. Invalid data entered
   └─ "Name or phone might be empty"
   └─ "Check if phone is correct format"

5. Database issue
   └─ "Database might be down"
   └─ "Check database connection"
```

**How to Debug (Steps):**

```
Step 1: Ask patient
├─ "Did you see any error message?"
├─ "What does it say?"
└─ "Can you take a screenshot?"

Step 2: Check browser console
├─ Open browser DevTools (F12)
├─ Go to Console tab
├─ Look for red error messages
└─ Screenshot and share

Step 3: Check backend logs
├─ SSH to server
├─ tail -f backend.log
├─ Submit form again
├─ Look for ERROR messages
└─ Identify the issue

Step 4: Test manually
├─ Try registering yourself
├─ See if it works
├─ If it works: user error
├─ If it doesn't: system issue
```

---

### Q30: A doctor says queue is not updating in real-time. How do you fix it?

**A:**

**Possible Causes:**

```
1. WebSocket not connected
   └─ Browser → DevTools → Network → look for "ws://" connection

2. Wrong token subscription
   └─ Check: Is doctor subscribing to correct topic?
   └─ Should be: /topic/queue/{doctorId}

3. Backend not broadcasting
   └─ Check backend logs for "publish" messages

4. Browser not detecting updates
   └─ Check: Is component subscribed?
   └─ Verify: Is ngOnInit() running?

5. Internet connection issue
   └─ Try refreshing page
   └─ Try different browser
```

**Quick Fixes (Try in Order):**

```
Fix 1: Refresh page
$ Hard refresh: Ctrl+Shift+R (clears cache)
$ Should reconnect WebSocket

Fix 2: Check server status
$ curl http://localhost:8081/actuator/health
Should return: { "status": "UP" }

Fix 3: Restart backend
$ Kill current process: Ctrl+C
$ Restart: java -jar backend.jar

Fix 4: Clear browser cache
$ Open DevTools → Application → Clear Storage
$ Refresh page

Fix 5: Check network tab
$ Open DevTools → Network
$ Filter by "ws"
$ Should see WebSocket connection
$ If red (failed): network issue
```

**Testing Manually:**

```
1. Login as doctor
2. Note your doctorId (check browser console)
3. Open DevTools → Console
4. Type: console.log("Doctor ID: " + doctorId)
5. Check if it logged correctly

6. Check WebSocket
7. Network tab → filter by "ws"
8. Should show "101 Switching Protocols" ✅
9. If error 404/500: WebSocket failed ❌
```

---

### Q31: Code won't compile. How to debug?

**A:**

**Common Compilation Errors:**

```
Error 1: "cannot find symbol"
├─ Cause: Typo in class/method name
├─ Solution: Check spelling
└─ Example: "patientService" vs "patientservice" (case sensitive!)

Error 2: "incompatible types"
├─ Cause: Wrong data type
├─ Solution: Check what type is expected
└─ Example: String age = 25; (should be: String age = "25";)

Error 3: "package does not exist"
├─ Cause: Missing import statement
├─ Solution: Add import at top of file
└─ Example: import com.hospital.queue.entity.Patient;

Error 4: "method signature does not match"
├─ Cause: Wrong method parameters
├─ Solution: Check parameter types
└─ Example: save(patient) vs save(patient, token)
```

**How to Fix (IDE helps):**

```
Use IntelliJ IDEA:

1. Look at error message line number
2. IDE shows red squiggly line
3. Hover over error
4. IDE suggests fix
5. Press Alt+Enter to apply quick fix

Example:
Compilation error: "cannot find symbol: method getTokens"
    ↓
Red line under: getTokens()
    ↓
Hover: "getTokens method not found in PatientService"
    ↓
IDE suggests: "Did you mean: getTokenCount()?"
    ↓
Press Alt+Enter → Fixed! ✅
```

**Manual Debug:**

```
1. Read error message carefully
2. Note: file, line number, class name
3. Open that file, go to that line
4. Check syntax/spelling
5. Verify imports are correct
6. Compile again

Example error:
cannot find symbol
  symbol:   method getPosition(java.lang.String)
  location: class com.hospital.queue.service.PatientService
PatientService.java:45:30

Fix:
$ Open PatientService.java
$ Go to line 45
$ Check method call getPosition()
$ Method might be: getQueuePosition() (different name!)
$ Change to correct name
$ Recompile ✅
```

---

### Q32: Application runs but shows error when you load a page. How to debug?

**A:**

**Error Types & Solutions:**

**Type 1: 404 Not Found (Page doesn't exist)**

```
Error: 404 Not Found

Causes:
├─ Wrong URL typed
├─ Component not registered in routing
├─ Backend endpoint doesn't exist

Fix:
1. Check URL: http://localhost:4200/admin
2. Check routes file: app-routing.module.ts
3. Verify path: { path: 'admin', ... }
4. Reload page
```

**Type 2: 401 Unauthorized (Not logged in)**

```
Error: 401 Unauthorized

Cause:
├─ JWT token missing/expired
├─ User not logged in

Fix:
1. Logout and login again
2. Check localStorage for token
   $ Open DevTools → Application → Storage → localhost
   $ Look for: hq_token
3. If missing: login again
4. Try page again
```

**Type 3: 403 Forbidden (Don't have permission)**

```
Error: 403 Forbidden

Cause:
├─ User role doesn't match required role
├─ Example: Patient trying to access /admin

Fix:
1. Check: Are you logged in as correct role?
2. If patient: Can't access admin pages
3. Login as admin and try again
4. OR navigate to correct role's page
```

**Type 4: 500 Internal Server Error (Backend crash)**

```
Error: 500 Internal Server Error

Cause:
├─ Backend code error
├─ Database connection failed
├─ Null pointer exception

Fix:
1. Check backend logs
2. SSH to server: ssh user@server
3. View logs: tail -f backend.log
4. Look for red ERROR messages
5. See error stacktrace
6. Fix code, recompile, redeploy
```

**Debug Steps (General):**

```
1. Check browser console (F12 → Console)
   ├─ Red errors?
   ├─ Stack trace?
   └─ Copy error message

2. Check network tab (F12 → Network)
   ├─ See the failed request?
   ├─ Status code: 400/401/403/404/500?
   ├─ Response body: Read error message
   └─ Note endpoint and parameters

3. Check backend logs
   ├─ SSH to server
   ├─ tail -f backend.log
   ├─ Reproduce error
   ├─ See error in logs
   └─ Fix accordingly

4. Use rubber duck debugging
   ├─ Explain code to someone
   ├─ Often you'll see the mistake yourself ✅
```

---

### Q33: You broke something in git. How to undo?

**A:**

**Scenario 1: Made changes but haven't committed yet**

```bash
# Accidentally edited Patient.java wrongly
$ git status
# Shows: modified: src/main/java/Patient.java

# Undo changes (revert to last commit)
$ git checkout -- src/main/java/Patient.java
# File is restored! ✅
```

**Scenario 2: Committed but want to undo commit**

```bash
# Accidentally committed wrong code
$ git log
# Shows: Commit "Added bug in queue logic"

# Option A: Undo last commit (keep changes)
$ git reset --soft HEAD~1
# Changes are unstaged (back to before commit)
# You can edit and commit correctly

# Option B: Undo last commit (discard changes)
$ git reset --hard HEAD~1
# Entire commit is deleted ⚠️
```

**Scenario 3: Pushed wrong code to GitHub**

```bash
# Pushed broken code and others already pulled
$ git revert HEAD
# Create NEW commit that undoes previous breaking commit
# Others should pull again
# Safe because doesn't lose history
```

**Important - Undo Different Ways:**

```
git checkout -- file    = Undo file (before commit)
git reset               = Undo changes (staging area)
git reset --hard HEAD~1 = DANGEROUS! Deletes entire commit
git revert              = SAFE! Creates new commit undoing it
```

**Best Practice:**

```
Before pushing to shared repo:
1. Check: Did I change wrong files?
   $ git diff
2. Check: Did I add confidential data?
   $ git show
3. Check: Did tests pass?
   $ npm test
4. Then push safely
   $ git push
```

---

### Q34: How do you report a bug to the team?

**A:**

**Good Bug Report Includes:**

```
1. TITLE (Clear, concise)
   ✅ "Admin cannot create department with duplicate name"
   ❌ "Department button broken"

2. DESCRIPTION (What's the problem?)
   ✅ "When I try to create department 'Cardiology' again, 
       system doesn't show error message, page hangs"
   ❌ "Something is wrong"

3. STEPS TO REPRODUCE (How to see the bug)
   ✅ Steps:
       1. Login as admin
       2. Go to Departments page
       3. Click "Add Department"
       4. Enter name: "Cardiology"
       5. Enter prefix: "CARD"
       6. Click Save
       7. Repeat steps 3-6 with same name
       8. Bug: No error shown, page hangs
   ❌ "It just happens sometimes"

4. EXPECTED vs ACTUAL
   ✅ Expected: "Error message: Department already exists"
       Actual: "Page hangs, no error message"
   ❌ "It doesn't work"

5. ENVIRONMENT
   ✅ Browser: Chrome 120
       OS: Windows 11
       Backend: Running locally
       Time reproduced: 2026-04-22 10:30 AM
   ❌ (Not mentioned)

6. SCREENSHOTS/ERROR LOGS
   ✅ (Screenshot showing the hang)
       (Console error message)
   ❌ (None)
```

**Example Bug Report:**

```
Title: Queue position doesn't update when doctor marks patient complete

Description:
Patient is tracking their queue position in real-time. 
When doctor marks them IN_PROGRESS, patient's screen 
should update to say "It's your turn!" but it doesn't.
Patient has to manually refresh to see update.

Steps to Reproduce:
1. Login as patient
2. Register and get token: CARD-20260422-001
3. Go to tracking page
4. See position: 5
5. Tell doctor to mark this token IN_PROGRESS
6. Wait for update on patient page
7. Page doesn't update (should update in real-time)
8. Refresh page manually
9. Now it shows: "It's your turn!"

Expected: Auto-update in real-time
Actual: Requires manual refresh

Environment:
- Browser: Chrome 120
- Backend version: v2.1.0
- Database: MySQL local
- Time: 2026-04-22 15:45

Screenshots: [attached]
Browser console errors: [none]
Backend logs: [attached tail output]
```

---

### Q35: What do you do when you don't understand the code?

**A:**

**When Stuck (Honest approach that gets respect):**

```
✅ DO:
1. Read code comments
   $ Understand intent

2. Read method/class names
   $ Sometimes names explain purpose

3. Google the pattern
   $ "Spring Data JPA @Lock example"

4. Read documentation
   $ Spring docs, Angular docs

5. Ask senior developer
   $ "I don't understand this line,
         can you explain how it works?"

6. Write tests
   $ Tests show how code is used

7. Debug step-by-step
   $ Set breakpoint
   $ See values change
   $ Understand flow

❌ DON'T:
1. Pretend you understand
2. Make random changes
3. Ignore the problem
4. Complain without trying
5. Copy-paste wrong code
```

**Example - Asking for Help (Good Way):**

```
BAD:
"I don't get this code"

GOOD:
"I'm reading QueueService.generateTokenNumber() method.
I understand it needs to generate unique token numbers,
and I see it uses pessimistic locking with @Lock annotation.
I don't understand WHY we need the lock here - wouldn't
a simple counter increment work? Can you explain the
concurrency issue being solved?"

Why this is good:
✅ Shows you've studied
✅ Shows specific question
✅ Shows effort to understand
✅ Makes it easy for senior to help
```

---

## COMMON INTERVIEW MISTAKES TO AVOID

### When Answering Questions:

✅ **DO:**
- Answer honestly ("I don't know")
- Explain in simple terms
- Ask clarifying questions
- Show you tried to understand
- Admit mistakes ("I was wrong about...")

❌ **DON'T:**
- Memorize and recite textbooks
- Pretend to know everything
- Interrupt interviewer
- Give overly long answers
- Complain about previous job/company

### Show Your Work:

```
Question: "How would you fix a bug in the queue?"

BAD:
"I would debug it"

GOOD:
"I would:
1. First reproduce the bug (understand it)
2. Check browser console for JS errors
3. Check backend logs for exceptions  
4. Look at the relevant code
5. Add console.log/print statements
6. Step through with debugger
7. Once I find root cause, fix it
8. Test locally
9. Write a test case
10. Get code review"

This shows:  
✅ Systematic approach
✅ Thought process
✅ Best practices
```

---

## SUMMARY FOR FRESHER CANDIDATES

**Key Concepts to Remember:**

1. **Frontend (Angular)** = What users see and interact with
2. **Backend (Spring Boot)** = Server that processes requests
3. **Database (MySQL)** = Permanent storage
4. **API** = Communication between frontend and backend
5. **Authentication** = Proving who you are
6. **Authorization** = What you're allowed to do
7. **Real-time (WebSocket)** = Instant updates
8. **Git** = Version control, save your code safely
9. **Testing** = Verify code works
10. **Debugging** = Find and fix problems

**For Interview Success:**

✅ Admit what you don't know  
✅ Show your learning process  
✅ Ask clarifying questions  
✅ Think out loud  
✅ Show enthusiasm  
✅ Ask about company culture  
✅ Prepare questions to ask them  

**Final Tip:**

> "Don't worry about knowing everything.
> Interviewers hire people who can LEARN quickly,
> not people who know everything.
> Show them you're curious, hardworking, and humble."

---

*Interview Questions & Answers for SmartApp - Fresher Level*  
*For: Junior/Entry-Level Developers (0-2 years)*  
*Prepared: 2026-04-22*
