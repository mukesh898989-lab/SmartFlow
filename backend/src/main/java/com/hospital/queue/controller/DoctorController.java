package com.hospital.queue.controller;

import com.hospital.queue.dto.request.UpdateStatusRequest;
import com.hospital.queue.dto.response.TokenResponse;
import com.hospital.queue.service.DoctorService;
import com.hospital.queue.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
@PreAuthorize("hasRole('DOCTOR')")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final SecurityUtils securityUtils;

    /**
     * GET /api/doctor/queue
     * Returns the ordered queue for the currently authenticated doctor.
     * IN_PROGRESS token appears first (position 0), followed by WAITING tokens.
     */
    @GetMapping("/queue")
    public ResponseEntity<List<TokenResponse>> getMyQueue() {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(doctorService.getMyQueue(userId));
    }

    /**
     * POST /api/doctor/queue/next
     * Calls the next patient:
     * - Completes the current IN_PROGRESS token (if any).
     * - Sets the first WAITING token to IN_PROGRESS.
     */
    @PostMapping("/queue/next")
    public ResponseEntity<TokenResponse> callNextPatient() {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(doctorService.callNextPatient(userId));
    }

    /**
     * PUT /api/doctor/tokens/{id}/status
     * Updates the consultation status of a specific token.
     * Doctor can only set: WAITING → IN_PROGRESS → COMPLETED.
     * Doctor CANNOT set priority.
     */
    @PutMapping("/tokens/{id}/status")
    public ResponseEntity<TokenResponse> updateTokenStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(
                doctorService.updateTokenStatus(id, request.getStatus(), userId));
    }

    /**
     * POST /api/doctor/tokens/{id}/skip
     * Skips an absent patient: WAITING → SKIPPED.
     * The next waiting patient automatically becomes the current patient.
     */
    @PostMapping("/tokens/{id}/skip")
    public ResponseEntity<TokenResponse> skipToken(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(doctorService.skipPatient(id, userId));
    }
}
