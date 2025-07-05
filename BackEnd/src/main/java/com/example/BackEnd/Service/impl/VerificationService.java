package com.example.BackEnd.Service.impl;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.example.BackEnd.Entity.EmailVerification;
import com.example.BackEnd.Repositories.mongodb.VerificationRepository;
import com.example.BackEnd.Service.IVerificationService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class VerificationService implements IVerificationService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final VerificationRepository verificationRepository;

    @Override
    public void sendOtpEmail(String to, String otp) throws MessagingException {
        throw new UnsupportedOperationException(
            "Use enqueueOtpEmail() instead of sendOtpEmail()"
        );
    }

    public void sendOtpEmailSync(String to, String otp) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
            message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
            "UTF-8"
        );

        Context ctx = new Context();
        ctx.setVariable("otp", otp);
        String html = templateEngine.process("email/otp-template.html", ctx);

        helper.setTo(to);
        helper.setSubject("Mã xác thực MedCare");
        helper.setText(html, true);

        mailSender.send(message);
    }

    @Override
    public EmailVerification createOne(String to, String otp) {
        EmailVerification ev = verificationRepository.findByEmail(otp);
        if (ev == null) {
            ev = new EmailVerification();
        }
        ev.setEmail(to);
        ev.setCode(otp);
        return verificationRepository.save(ev);
    }
}
