package com.project2.service;

import com.project2.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${admin.email}")
    private String adminEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(User user) {
        String subject = "Registration Successful - Welcome to Talentrox!";
        String content = "<html>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; background-color: #f9f9f9;'>" +
                "<h2 style='color: #6C63FF; text-align: center;'>Welcome to Talentrox!</h2>" +
                "<p>Hi <strong>" + user.getFullName() + "</strong>,</p>" +
                "<p>Your registration was successful! We're thrilled to have you join our community at Talentrox.</p>" +
                "<div style='background-color: #fff; padding: 15px; border-radius: 5px; border-left: 5px solid #6C63FF; margin: 20px 0;'>" +
                "<p style='margin: 0;'><strong>Username:</strong> " + user.getUsername() + "</p>" +
                "<p style='margin: 0;'><strong>Role:</strong> " + user.getRole() + "</p>" +
                "</div>" +
                "<p>Start exploring opportunities and connecting with top talent today.</p>" +
                "<div style='text-align: center; margin-top: 30px;'>" +
                "<a href='http://localhost:8080/login' style='background-color: #6C63FF; color: #fff; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;'>Login to Your Account</a>" +
                "</div>" +
                "<p style='margin-top: 30px; font-size: 0.9em; color: #777;'>Best regards,<br>The Talentrox Team</p>" +
                "</div>" +
                "</body>" +
                "</html>";

        sendHtmlEmail(user.getEmail(), subject, content);
    }

    public void sendAdminRegistrationNotification(User newUser) {
        String subject = "New User Registration Alert - Talentrox";
        String content = "<html>" +
                "<body>" +
                "<h3>A new user has registered on Talentrox</h3>" +
                "<p><strong>Name:</strong> " + newUser.getFullName() + "</p>" +
                "<p><strong>Username:</strong> " + newUser.getUsername() + "</p>" +
                "<p><strong>Email:</strong> " + newUser.getEmail() + "</p>" +
                "<p><strong>Role:</strong> " + newUser.getRole() + "</p>" +
                "<p><strong>Joined:</strong> " + newUser.getCreatedAt() + "</p>" +
                "</body>" +
                "</html>";

        sendHtmlEmail(adminEmail, subject, content);
    }

    public void sendOtpEmail(String email, String otp) {
        String subject = "Verification Code - Talentrox";
        String content = "<html>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; background-color: #f9f9f9;'>" +
                "<h2 style='color: #6C63FF; text-align: center;'>Verify Your Account</h2>" +
                "<p>Hi there,</p>" +
                "<p>Thank you for registering with Talentrox! Please use the following 6-digit verification code to complete your registration:</p>" +
                "<div style='background-color: #fff; padding: 20px; text-align: center; border-radius: 5px; border: 1px dashed #6C63FF; margin: 20px 0;'>" +
                "<h1 style='color: #6C63FF; letter-spacing: 5px; margin: 0;'>" + otp + "</h1>" +
                "</div>" +
                "<p>This code will expire in 5 minutes. If you didn't request this, please ignore this email.</p>" +
                "<p style='margin-top: 30px; font-size: 0.9em; color: #777;'>Best regards,<br>The Talentrox Team</p>" +
                "</div>" +
                "</body>" +
                "</html>";

        sendHtmlEmail(email, subject, content);
    }

    public void sendPasswordResetEmail(User user, String token) {
        String resetUrl = "http://localhost:8080/reset-password?token=" + token;
        String subject = "Password Reset Request - TalentRock";
        String content = "<html>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; background-color: #fff;'>" +
                "<h2 style='color: #dc3545; text-align: center;'>Reset Your Password</h2>" +
                "<p>Hi <strong>" + user.getFullName() + "</strong>,</p>" +
                "<p>We received a request to reset the password for your TalentRock account. Click the button below to set a new password:</p>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='" + resetUrl + "' style='background-color: #dc3545; color: #fff; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;'>Reset Password</a>" +
                "</div>" +
                "<p>If you didn't request a password reset, you can safely ignore this email. This link will expire in 1 hour.</p>" +
                "<p style='margin-top: 30px; font-size: 0.9em; color: #777;'>Best regards,<br>The TalentRock Team</p>" +
                "</div>" +
                "</body>" +
                "</html>";

        sendHtmlEmail(user.getEmail(), subject, content);
    }

    private void sendHtmlEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}
