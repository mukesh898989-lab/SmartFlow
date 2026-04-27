package com.hospital.queue.service;

import com.hospital.queue.dto.response.TokenResponse;
import com.hospital.queue.entity.Doctor;
import com.hospital.queue.entity.Token;
import com.hospital.queue.enums.TokenStatus;
import com.hospital.queue.exception.ResourceNotFoundException;
import com.hospital.queue.repository.DoctorRepository;
import com.hospital.queue.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core queue ordering and token mapping logic.
 *
 * Queue ordering rule:
 *   Primary   → priority.order ASC  (EMERGENCY=1, URGENT=2, NORMAL=3)
 *   Secondary → createdAt ASC       (earlier arrival first within same priority)
 */
@Service
@RequiredArgsConstructor
public class QueueService {

    private final TokenRepository tokenRepository;
    private final DoctorRepository doctorRepository;

    /**
     * Returns the WAITING queue for a doctor in correct priority order.
     * Recalled tokens (patients who returned after being skipped) are placed first,
     * so they become the next patient after whoever is currently IN_PROGRESS.
     */
    public List<Token> getOrderedWaitingQueue(Doctor doctor) {
        List<Token> waiting = tokenRepository.findByDoctorAndStatus(doctor, TokenStatus.WAITING);
        waiting.sort((a, b) -> {
            boolean aRecalled = Boolean.TRUE.equals(a.getRecalled());
            boolean bRecalled = Boolean.TRUE.equals(b.getRecalled());

            // Recalled tokens always jump to the front of the waiting queue
            if (aRecalled != bRecalled) return aRecalled ? -1 : 1;

            // Multiple recalled tokens: earliest recall time first
            if (aRecalled) return a.getRecalledAt().compareTo(b.getRecalledAt());

            // Normal WAITING tokens: priority first, then arrival time
            int priCmp = Integer.compare(a.getPriority().getOrder(), b.getPriority().getOrder());
            return priCmp != 0 ? priCmp : a.getCreatedAt().compareTo(b.getCreatedAt());
        });
        return waiting;
    }

    /**
     * Calculates the 1-based queue position for a WAITING token.
     * Returns 0 if IN_PROGRESS, -1 if COMPLETED or SKIPPED.
     */
    public int getQueuePosition(Token token) {
        if (token.getStatus() == TokenStatus.IN_PROGRESS) return 0;
        if (token.getStatus() == TokenStatus.COMPLETED) return -1;
        if (token.getStatus() == TokenStatus.SKIPPED) return -1;

        List<Token> queue = getOrderedWaitingQueue(token.getDoctor());
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getId().equals(token.getId())) {
                return i + 1;
            }
        }
        return -1;
    }

    /**
     * Generates a unique token number for a doctor.
     * Format: {DEPT_PREFIX}-{YYYYMMDD}-{3-digit sequence}
     * Example: CARD-20260419-001
     */
    @Transactional
    public String generateTokenNumber(Doctor doctor) {
        // Pessimistic write lock on the Doctor row serializes concurrent token generation
        Doctor lockedDoctor = doctorRepository.findByIdForUpdate(doctor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctor.getId()));

        String deptName = lockedDoctor.getDepartment().getName();
        String prefix = deptName.replaceAll("[^A-Za-z]", "")
                .toUpperCase()
                .substring(0, Math.min(4, deptName.replaceAll("[^A-Za-z]", "").length()));

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        long todayCount = tokenRepository.countByDoctorAndCreatedAtBetween(lockedDoctor, startOfDay, endOfDay);
        String sequence = String.format("%03d", todayCount + 1);
        String dateStr = today.toString().replace("-", "");

        return prefix + "-" + dateStr + "-" + sequence;
    }

    /**
     * Maps a Token entity to a TokenResponse DTO, including queue position.
     */
    public TokenResponse toTokenResponse(Token token) {
        return TokenResponse.builder()
                .id(token.getId())
                .tokenNumber(token.getTokenNumber())
                .patientId(token.getPatient().getId())
                .patientName(token.getPatient().getName())
                .patientPhone(token.getPatient().getPhone())
                .doctorId(token.getDoctor().getId())
                .doctorName(token.getDoctor().getUser().getName())
                .specialization(token.getDoctor().getSpecialization())
                .departmentId(token.getDepartment().getId())
                .departmentName(token.getDepartment().getName())
                .priority(token.getPriority())
                .status(token.getStatus())
                .generatedBy(token.getGeneratedBy())
                .createdAt(token.getCreatedAt())
                .updatedAt(token.getUpdatedAt())
                .skippedAt(token.getSkippedAt())
                .recalled(token.getRecalled())
                .recalledAt(token.getRecalledAt())
                .queuePosition(getQueuePosition(token))
                .build();
    }

    /**
     * Maps a list of tokens to DTOs, including correct queue positions.
     */
    public List<TokenResponse> toTokenResponseList(List<Token> tokens) {
        return tokens.stream()
                .map(this::toTokenResponse)
                .collect(Collectors.toList());
    }
}
