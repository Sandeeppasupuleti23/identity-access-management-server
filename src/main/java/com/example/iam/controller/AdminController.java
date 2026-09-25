package com.example.iam.controller;

import com.example.iam.entity.AuditLog;
import com.example.iam.entity.UserEntity;
import com.example.iam.repository.AuditLogRepository;
import com.example.iam.service.TokenService;
import com.example.iam.service.UserService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AuditLogRepository auditLogRepository;
    private final TokenService tokenService;

    @GetMapping("/users")
    public ResponseEntity<?> listUsers() {
        return ResponseEntity.ok(userService.getAllUsers().stream().map(user -> Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "enabled", user.isEnabled(),
                "roles", user.getRoles().stream().map(role -> role.getName()).toList()
        )).toList());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        UserEntity user = userService.getUserById(id);
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "roles", user.getRoles().stream().map(role -> role.getName()).toList(),
                "enabled", user.isEnabled()
        ));
    }

    @PutMapping("/users/{id}/roles")
    public ResponseEntity<?> updateRoles(@PathVariable Long id, @RequestBody Map<String, Set<String>> body) {
        Set<String> roles = body.getOrDefault("roles", new HashSet<>());
        UserEntity updated = userService.updateUserRoles(id, roles);
        return ResponseEntity.ok(Map.of("message", "Roles updated", "roles", updated.getRoles().stream().map(role -> role.getName()).toList()));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        UserEntity updated = userService.updateUserStatus(id, body.getOrDefault("enabled", true));
        return ResponseEntity.ok(Map.of("message", "Status updated", "enabled", updated.isEnabled()));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<?> auditLogs(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        Page<AuditLog> auditLogs = auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        return ResponseEntity.ok(auditLogs.map(log -> Map.of(
                "id", log.getId(),
                "username", log.getUsername(),
                "eventType", log.getEventType(),
                "success", log.isSuccess(),
                "details", log.getDetails(),
                "createdAt", log.getCreatedAt()
        )));
    }

    @PostMapping("/tokens/revoke")
    public ResponseEntity<?> revokeTokens(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        tokenService.revokeUser(username);
        return ResponseEntity.ok(Map.of("message", "Tokens revoked for user", "username", username));
    }
}
