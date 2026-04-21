package com.hospital.queue.controller;

import com.hospital.queue.dto.request.GenerateTokenRequest;
import com.hospital.queue.dto.request.RegisterPatientRequest;
import com.hospital.queue.dto.request.UpdatePriorityRequest;
import com.hospital.queue.dto.response.PatientResponse;
import com.hospital.queue.dto.response.TokenResponse;
import com.hospital.queue.service.ReceptionistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/receptionist")
@PreAuthorize("hasRole('RECEPTIONIST')")
@RequiredArgsConstructor
public class ReceptionistController {

    private final ReceptionistService receptionistService;

    // ─── Patient Management ───────────────────────────────────────────────────

    /** POST /api/receptionist/patients — Register a patient offline. */
    @PostMapping("/patients")
    public ResponseEntity<PatientResponse> registerPatient(
            @Valid @RequestBody RegisterPatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(receptionistService.registerPatient(request));
    }

    /** GET /api/receptionist/patients — List all registered patients. */
    @GetMapping("/patients")
    public ResponseEntity<List<PatientResponse>> getAllPatients() {
        return ResponseEntity.ok(receptionistService.getAllPatients());
    }

    // ─── Token Management ─────────────────────────────────────────────────────

    /**
     * POST /api/receptionist/tokens — Generate a token for an offline patient.
     * Receptionist can set priority at this point.
     */
    @PostMapping("/tokens")
    public ResponseEntity<TokenResponse> generateToken(
            @Valid @RequestBody GenerateTokenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(receptionistService.generateToken(request));
    }

    /**
     * PUT /api/receptionist/tokens/{id}/priority — Update token priority.
     * Only RECEPTIONIST can change priority.
     */
    @PutMapping("/tokens/{id}/priority")
    public ResponseEntity<TokenResponse> updatePriority(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePriorityRequest request) {
        return ResponseEntity.ok(receptionistService.updatePriority(id, request));
    }

    /**
     * DELETE /api/receptionist/tokens/{id} — Remove a patient from the queue.
     */
    @DeleteMapping("/tokens/{id}")
    public ResponseEntity<Void> removeToken(@PathVariable Long id) {
        receptionistService.removeToken(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Queue View ───────────────────────────────────────────────────────────

    /** GET /api/receptionist/queue — View all active tokens. */
    @GetMapping("/queue")
    public ResponseEntity<List<TokenResponse>> getActiveQueue() {
        return ResponseEntity.ok(receptionistService.getActiveQueue());
    }
}
