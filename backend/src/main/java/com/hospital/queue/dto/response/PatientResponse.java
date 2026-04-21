package com.hospital.queue.dto.response;

import com.hospital.queue.enums.RegistrationMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {

    private Long id;
    private String name;
    private String phone;
    private String email;
    private Integer age;
    private String gender;
    private RegistrationMode registeredBy;
    private LocalDateTime createdAt;
}
