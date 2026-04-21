package com.hospital.queue.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateDepartmentRequest {

    @NotBlank(message = "Department name is required")
    @Size(min = 2, max = 100)
    private String name;

    @Size(max = 255)
    private String description;
}
