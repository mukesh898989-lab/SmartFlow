package com.hospital.queue.controller;

import com.hospital.queue.dto.request.GenerateTokenRequest;
import com.hospital.queue.dto.request.RegisterPatientRequest;
import com.hospital.queue.dto.response.DoctorResponse;
import com.hospital.queue.dto.response.PatientResponse;
import com.hospital.queue.dto.response.QueuePositionResponse;
import com.hospital.queue.dto.response.TokenResponse;
import com.hospital.queue.service.AdminService;
import com.hospital.queue.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final AdminService adminService;

    /**
     * POST /api/patient/register — Public: Patient self-registers.
     * No authentication required.
     */
    @PostMapping("/register")
    public ResponseEntity<PatientResponse> selfRegister(
            @Valid @RequestBody RegisterPatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(patientService.selfRegister(request));
    }

    /**
     * POST /api/patient/token — Public: Patient generates a token online.
     * Priority is always forced to NORMAL server-side.
     * No authentication required.
     */
    @PostMapping("/token")
    public ResponseEntity<TokenResponse> generateToken(
            @Valid @RequestBody GenerateTokenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(patientService.generateTokenOnline(request));
    }

    /**
     * GET /api/patient/token-status/{tokenNumber} — Public: Check token status.
     * No authentication required.
     */
    @GetMapping("/token-status/{tokenNumber}")
    public ResponseEntity<TokenResponse> getTokenStatus(@PathVariable String tokenNumber) {
        return ResponseEntity.ok(patientService.getTokenStatus(tokenNumber));
    }

    /**
     * GET /api/patient/queue-position/{tokenNumber} — Public: Check live queue position.
     * No authentication required.
     */
    @GetMapping("/queue-position/{tokenNumber}")
    public ResponseEntity<QueuePositionResponse> getQueuePosition(@PathVariable String tokenNumber) {
        return ResponseEntity.ok(patientService.getQueuePosition(tokenNumber));
    }
}

/**
 * Public controller for department and doctor listing.
 * Used by patients to select a doctor when generating a token online.
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
class PublicController {

    private final AdminService adminService;
    private final PatientService patientService;

    /** GET /api/public/departments — List all active departments. */
    @GetMapping("/departments")
    public ResponseEntity<List<?>> getDepartments() {
        return ResponseEntity.ok(adminService.getAllDepartments().stream()
                .filter(d -> d.getActive())
                .collect(Collectors.toList()));
    }

    /** GET /api/public/doctors — List all available doctors. */
    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorResponse>> getAvailableDoctors() {
        return ResponseEntity.ok(
                patientService.getAvailableDoctors().stream()
                        .map(adminService::toDoctorResponse)
                        .collect(Collectors.toList()));
    }

    /** GET /api/public/doctors/department/{departmentId} — Doctors by department. */
    @GetMapping("/doctors/department/{departmentId}")
    public ResponseEntity<List<DoctorResponse>> getDoctorsByDepartment(
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(
                patientService.getAvailableDoctorsByDepartment(departmentId).stream()
                        .map(adminService::toDoctorResponse)
                        .collect(Collectors.toList()));
    }
}
