# Smart Hospital Queue Management System - API Documentation

## Overview

This document provides comprehensive API documentation for the Smart Hospital Queue Management System. The system provides RESTful APIs for managing hospital queues with role-based access control and real-time WebSocket updates.

**Base URL:** `http://localhost:8081/api`  
**Authentication:** JWT Bearer Token  
**Content-Type:** `application/json`

---

## Authentication

### POST /api/auth/login
Authenticate staff users (Admin, Receptionist, Doctor) and receive JWT token.

**Request Body:**
```json
{
  "email": "admin@hospital.com",
  "password": "Admin@123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userId": 1,
  "name": "System Admin",
  "email": "admin@hospital.com",
  "role": "ADMIN"
}
```

**Status Codes:** 200 OK, 401 Unauthorized

---

## Admin Endpoints
*All endpoints require ADMIN role*

### User Management

#### POST /api/admin/users
Create a new staff user (Admin/Receptionist).

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@hospital.com",
  "password": "password123",
  "role": "RECEPTIONIST"
}
```

**Response:** UserResponse object  
**Status Codes:** 201 Created, 400 Bad Request, 403 Forbidden

#### GET /api/admin/users
List all staff users.

**Response:** Array of UserResponse objects  
**Status Codes:** 200 OK

#### PUT /api/admin/users/{id}/activate
Activate a user account.

**Response:** UserResponse object  
**Status Codes:** 200 OK, 404 Not Found

#### PUT /api/admin/users/{id}/deactivate
Deactivate a user account.

**Response:** UserResponse object  
**Status Codes:** 200 OK, 404 Not Found

### Department Management

#### POST /api/admin/departments
Create a new department.

**Request Body:**
```json
{
  "name": "Cardiology",
  "description": "Heart and cardiovascular care"
}
```

**Response:** DepartmentResponse object  
**Status Codes:** 201 Created, 400 Bad Request

#### GET /api/admin/departments
List all departments.

**Response:** Array of DepartmentResponse objects  
**Status Codes:** 200 OK

#### PUT /api/admin/departments/{id}
Update department details.

**Request Body:**
```json
{
  "name": "Updated Cardiology",
  "description": "Updated description"
}
```

**Response:** DepartmentResponse object  
**Status Codes:** 200 OK, 404 Not Found

### Doctor Management

#### POST /api/admin/doctors
Create a new doctor (creates User + Doctor profile).

**Request Body:**
```json
{
  "name": "Dr. Smith",
  "email": "smith@hospital.com",
  "password": "password123",
  "departmentId": 1,
  "specialization": "Cardiologist"
}
```

**Response:** DoctorResponse object  
**Status Codes:** 201 Created, 400 Bad Request

#### GET /api/admin/doctors
List all doctors.

**Response:** Array of DoctorResponse objects  
**Status Codes:** 200 OK

#### PUT /api/admin/doctors/{id}
Update doctor details.

**Request Body:** Same as create doctor  
**Response:** DoctorResponse object  
**Status Codes:** 200 OK, 404 Not Found

### Queue Overview

#### GET /api/admin/queue
View all active tokens across all doctors.

**Response:** Array of TokenResponse objects  
**Status Codes:** 200 OK

---

## Receptionist Endpoints
*All endpoints require RECEPTIONIST role*

### Patient Management

#### POST /api/receptionist/patients
Register a patient offline.

**Request Body:**
```json
{
  "name": "John Doe",
  "phone": "1234567890",
  "email": "john@example.com",
  "age": 30,
  "gender": "MALE"
}
```

**Response:** PatientResponse object  
**Status Codes:** 201 Created, 400 Bad Request

#### GET /api/receptionist/patients
List all registered patients.

**Response:** Array of PatientResponse objects  
**Status Codes:** 200 OK

### Token Management

#### POST /api/receptionist/tokens
Generate a token for an offline patient.

**Request Body:**
```json
{
  "patientId": 1,
  "doctorId": 2,
  "priority": "NORMAL"
}
```

**Response:** TokenResponse object  
**Status Codes:** 201 Created, 400 Bad Request

#### PUT /api/receptionist/tokens/{id}/priority
Update token priority.

**Request Body:**
```json
{
  "priority": "URGENT"
}
```

**Response:** TokenResponse object  
**Status Codes:** 200 OK, 404 Not Found

#### DELETE /api/receptionist/tokens/{id}
Remove a patient from the queue.

**Status Codes:** 204 No Content, 404 Not Found

### Queue View

#### GET /api/receptionist/queue
View all active tokens.

**Response:** Array of TokenResponse objects  
**Status Codes:** 200 OK

---

## Doctor Endpoints
*All endpoints require DOCTOR role*

### Queue Management

#### GET /api/doctor/queue
Get the ordered queue for the authenticated doctor.

**Response:** Array of TokenResponse objects (ordered by priority and status)  
**Status Codes:** 200 OK

#### POST /api/doctor/queue/next
Call the next patient in queue.

**Response:** TokenResponse object (the token set to IN_PROGRESS)  
**Status Codes:** 200 OK, 404 Not Found

#### PUT /api/doctor/tokens/{id}/status
Update consultation status of a token.

**Request Body:**
```json
{
  "status": "COMPLETED"
}
```

**Response:** TokenResponse object  
**Status Codes:** 200 OK, 400 Bad Request, 404 Not Found

---

## Patient Endpoints
*Public endpoints - no authentication required*

### Self-Service

#### POST /api/patient/register
Patient self-registration.

**Request Body:**
```json
{
  "name": "Jane Doe",
  "phone": "0987654321",
  "email": "jane@example.com",
  "age": 25,
  "gender": "FEMALE"
}
```

**Response:** PatientResponse object  
**Status Codes:** 201 Created, 400 Bad Request

#### POST /api/patient/token
Generate a token online (priority automatically set to NORMAL).

**Request Body:**
```json
{
  "patientId": 1,
  "doctorId": 2
}
```

**Response:** TokenResponse object  
**Status Codes:** 201 Created, 400 Bad Request

#### GET /api/patient/token-status/{tokenNumber}
Check token status.

**Path Parameters:**
- `tokenNumber`: The token number (e.g., "DOC001-001")

**Response:** TokenResponse object  
**Status Codes:** 200 OK, 404 Not Found

#### GET /api/patient/queue-position/{tokenNumber}
Check live queue position.

**Path Parameters:**
- `tokenNumber`: The token number

**Response:**
```json
{
  "tokenNumber": "DOC001-001",
  "status": "WAITING",
  "position": 3,
  "totalWaiting": 5,
  "doctorName": "Dr. Smith",
  "departmentName": "Cardiology"
}
```

**Status Codes:** 200 OK, 404 Not Found

---

## Public Endpoints
*No authentication required*

### Department & Doctor Information

#### GET /api/public/departments
List all active departments.

**Response:** Array of DepartmentResponse objects  
**Status Codes:** 200 OK

#### GET /api/public/doctors
List all available doctors.

**Response:** Array of DoctorResponse objects  
**Status Codes:** 200 OK

#### GET /api/public/doctors/department/{departmentId}
List available doctors by department.

**Path Parameters:**
- `departmentId`: Department ID

**Response:** Array of DoctorResponse objects  
**Status Codes:** 200 OK

---

## Data Models

### UserResponse
```json
{
  "id": 1,
  "name": "System Admin",
  "email": "admin@hospital.com",
  "role": "ADMIN",
  "active": true,
  "createdAt": "2024-01-01T10:00:00"
}
```

### DepartmentResponse
```json
{
  "id": 1,
  "name": "Cardiology",
  "description": "Heart and cardiovascular care",
  "active": true
}
```

### DoctorResponse
```json
{
  "id": 1,
  "userId": 2,
  "name": "Dr. Smith",
  "email": "smith@hospital.com",
  "specialization": "Cardiologist",
  "available": true,
  "department": {
    "id": 1,
    "name": "Cardiology",
    "description": "Heart and cardiovascular care",
    "active": true
  }
}
```

### PatientResponse
```json
{
  "id": 1,
  "name": "John Doe",
  "phone": "1234567890",
  "email": "john@example.com",
  "age": 30,
  "gender": "MALE",
  "registeredBy": "RECEPTIONIST",
  "createdAt": "2024-01-01T10:00:00"
}
```

### TokenResponse
```json
{
  "id": 1,
  "tokenNumber": "DOC001-001",
  "patientId": 1,
  "patientName": "John Doe",
  "patientPhone": "1234567890",
  "doctorId": 2,
  "doctorName": "Dr. Smith",
  "specialization": "Cardiologist",
  "departmentId": 1,
  "departmentName": "Cardiology",
  "priority": "NORMAL",
  "status": "WAITING",
  "generatedBy": "RECEPTIONIST",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00",
  "queuePosition": 1
}
```

---

## Enumerations

### Role
- `ADMIN`
- `RECEPTIONIST`
- `DOCTOR`

### Priority
- `EMERGENCY` (highest priority)
- `URGENT`
- `NORMAL` (lowest priority)

### TokenStatus
- `WAITING`
- `IN_PROGRESS`
- `COMPLETED`

### RegistrationMode
- `SELF`
- `RECEPTIONIST`

### Gender
- `MALE`
- `FEMALE`
- `OTHER`

---

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/auth/login"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid credentials",
  "path": "/api/auth/login"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied",
  "path": "/api/admin/users"
}
```

### 404 Not Found
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found",
  "path": "/api/admin/users/999"
}
```

---

## WebSocket Endpoints

### STOMP Broker
**URL:** `ws://localhost:8081/ws`

### Topics

#### /topic/queue/{doctorId}
Subscribe to real-time queue updates for a specific doctor.

**Example:** `/topic/queue/1`

#### /topic/token/{tokenNumber}
Subscribe to real-time status updates for a specific token.

**Example:** `/topic/token/DOC001-001`

### Message Format
```json
{
  "id": 1,
  "tokenNumber": "DOC001-001",
  "status": "IN_PROGRESS",
  "updatedAt": "2024-01-01T10:00:00"
}
```

---

## Security

### JWT Authentication
- Include JWT token in Authorization header: `Bearer {token}`
- Token expires after 24 hours
- Stateless authentication

### CORS Configuration
- Allowed origins: `http://localhost:4200`
- Allowed methods: GET, POST, PUT, DELETE, OPTIONS
- Allowed headers: All
- Credentials: Allowed

### Role-Based Access Control
- **ADMIN**: Full system access
- **RECEPTIONIST**: Patient registration and queue management
- **DOCTOR**: Queue viewing and status updates
- **Public**: Patient self-service and information viewing

---

## Rate Limiting
- No explicit rate limiting implemented
- Consider implementing for production use

## Versioning
- Current API version: v1
- No versioning strategy implemented yet

---

*Document generated on: April 22, 2026*  
*API Version: 1.0.0*  
*System: Smart Hospital Queue Management System*