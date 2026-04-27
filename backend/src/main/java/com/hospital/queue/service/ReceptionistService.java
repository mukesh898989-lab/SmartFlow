package com.hospital.queue.service;

import com.hospital.queue.dto.request.GenerateTokenRequest;
import com.hospital.queue.dto.request.RegisterPatientRequest;
import com.hospital.queue.dto.request.UpdatePriorityRequest;
import com.hospital.queue.dto.response.PatientResponse;
import com.hospital.queue.dto.response.TokenResponse;
import com.hospital.queue.entity.Department;
import com.hospital.queue.entity.Doctor;
import com.hospital.queue.entity.Patient;
import com.hospital.queue.entity.Token;
import com.hospital.queue.enums.RegistrationMode;
import com.hospital.queue.enums.TokenStatus;
import com.hospital.queue.exception.BusinessException;
import com.hospital.queue.exception.ResourceNotFoundException;
import com.hospital.queue.repository.DoctorRepository;
import com.hospital.queue.repository.PatientRepository;
import com.hospital.queue.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReceptionistService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenRepository tokenRepository;
    private final QueueService queueService;
    private final SimpMessagingTemplate messagingTemplate;

    // ─── Patient Registration ─────────────────────────────────────────────────

    @Transactional
    public PatientResponse registerPatient(RegisterPatientRequest request) {
        if (patientRepository.existsByPhone(request.getPhone())) {
            // If patient already exists, return existing record
            Patient existing = patientRepository.findByPhone(request.getPhone())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with phone: " + request.getPhone()));
            return toPatientResponse(existing);
        }
        Patient patient = Patient.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .age(request.getAge())
                .gender(request.getGender())
                .registeredBy(RegistrationMode.RECEPTIONIST)
                .build();
        return toPatientResponse(patientRepository.save(patient));
    }

    public List<PatientResponse> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::toPatientResponse)
                .collect(Collectors.toList());
    }

    // ─── Token Generation ─────────────────────────────────────────────────────

    @Transactional
    public TokenResponse generateToken(GenerateTokenRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", request.getPatientId()));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", request.getDoctorId()));

        if (!doctor.getAvailable()) {
            throw new BusinessException("Doctor is not available");
        }

        Department department = doctor.getDepartment();
        String tokenNumber = queueService.generateTokenNumber(doctor);

        Token token = Token.builder()
                .tokenNumber(tokenNumber)
                .patient(patient)
                .doctor(doctor)
                .department(department)
                .priority(request.getPriority())
                .status(TokenStatus.WAITING)
                .generatedBy(RegistrationMode.RECEPTIONIST)
                .build();

        Token saved = tokenRepository.save(token);
        TokenResponse response = queueService.toTokenResponse(saved);

        // Broadcast updated queue to doctor's channel
        broadcastQueueUpdate(doctor);

        return response;
    }

    // ─── Priority Management (ONLY receptionist can do this) ──────────────────

    @Transactional
    public TokenResponse updatePriority(Long tokenId, UpdatePriorityRequest request) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token", tokenId));

        if (token.getStatus() == TokenStatus.COMPLETED) {
            throw new BusinessException("Cannot change priority of a completed token");
        }
        if (token.getStatus() == TokenStatus.IN_PROGRESS) {
            throw new BusinessException("Cannot change priority of a token that is in progress");
        }

        token.setPriority(request.getPriority());
        Token saved = tokenRepository.save(token);
        TokenResponse response = queueService.toTokenResponse(saved);

        // Queue order may change — broadcast update
        broadcastQueueUpdate(token.getDoctor());
        broadcastTokenUpdate(saved);

        return response;
    }

    // ─── Remove Token from Queue ──────────────────────────────────────────────

    @Transactional
    public void removeToken(Long tokenId) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token", tokenId));

        if (token.getStatus() == TokenStatus.IN_PROGRESS) {
            throw new BusinessException("Cannot remove a token that is currently in progress");
        }

        Doctor doctor = token.getDoctor();
        tokenRepository.delete(token);

        broadcastQueueUpdate(doctor);
    }

    // ─── Queue View ───────────────────────────────────────────────────────────

    public List<TokenResponse> getActiveQueue() {
        return queueService.toTokenResponseList(tokenRepository.findAllActive());
    }

    // ─── Skip / Recall ────────────────────────────────────────────────────────

    /**
     * Recalls a skipped patient back into the queue.
     * The recalled token re-enters as WAITING with the recalled flag set,
     * which causes QueueService to place it immediately after the current patient.
     */
    @Transactional
    public TokenResponse recallPatient(Long tokenId) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token", tokenId));

        if (token.getStatus() != TokenStatus.SKIPPED) {
            throw new BusinessException("Only SKIPPED tokens can be recalled");
        }

        token.setStatus(TokenStatus.WAITING);
        token.setRecalled(true);
        token.setRecalledAt(LocalDateTime.now());
        Token saved = tokenRepository.save(token);

        broadcastQueueUpdate(token.getDoctor());
        broadcastTokenUpdate(saved);

        return queueService.toTokenResponse(saved);
    }

    /**
     * Returns all currently skipped patients across all doctors.
     */
    public List<TokenResponse> getSkippedQueue() {
        return queueService.toTokenResponseList(tokenRepository.findAllSkipped());
    }

    // ─── WebSocket Broadcast ──────────────────────────────────────────────────

    private void broadcastQueueUpdate(Doctor doctor) {
        List<Token> queue = queueService.getOrderedWaitingQueue(doctor);
        List<TokenResponse> queueResponse = queueService.toTokenResponseList(queue);
        messagingTemplate.convertAndSend("/topic/queue/" + doctor.getId(), queueResponse);
    }

    private void broadcastTokenUpdate(Token token) {
        TokenResponse response = queueService.toTokenResponse(token);
        messagingTemplate.convertAndSend("/topic/token/" + token.getTokenNumber(), response);
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────

    private PatientResponse toPatientResponse(Patient patient) {
        return PatientResponse.builder()
                .id(patient.getId())
                .name(patient.getName())
                .phone(patient.getPhone())
                .email(patient.getEmail())
                .age(patient.getAge())
                .gender(patient.getGender())
                .registeredBy(patient.getRegisteredBy())
                .createdAt(patient.getCreatedAt())
                .build();
    }
}
