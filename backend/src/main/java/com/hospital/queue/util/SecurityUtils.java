package com.hospital.queue.util;

import com.hospital.queue.entity.User;
import com.hospital.queue.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    /**
     * Returns the authenticated user's database ID.
     * The Spring Security principal holds the email (username).
     */
    public Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
    }
}
