package com.example.iam.service;

import com.example.iam.dto.LoginRequest;
import com.example.iam.entity.UserEntity;
import com.example.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final RateLimitService rateLimitService;

    public boolean authenticate(LoginRequest loginRequest, String ipAddress, String userAgent) {
        String key = "login:" + ipAddress + ":" + loginRequest.username();
        if (!rateLimitService.allow(key, 5, java.time.Duration.ofMinutes(15))) {
            auditService.record(loginRequest.username(), "LOGIN_FAILED", ipAddress, userAgent, false,
                    "Rate limit exceeded");
            throw new IllegalStateException("Too many login attempts. Please wait.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));
            UserEntity user = userRepository.findByUsername(loginRequest.username())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            auditService.record(user.getUsername(), "LOGIN_SUCCESS", ipAddress, userAgent, true,
                    "User authenticated successfully");
            return authentication.isAuthenticated();
        } catch (AuthenticationException ex) {
            auditService.record(loginRequest.username(), "LOGIN_FAILED", ipAddress, userAgent, false,
                    "Invalid username or password");
            throw ex;
        }
    }
}
