package com.example.iam;

import com.example.iam.util.PasswordValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordValidatorTest {

    @Test
    void strongPasswordIsAccepted() {
        assertTrue(PasswordValidator.isStrongPassword("Password@123"));
    }

    @Test
    void weakPasswordIsRejected() {
        assertFalse(PasswordValidator.isStrongPassword("password"));
    }
}
