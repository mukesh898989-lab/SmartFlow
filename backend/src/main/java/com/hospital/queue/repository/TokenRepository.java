package com.hospital.queue.repository;

import com.hospital.queue.entity.Doctor;
import com.hospital.queue.entity.Patient;
import com.hospital.queue.entity.Token;
import com.hospital.queue.enums.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByTokenNumber(String tokenNumber);

    List<Token> findByDoctorAndStatus(Doctor doctor, TokenStatus status);

    List<Token> findByPatient(Patient patient);

    /**
     * Find the first (only one expected) IN_PROGRESS token for a doctor.
     */
    @Query("SELECT t FROM Token t WHERE t.doctor = :doctor AND t.status = 'IN_PROGRESS'")
    Optional<Token> findInProgressByDoctor(@Param("doctor") Doctor doctor);

    /**
     * Count tokens for a doctor created on a specific date (for token number generation).
     */
    @Query("SELECT COUNT(t) FROM Token t WHERE t.doctor = :doctor AND t.createdAt >= :start AND t.createdAt < :end")
    long countByDoctorAndCreatedAtBetween(@Param("doctor") Doctor doctor,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    /**
     * All tokens for admin overview, ordered by creation time desc.
     */
    List<Token> findAllByOrderByCreatedAtDesc();

    /**
     * All active (WAITING + IN_PROGRESS) tokens across all doctors.
     */
    @Query("SELECT t FROM Token t WHERE t.status IN ('WAITING', 'IN_PROGRESS') ORDER BY t.createdAt ASC")
    List<Token> findAllActive();
}
