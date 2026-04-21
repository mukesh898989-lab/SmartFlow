package com.hospital.queue.dto.request;

import com.hospital.queue.enums.TokenStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private TokenStatus status;
}
