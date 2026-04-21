package com.hospital.queue.service;

import com.hospital.queue.dto.request.CreateDepartmentRequest;
import com.hospital.queue.dto.request.CreateDoctorRequest;
import com.hospital.queue.dto.request.CreateUserRequest;
import com.hospital.queue.dto.response.DepartmentResponse;
import com.hospital.queue.dto.response.DoctorResponse;
import com.hospital.queue.dto.response.TokenResponse;
import com.hospital.queue.dto.response.UserResponse;
import com.hospital.queue.entity.Department;
import com.hospital.queue.entity.Doctor;
import com.hospital.queue.entity.User;
import com.hospital.queue.enums.Role;
import com.hospital.queue.exception.BusinessException;
import com.hospital.queue.exception.ResourceNotFoundException;
import com.hospital.queue.repository.DepartmentRepository;
import com.hospital.queue.repository.DoctorRepository;
import com.hospital.queue.repository.TokenRepository;
import com.hospital.queue.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final QueueService queueService;

    // ─── Users ────────────────────────────────────────────────────────────────

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already in use: " + request.getEmail());
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .active(true)
                .build();
        return toUserResponse(userRepository.save(user));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUserStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setActive(active);
        return toUserResponse(userRepository.save(user));
    }

    // ─── Departments ──────────────────────────────────────────────────────────

    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new BusinessException("Department already exists: " + request.getName());
        }
        Department dept = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .build();
        return toDepartmentResponse(departmentRepository.save(dept));
    }

    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::toDepartmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, CreateDepartmentRequest request) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        dept.setName(request.getName());
        dept.setDescription(request.getDescription());
        return toDepartmentResponse(departmentRepository.save(dept));
    }

    // ─── Doctors ──────────────────────────────────────────────────────────────

    /**
     * Creating a doctor: creates a DOCTOR-role User + links it to a Doctor profile.
     */
    @Transactional
    public DoctorResponse createDoctor(CreateDoctorRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already in use: " + request.getEmail());
        }
        Department dept = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", request.getDepartmentId()));

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.DOCTOR)
                .active(true)
                .build();
        user = userRepository.save(user);

        Doctor doctor = Doctor.builder()
                .user(user)
                .department(dept)
                .specialization(request.getSpecialization())
                .available(true)
                .build();
        return toDoctorResponse(doctorRepository.save(doctor));
    }

    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::toDoctorResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DoctorResponse updateDoctor(Long id, CreateDoctorRequest request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
        Department dept = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", request.getDepartmentId()));

        doctor.setDepartment(dept);
        doctor.setSpecialization(request.getSpecialization());

        // Update user name
        User user = doctor.getUser();
        user.setName(request.getName());
        userRepository.save(user);

        return toDoctorResponse(doctorRepository.save(doctor));
    }

    // ─── Queue Overview ───────────────────────────────────────────────────────

    public List<TokenResponse> getAllActiveTokens() {
        return queueService.toTokenResponseList(tokenRepository.findAllActive());
    }

    // ─── Mappers ──────────────────────────────────────────────────────────────

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public DepartmentResponse toDepartmentResponse(Department dept) {
        return DepartmentResponse.builder()
                .id(dept.getId())
                .name(dept.getName())
                .description(dept.getDescription())
                .active(dept.getActive())
                .build();
    }

    public DoctorResponse toDoctorResponse(Doctor doctor) {
        return DoctorResponse.builder()
                .id(doctor.getId())
                .userId(doctor.getUser().getId())
                .name(doctor.getUser().getName())
                .email(doctor.getUser().getEmail())
                .specialization(doctor.getSpecialization())
                .available(doctor.getAvailable())
                .department(toDepartmentResponse(doctor.getDepartment()))
                .build();
    }
}
