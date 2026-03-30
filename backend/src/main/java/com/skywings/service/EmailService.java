package com.skywings.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            Context context = new Context();
            context.setVariable("otp", otp);
            context.setVariable("validMinutes", 5);
            String htmlBody = templateEngine.process("otp-email", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject("SkyWings Airways - Payment Verification Code");
            helper.setText(htmlBody, true);
            helper.setFrom("noreply@skywings.com");
            mailSender.send(message);
            log.info("OTP email sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    public void sendBookingConfirmation(String toEmail, String subject, String htmlBody, byte[] pdfAttachment) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.setFrom("noreply@skywings.com");

            if (pdfAttachment != null) {
                helper.addAttachment("SkyWings-ETicket.pdf",
                    new org.springframework.core.io.ByteArrayResource(pdfAttachment),
                    "application/pdf");
            }

            mailSender.send(message);
            log.info("Booking confirmation email sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send booking confirmation to {}: {}", toEmail, e.getMessage());
        }
    }

    public void sendCancellationNotice(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.setFrom("noreply@skywings.com");
            mailSender.send(message);
            log.info("Cancellation email sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send cancellation email to {}: {}", toEmail, e.getMessage());
        }
    }
}
