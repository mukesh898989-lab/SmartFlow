package com.hospital.queue.entity;

import com.hospital.queue.enums.Priority;
import com.hospital.queue.enums.RegistrationMode;
import com.hospital.queue.enums.TokenStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_number", nullable = false, unique = true, length = 30)
    private String tokenNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    @Builder.Default
    private TokenStatus status = TokenStatus.WAITING;

    @Enumerated(EnumType.STRING)
    @Column(name = "generated_by", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    @Builder.Default
    private RegistrationMode generatedBy = RegistrationMode.SELF;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "skipped_at")
    private LocalDateTime skippedAt;

    @Column(name = "recalled")
    @Builder.Default
    private Boolean recalled = false;

    @Column(name = "recalled_at")
    private LocalDateTime recalledAt;
}
