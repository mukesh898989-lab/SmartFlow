package com.hospital.queue.dto.request;

import com.hospital.queue.enums.Priority;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePriorityRequest {

    @NotNull(message = "Priority is required")
    private Priority priority;
}
