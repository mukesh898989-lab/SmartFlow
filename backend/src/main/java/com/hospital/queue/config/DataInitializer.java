package com.hospital.queue.config;

import com.hospital.queue.entity.User;
import com.hospital.queue.enums.Role;
import com.hospital.queue.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByRole(Role.ADMIN).isEmpty()) {
            User admin = User.builder()
                    .name("System Admin")
                    .email("admin@hospital.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
            log.info("Default admin created: admin@hospital.com / Admin@123");
        }
    }
}
