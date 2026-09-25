package com.example.iam.controller;

import com.example.iam.dto.LoginRequest;
import com.example.iam.dto.PasswordResetRequest;
import com.example.iam.dto.RegisterRequest;
import com.example.iam.dto.ResetPasswordRequest;
import com.example.iam.entity.UserEntity;
import com.example.iam.service.AuthenticationService;
import com.example.iam.service.PasswordResetService;
import com.example.iam.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        UserEntity saved = userService.registerUser(
                request.username(),
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName(),
                request.phoneNumber());

        return ResponseEntity.ok(Map.of(
                "message", "User registered successfully",
                "username", saved.getUsername(),
                "email", saved.getEmail(),
                "roles", saved.getRoles().stream().map(role -> role.getName()).toList()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        String ipAddress = httpServletRequest.getRemoteAddr();
        String userAgent = httpServletRequest.getHeader("User-Agent");
        authenticationService.authenticate(request, ipAddress, userAgent);
        return ResponseEntity.ok(Map.of("message", "Login successful"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        String token = passwordResetService.requestPasswordReset(request.email());
        return ResponseEntity.ok(Map.of("message", "Password reset token generated", "token", token));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.password());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}
