package com.hospital.queue.entity;

import com.hospital.queue.enums.RegistrationMode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    private Integer age;

    @Column(length = 10)
    private String gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "registered_by", nullable = false, length = 20)
    @Builder.Default
    private RegistrationMode registeredBy = RegistrationMode.SELF;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
