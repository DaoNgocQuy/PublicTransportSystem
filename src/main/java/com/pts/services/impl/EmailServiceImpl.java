package com.pts.services.impl;

import com.pts.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    
    private final TemplateEngine templateEngine;
    
    // Inject qua constructor thay vì field
    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        System.out.println("EmailServiceImpl constructor called with mailSender: " + 
                           (mailSender != null ? "not null" : "null"));
    }
    
    private String frontendUrl = "http://localhost:3000"; // Cấu hình URL của frontend

    @Override
    public boolean sendResetPasswordEmail(String email, String token, String fullName) {
        try {
            
            // Tạo context cho template
            Context context = new Context();
            context.setVariable("fullName", fullName);
            context.setVariable("token", token);
            
            // Process template
            String htmlContent = templateEngine.process("reset-password-email", context);
            
            // Gửi email
            return sendEmail(email, "Đặt lại mật khẩu của bạn", htmlContent);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}