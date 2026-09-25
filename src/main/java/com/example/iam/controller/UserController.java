package com.example.iam.controller;

import com.example.iam.dto.UserProfileUpdateRequest;
import com.example.iam.entity.UserEntity;
import com.example.iam.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails userDetails) {
        UserEntity user = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "phoneNumber", user.getPhoneNumber(),
                "roles", user.getRoles().stream().map(role -> role.getName()).toList(),
                "mfaEnabled", user.isMfaEnabled()
        ));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                          @Valid @RequestBody UserProfileUpdateRequest request) {
        UserEntity updated = userService.updateProfile(userDetails.getUsername(), request.firstName(), request.lastName(), request.phoneNumber());
        return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully",
                "firstName", updated.getFirstName(),
                "lastName", updated.getLastName(),
                "phoneNumber", updated.getPhoneNumber()
        ));
    }
}
