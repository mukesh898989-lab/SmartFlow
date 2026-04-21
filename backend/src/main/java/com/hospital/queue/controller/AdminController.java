package com.hospital.queue.controller;

import com.hospital.queue.dto.request.CreateDepartmentRequest;
import com.hospital.queue.dto.request.CreateDoctorRequest;
import com.hospital.queue.dto.request.CreateUserRequest;
import com.hospital.queue.dto.response.DepartmentResponse;
import com.hospital.queue.dto.response.DoctorResponse;
import com.hospital.queue.dto.response.TokenResponse;
import com.hospital.queue.dto.response.UserResponse;
import com.hospital.queue.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ─── User Management ──────────────────────────────────────────────────────

    /** POST /api/admin/users — Create a new staff user (Admin/Receptionist). */
    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(request));
    }

    /** GET /api/admin/users — List all staff users. */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /** PUT /api/admin/users/{id}/activate — Activate a user. */
    @PutMapping("/users/{id}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, true));
    }

    /** PUT /api/admin/users/{id}/deactivate — Deactivate a user. */
    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, false));
    }

    // ─── Department Management ────────────────────────────────────────────────

    /** POST /api/admin/departments — Create a new department. */
    @PostMapping("/departments")
    public ResponseEntity<DepartmentResponse> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createDepartment(request));
    }

    /** GET /api/admin/departments — List all departments. */
    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments() {
        return ResponseEntity.ok(adminService.getAllDepartments());
    }

    /** PUT /api/admin/departments/{id} — Update a department. */
    @PutMapping("/departments/{id}")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.ok(adminService.updateDepartment(id, request));
    }

    // ─── Doctor Management ────────────────────────────────────────────────────

    /** POST /api/admin/doctors — Create a new doctor (creates User + Doctor profile). */
    @PostMapping("/doctors")
    public ResponseEntity<DoctorResponse> createDoctor(@Valid @RequestBody CreateDoctorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createDoctor(request));
    }

    /** GET /api/admin/doctors — List all doctors. */
    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorResponse>> getAllDoctors() {
        return ResponseEntity.ok(adminService.getAllDoctors());
    }

    /** PUT /api/admin/doctors/{id} — Update doctor details. */
    @PutMapping("/doctors/{id}")
    public ResponseEntity<DoctorResponse> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody CreateDoctorRequest request) {
        return ResponseEntity.ok(adminService.updateDoctor(id, request));
    }

    // ─── Queue Overview ───────────────────────────────────────────────────────

    /** GET /api/admin/queue — View all active tokens across all doctors. */
    @GetMapping("/queue")
    public ResponseEntity<List<TokenResponse>> getAllActiveTokens() {
        return ResponseEntity.ok(adminService.getAllActiveTokens());
    }
}
