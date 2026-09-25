package com.example.iam;

import com.example.iam.entity.Role;
import com.example.iam.entity.UserEntity;
import com.example.iam.repository.AuthorityRepository;
import com.example.iam.repository.RoleRepository;
import com.example.iam.repository.UserRepository;
import com.example.iam.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuthorityRepository authorityRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUserShouldHashPasswordAndAssignDefaultRole() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(new Role("ROLE_USER", "Standard user")));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService = new UserService(userRepository, roleRepository, authorityRepository, new BCryptPasswordEncoder());
        UserEntity saved = userService.registerUser("john", "john@example.com", "Password@123", "John", "Doe", "+123456789");

        assertEquals("john", saved.getUsername());
        assertNotEquals("Password@123", saved.getPassword());
        assertTrue(saved.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_USER")));
    }
}
