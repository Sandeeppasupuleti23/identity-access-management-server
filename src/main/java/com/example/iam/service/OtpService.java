package com.example.iam.service;

import com.example.iam.entity.MfaOtp;
import com.example.iam.entity.UserEntity;
import com.example.iam.repository.MfaOtpRepository;
import com.example.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final UserRepository userRepository;
    private final MfaOtpRepository otpRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(1000000));
    }

    @Transactional
    public MfaOtp createOtp(UserEntity user, String purpose) {
        MfaOtp otp = new MfaOtp();
        otp.setUser(user);
        otp.setOtp(generateOtp());
        otp.setPurpose(purpose);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otp.setAttempts(0);
        otp.setUsed(false);
        return otpRepository.save(otp);
    }

    @Transactional
    public boolean verifyOtp(String username, String purpose, String code) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        MfaOtp otp = otpRepository.findTopByUserAndPurposeOrderByExpiresAtDesc(user, purpose)
                .orElseThrow(() -> new IllegalArgumentException("No OTP found"));

        if (otp.isUsed() || otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP expired or already used");
        }

        otp.setAttempts(otp.getAttempts() + 1);
        otpRepository.save(otp);

        if (!otp.getOtp().equals(code)) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        otp.setUsed(true);
        otpRepository.save(otp);
        return true;
    }
}
