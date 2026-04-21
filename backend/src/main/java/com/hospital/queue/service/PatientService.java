package com.hospital.queue.service;

import com.hospital.queue.dto.request.GenerateTokenRequest;
import com.hospital.queue.dto.request.RegisterPatientRequest;
import com.hospital.queue.dto.response.PatientResponse;
import com.hospital.queue.dto.response.QueuePositionResponse;
import com.hospital.queue.dto.response.TokenResponse;
import com.hospital.queue.entity.Department;
import com.hospital.queue.entity.Doctor;
import com.hospital.queue.entity.Patient;
import com.hospital.queue.entity.Token;
import com.hospital.queue.enums.Priority;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenRepository tokenRepository;
    private final QueueService queueService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Patient self-registers. Phone number is the unique identifier.
     * If phone already exists, returns the existing patient.
     */
    @Transactional
    public PatientResponse selfRegister(RegisterPatientRequest request) {
        if (patientRepository.existsByPhone(request.getPhone())) {
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
                .registeredBy(RegistrationMode.SELF)
                .build();
        return toPatientResponse(patientRepository.save(patient));
    }

    /**
     * Patient generates a token online.
     * Priority is ALWAYS forced to NORMAL for online tokens.
     */
    @Transactional
    public TokenResponse generateTokenOnline(GenerateTokenRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", request.getPatientId()));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", request.getDoctorId()));

        if (!doctor.getAvailable()) {
            throw new BusinessException("Doctor is not currently available");
        }

        Department department = doctor.getDepartment();
        String tokenNumber = queueService.generateTokenNumber(doctor);

        Token token = Token.builder()
                .tokenNumber(tokenNumber)
                .patient(patient)
                .doctor(doctor)
                .department(department)
                .priority(Priority.NORMAL)   // Online tokens always NORMAL
                .status(TokenStatus.WAITING)
                .generatedBy(RegistrationMode.SELF)
                .build();

        Token saved = tokenRepository.save(token);

        // Notify doctor's queue subscribers
        List<Token> queue = queueService.getOrderedWaitingQueue(doctor);
        messagingTemplate.convertAndSend(
                "/topic/queue/" + doctor.getId(),
                queueService.toTokenResponseList(queue));

        return queueService.toTokenResponse(saved);
    }

    /**
     * Public status check — no auth required.
     */
    public TokenResponse getTokenStatus(String tokenNumber) {
        Token token = tokenRepository.findByTokenNumber(tokenNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found: " + tokenNumber));
        return queueService.toTokenResponse(token);
    }

    /**
     * Public queue position check — no auth required.
     */
    public QueuePositionResponse getQueuePosition(String tokenNumber) {
        Token token = tokenRepository.findByTokenNumber(tokenNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found: " + tokenNumber));

        int position = queueService.getQueuePosition(token);
        int totalWaiting = queueService.getOrderedWaitingQueue(token.getDoctor()).size();

        return QueuePositionResponse.builder()
                .tokenNumber(tokenNumber)
                .status(token.getStatus())
                .position(position)
                .totalWaiting(totalWaiting)
                .doctorName(token.getDoctor().getUser().getName())
                .departmentName(token.getDepartment().getName())
                .build();
    }

    /**
     * Returns all available doctors (for department/doctor selection during token generation).
     */
    public List<Doctor> getAvailableDoctors() {
        return doctorRepository.findByAvailableTrue();
    }

    public List<Doctor> getAvailableDoctorsByDepartment(Long departmentId) {
        return doctorRepository.findAll().stream()
                .filter(d -> d.getDepartment().getId().equals(departmentId) && d.getAvailable())
                .collect(Collectors.toList());
    }

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
