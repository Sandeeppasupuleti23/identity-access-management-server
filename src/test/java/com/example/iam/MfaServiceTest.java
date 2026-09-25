package com.example.iam;

import com.example.iam.entity.UserEntity;
import com.example.iam.repository.UserRepository;
import com.example.iam.service.MfaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MfaServiceTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void generateSecretAndTotpUriShouldWork() {
        MfaService mfaService = new MfaService(userRepository);

        String secret = mfaService.generateSecret();
        assertNotNull(secret);
        assertFalse(secret.isBlank());

        String uri = mfaService.createTotpUri("john", secret);
        assertTrue(uri.startsWith("otpauth://totp/"));
    }

    @Test
    void verifyTotpShouldMatchGeneratedCode() {
        MfaService mfaService = new MfaService(userRepository);
        UserEntity user = new UserEntity();
        String secret = mfaService.generateSecret();
        user.setTotpSecret(secret);

        String code = mfaService.generateCode(secret);
        assertTrue(mfaService.verifyTotp(user, code));
    }
}
