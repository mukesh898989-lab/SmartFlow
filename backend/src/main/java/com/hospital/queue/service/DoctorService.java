package com.hospital.queue.service;

import com.hospital.queue.dto.response.TokenResponse;
import com.hospital.queue.entity.Doctor;
import com.hospital.queue.entity.Token;
import com.hospital.queue.enums.TokenStatus;
import com.hospital.queue.exception.BusinessException;
import com.hospital.queue.exception.ResourceNotFoundException;
import com.hospital.queue.repository.DoctorRepository;
import com.hospital.queue.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final TokenRepository tokenRepository;
    private final QueueService queueService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Resolves the Doctor profile from the logged-in user's ID.
     */
    public Doctor getDoctorByUserId(Long userId) {
        return doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user id: " + userId));
    }

    /**
     * Returns the ordered WAITING queue for this doctor.
     */
    public List<TokenResponse> getMyQueue(Long userId) {
        Doctor doctor = getDoctorByUserId(userId);
        List<Token> queue = queueService.getOrderedWaitingQueue(doctor);

        // Prepend the IN_PROGRESS token if any, at position 0
        Optional<Token> inProgress = tokenRepository.findInProgressByDoctor(doctor);
        inProgress.ifPresent(t -> queue.add(0, t));

        return queueService.toTokenResponseList(queue);
    }

    /**
     * Calls the next patient.
     * - Marks current IN_PROGRESS token as COMPLETED.
     * - Takes the first WAITING token (priority-ordered) → marks IN_PROGRESS.
     * - Broadcasts update.
     */
    @Transactional
    public TokenResponse callNextPatient(Long userId) {
        Doctor doctor = getDoctorByUserId(userId);

        // Complete the current in-progress token if any
        tokenRepository.findInProgressByDoctor(doctor).ifPresent(current -> {
            current.setStatus(TokenStatus.COMPLETED);
            tokenRepository.save(current);
            broadcastTokenUpdate(current);
        });

        // Get next WAITING token in priority order
        List<Token> queue = queueService.getOrderedWaitingQueue(doctor);
        if (queue.isEmpty()) {
            throw new BusinessException("No patients waiting in the queue");
        }

        Token next = queue.get(0);
        next.setStatus(TokenStatus.IN_PROGRESS);
        Token saved = tokenRepository.save(next);

        broadcastQueueUpdate(doctor);
        broadcastTokenUpdate(saved);

        return queueService.toTokenResponse(saved);
    }

    /**
     * Doctor updates the status of a specific token.
     * Only WAITING → IN_PROGRESS → COMPLETED transitions allowed.
     * Doctor cannot set PRIORITY.
     */
    @Transactional
    public TokenResponse updateTokenStatus(Long tokenId, TokenStatus newStatus, Long userId) {
        Doctor doctor = getDoctorByUserId(userId);
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token", tokenId));

        // Ensure the token belongs to this doctor
        if (!token.getDoctor().getId().equals(doctor.getId())) {
            throw new BusinessException("This token does not belong to your queue");
        }

        // Validate transition
        validateStatusTransition(token.getStatus(), newStatus);

        // If marking IN_PROGRESS, ensure no other IN_PROGRESS token exists
        if (newStatus == TokenStatus.IN_PROGRESS) {
            tokenRepository.findInProgressByDoctor(doctor).ifPresent(existing -> {
                if (!existing.getId().equals(token.getId())) {
                    throw new BusinessException("Another patient is already in progress. Call next to complete first.");
                }
            });
        }

        token.setStatus(newStatus);
        Token saved = tokenRepository.save(token);

        broadcastQueueUpdate(doctor);
        broadcastTokenUpdate(saved);

        return queueService.toTokenResponse(saved);
    }

    private void validateStatusTransition(TokenStatus current, TokenStatus next) {
        boolean valid = switch (current) {
            case WAITING -> next == TokenStatus.IN_PROGRESS;
            case IN_PROGRESS -> next == TokenStatus.COMPLETED;
            case COMPLETED -> false;
        };
        if (!valid) {
            throw new BusinessException(
                    "Invalid status transition: " + current + " → " + next);
        }
    }

    private void broadcastQueueUpdate(Doctor doctor) {
        List<Token> queue = queueService.getOrderedWaitingQueue(doctor);
        messagingTemplate.convertAndSend(
                "/topic/queue/" + doctor.getId(),
                queueService.toTokenResponseList(queue));
    }

    private void broadcastTokenUpdate(Token token) {
        messagingTemplate.convertAndSend(
                "/topic/token/" + token.getTokenNumber(),
                queueService.toTokenResponse(token));
    }
}
