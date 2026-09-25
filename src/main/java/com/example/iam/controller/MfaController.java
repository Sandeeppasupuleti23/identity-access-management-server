package com.example.iam.controller;

import com.example.iam.entity.UserEntity;
import com.example.iam.service.MfaService;
import com.example.iam.service.OtpService;
import com.example.iam.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mfa")
@RequiredArgsConstructor
public class MfaController {

    private final MfaService mfaService;
    private final UserService userService;
    private final OtpService otpService;

    @PostMapping("/totp/setup")
    public ResponseEntity<?> setupTotp(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        String secret = mfaService.generateSecret();
        String uri = mfaService.createTotpUri(username, secret);
        UserEntity user = userService.findByUsername(username);
        user.setTotpSecret(secret);
        userService.updateProfile(username, user.getFirstName(), user.getLastName(), user.getPhoneNumber());
        return ResponseEntity.ok(Map.of(
                "message", "TOTP setup started",
                "secret", secret,
                "otpauth", uri
        ));
    }

    @PostMapping("/totp/verify")
    public ResponseEntity<?> verifyTotp(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, String> payload) {
        UserEntity user = userService.findByUsername(userDetails.getUsername());
        String code = payload.get("code");
        boolean verified = mfaService.verifyTotp(user, code);
        if (!verified) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid TOTP code"));
        }
        mfaService.enableTotp(user.getUsername(), user.getTotpSecret());
        return ResponseEntity.ok(Map.of("message", "TOTP enabled"));
    }

    @PostMapping("/totp/disable")
    public ResponseEntity<?> disableTotp(@AuthenticationPrincipal UserDetails userDetails) {
        mfaService.disableTotp(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "TOTP disabled"));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@AuthenticationPrincipal UserDetails userDetails) {
        UserEntity user = userService.findByUsername(userDetails.getUsername());
        String otp = otpService.generateOtp();
        System.out.println("Development OTP for " + user.getEmail() + ": " + otp);
        return ResponseEntity.ok(Map.of("message", "OTP sent", "otp", otp));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, String> payload) {
        boolean valid = otpService.verifyOtp(userDetails.getUsername(), "EMAIL_OTP", payload.get("code"));
        return valid ? ResponseEntity.ok(Map.of("message", "OTP verified"))
                : ResponseEntity.badRequest().body(Map.of("message", "OTP invalid"));
    }
}
