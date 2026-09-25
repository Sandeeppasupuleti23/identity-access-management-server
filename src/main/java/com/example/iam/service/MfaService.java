package com.example.iam.service;

import com.example.iam.entity.UserEntity;
import com.example.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MfaService {

    private static final int TIME_STEP_SECONDS = 30;
    private static final int CODE_LENGTH = 6;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateSecret() {
        byte[] bytes = new byte[20];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String createTotpUri(String username, String secret) {
        return "otpauth://totp/IAM%20Server:" + username + "?secret=" + secret + "&issuer=IAM%20Server";
    }

    public boolean verifyTotp(UserEntity user, String code) {
        if (user.getTotpSecret() == null || user.getTotpSecret().isBlank()) {
            return false;
        }
        String expected = generateCode(user.getTotpSecret());
        return expected.equals(code);
    }

    public void enableTotp(String username, String secret) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setTotpSecret(secret);
        user.setMfaEnabled(true);
        userRepository.save(user);
    }

    public void disableTotp(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setTotpSecret(null);
        user.setMfaEnabled(false);
        userRepository.save(user);
    }

    public String generateCode(String secret) {
        long counter = System.currentTimeMillis() / 1000L / TIME_STEP_SECONDS;
        String normalized = secret.replace('-', '+').replace('_', '/');
        while (normalized.length() % 4 != 0) {
            normalized += '=';
        }
        byte[] key = Base64.getDecoder().decode(normalized);
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA1");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(counter).array());
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            int otp = binary % (int) Math.pow(10, CODE_LENGTH);
            return String.format(Locale.US, "%0" + CODE_LENGTH + "d", otp);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Unable to generate TOTP", e);
        }
    }
}
