package com.hospital.queue.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterPatientRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone must be 10–15 digits")
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    @Min(value = 0, message = "Age must be positive")
    @Max(value = 150, message = "Age is not valid")
    private Integer age;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER")
    private String gender;
}
