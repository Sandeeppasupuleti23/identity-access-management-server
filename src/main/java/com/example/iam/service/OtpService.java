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

    private static final int MAX_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final MfaOtpRepository otpRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
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
@Transactional(noRollbackFor = IllegalArgumentException.class)
    public boolean verifyOtp(String username, String purpose, String code) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        MfaOtp otp = otpRepository
                .findTopByUserAndPurposeOrderByExpiresAtDesc(user, purpose)
                .orElseThrow(() -> new IllegalArgumentException("No OTP found"));

        if (otp.isUsed()) {
            throw new IllegalArgumentException("OTP already used");
        }

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP expired");
        }

        if (otp.getAttempts() >= MAX_ATTEMPTS) {
            otp.setUsed(true);
            otpRepository.save(otp);
            throw new IllegalArgumentException("Maximum OTP attempts exceeded");
        }

        if (code == null || !code.matches("\\d{6}") || !otp.getOtp().equals(code)) {
            int attempts = otp.getAttempts() + 1;
            otp.setAttempts(attempts);

            if (attempts >= MAX_ATTEMPTS) {
                otp.setUsed(true);
            }

            otpRepository.save(otp);
            throw new IllegalArgumentException(
                    attempts >= MAX_ATTEMPTS
                            ? "Maximum OTP attempts exceeded"
                            : "Invalid OTP"
            );
        }

        otp.setUsed(true);
        otpRepository.save(otp);
        return true;
    }
}