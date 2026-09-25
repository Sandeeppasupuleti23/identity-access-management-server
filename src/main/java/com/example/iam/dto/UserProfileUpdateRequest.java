package com.example.iam.dto;

public record UserProfileUpdateRequest(
        String firstName,
        String lastName,
        String phoneNumber
) {}
