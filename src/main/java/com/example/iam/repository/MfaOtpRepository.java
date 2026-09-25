package com.example.iam.repository;

import com.example.iam.entity.MfaOtp;
import com.example.iam.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MfaOtpRepository extends JpaRepository<MfaOtp, Long> {
    Optional<MfaOtp> findTopByUserAndPurposeOrderByExpiresAtDesc(UserEntity user, String purpose);
}
