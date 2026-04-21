package com.hospital.queue.dto.request;

import com.hospital.queue.enums.Priority;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateTokenRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    /**
     * Priority is optional — defaults to NORMAL.
     * Receptionist can set priority at token generation time.
     * Online (patient) tokens always default to NORMAL (enforced in service).
     */
    private Priority priority = Priority.NORMAL;
}
