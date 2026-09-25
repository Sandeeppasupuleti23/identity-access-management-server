package com.example.iam.controller;

import com.example.iam.entity.UserEntity;
import com.example.iam.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserInfoController {

    private final UserService userService;

    @GetMapping("/userinfo")
    public Map<String, Object> userInfo(@AuthenticationPrincipal UserDetails userDetails) {
        UserEntity user = userService.findByUsername(userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", String.valueOf(user.getId()));
        claims.put("name", user.getFirstName() + " " + user.getLastName());
        claims.put("email", user.getEmail());
        claims.put("roles", user.getRoles().stream().map(role -> role.getName()).toList());
        return claims;
    }
}
