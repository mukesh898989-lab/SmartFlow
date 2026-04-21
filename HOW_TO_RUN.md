# How to Run — Smart Hospital Queue Management System

## Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8+
- Node.js 18+ and npm

---

## Step 1 — Start MySQL and create the database

```sql
-- In MySQL shell:
CREATE DATABASE hospital_queue;
```

Or the Spring Boot app auto-creates it via:
`createDatabaseIfNotExist=true` in `application.properties`.

Update credentials in:
`backend/src/main/resources/application.properties`

```properties
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

---

## Step 2 — Run the Backend

```bash
cd backend
mvn spring-boot:run
```

The server starts at: http://localhost:8081

**Default Admin Account (auto-created on first run):**
- Email: `admin@hospital.com`
- Password: `Admin@123`

---

## Step 3 — Run the Frontend

```bash
cd frontend
npm install
npm start
```

The app opens at: http://localhost:4200

---

## Typical Setup Workflow (After First Run)

1. Login as Admin → http://localhost:4200/auth/login
2. Create Departments (e.g., Cardiology, Orthopedics)
3. Create Doctors (creates User + Doctor profile automatically)
4. Create Receptionist user account

5. Login as Receptionist
6. Register patients → Generate tokens → Manage queue priority

7. Login as Doctor
8. View queue → Call next patient → Update status

9. Patient self-service → http://localhost:4200/patient
   - Register + Get Token online
   - Track token status in real time

---

## Port Summary

| Service   | URL                    |
|-----------|------------------------|
| Backend   | http://localhost:8081  |
| Frontend  | http://localhost:4200  |
| WebSocket | ws://localhost:8081/ws |
| MySQL     | localhost:3306         |

---

## API Quick Reference

| Role         | Base Path          |
|--------------|--------------------|
| Auth         | POST /api/auth/login |
| Admin        | /api/admin/**      |
| Receptionist | /api/receptionist/** |
| Doctor       | /api/doctor/**     |
| Patient      | /api/patient/**    |
| Public       | /api/public/**     |
