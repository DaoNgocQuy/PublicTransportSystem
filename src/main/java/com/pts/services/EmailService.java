package com.pts.services;

public interface EmailService {
    boolean sendResetPasswordEmail(String email, String token, String fullName);
    boolean sendEmail(String to, String subject, String content);
}