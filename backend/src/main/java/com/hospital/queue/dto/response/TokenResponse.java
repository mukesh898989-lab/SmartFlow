package com.hospital.queue.dto.response;

import com.hospital.queue.enums.Priority;
import com.hospital.queue.enums.RegistrationMode;
import com.hospital.queue.enums.TokenStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    private Long id;
    private String tokenNumber;

    // Patient info (flattened for convenience)
    private Long patientId;
    private String patientName;
    private String patientPhone;

    // Doctor info (flattened)
    private Long doctorId;
    private String doctorName;
    private String specialization;

    // Department info
    private Long departmentId;
    private String departmentName;

    private Priority priority;
    private TokenStatus status;
    private RegistrationMode generatedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Queue position (1-based). Populated on demand by QueueService.
     * -1 if not in WAITING state.
     */
    private Integer queuePosition;
}
