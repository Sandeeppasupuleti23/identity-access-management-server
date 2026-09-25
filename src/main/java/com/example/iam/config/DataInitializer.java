package com.example.iam.config;

import com.example.iam.entity.Authority;
import com.example.iam.entity.Role;
import com.example.iam.entity.UserEntity;
import com.example.iam.repository.AuthorityRepository;
import com.example.iam.repository.RoleRepository;
import com.example.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final AuthorityRepository authorityRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        Authority profileRead = saveAuthority("PROFILE_READ", "Read profile");
        Authority profileWrite = saveAuthority("PROFILE_WRITE", "Update profile");
        Authority userRead = saveAuthority("USER_READ", "Read users");
        Authority userWrite = saveAuthority("USER_WRITE", "Write users");
        Authority userDelete = saveAuthority("USER_DELETE", "Delete users");
        Authority adminRead = saveAuthority("ADMIN_READ", "Read admin data");
        Authority adminWrite = saveAuthority("ADMIN_WRITE", "Write admin data");
        Authority tokenRevoke = saveAuthority("TOKEN_REVOKE", "Revoke tokens");
        Authority auditRead = saveAuthority("AUDIT_READ", "Read audit logs");

        Role userRole = saveRole("ROLE_USER", "Standard user");
        userRole.setAuthorities(Set.of(profileRead, profileWrite));
        roleRepository.save(userRole);

        Role managerRole = saveRole("ROLE_MANAGER", "Manager access");
        managerRole.setAuthorities(Set.of(profileRead, profileWrite, userRead, userWrite));
        roleRepository.save(managerRole);

        Role adminRole = saveRole("ROLE_ADMIN", "Admin access");
        adminRole.setAuthorities(Set.of(userRead, userWrite, userDelete, adminRead, adminWrite, tokenRevoke, auditRead));
        roleRepository.save(adminRole);

        Role serviceRole = saveRole("ROLE_SERVICE", "Service account");
        serviceRole.setAuthorities(Set.of(userRead));
        roleRepository.save(serviceRole);

        if (!userRepository.existsByUsername("admin")) {
            UserEntity admin = new UserEntity();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("Admin@12345"));
            admin.setFirstName("System");
            admin.setLastName("Admin");
            admin.setEnabled(true);
            admin.setRoles(Set.of(adminRole));
            userRepository.save(admin);
        }

        if (!userRepository.existsByUsername("user")) {
            UserEntity user = new UserEntity();
            user.setUsername("user");
            user.setEmail("user@example.com");
            user.setPassword(passwordEncoder.encode("User@12345"));
            user.setFirstName("Sample");
            user.setLastName("User");
            user.setEnabled(true);
            user.setRoles(Set.of(userRole));
            userRepository.save(user);
        }
    }

    private Authority saveAuthority(String name, String description) {
        return authorityRepository.findByName(name)
                .orElseGet(() -> authorityRepository.save(new Authority(name, description)));
    }

    private Role saveRole(String name, String description) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new Role(name, description)));
    }
}
