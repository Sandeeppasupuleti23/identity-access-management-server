package com.example.iam.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    public void sendOtp(String recipient, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipient);
        message.setSubject("IAM Server - Email Verification Code");
        message.setText(
                "Your IAM Server verification code is: " + otp
                + "\n\nThis code expires in 5 minutes."
                + "\nIf you did not request this code, you can ignore this email."
        );

        mailSender.send(message);
    }
}
