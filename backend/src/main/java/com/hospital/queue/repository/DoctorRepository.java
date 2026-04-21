package com.hospital.queue.repository;

import com.hospital.queue.entity.Department;
import com.hospital.queue.entity.Doctor;
import com.hospital.queue.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUser(User user);

    Optional<Doctor> findByUserId(Long userId);

    List<Doctor> findByDepartment(Department department);

    List<Doctor> findByDepartmentAndAvailableTrue(Department department);

    List<Doctor> findByAvailableTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Doctor d WHERE d.id = :id")
    Optional<Doctor> findByIdForUpdate(@Param("id") Long id);
}
