package com.flowzapi.flowz_api_builder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.mail.password}")
    private String emailSender;
    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailSender);
        message.setTo(toEmail);
        message.setSubject("Welcome to Flowz-API! Verify your email");
        message.setText("Thank you for registering! Your verification code is: " + verificationCode +
                "\nThis code will expire in 2 minutes.");

        mailSender.send(message);
    }
}
