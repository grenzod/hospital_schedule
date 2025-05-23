package com.example.BackEnd.Service;

import com.example.BackEnd.Entity.EmailVerification;

import jakarta.mail.MessagingException;

public interface IVerificationService {
    void sendOtpEmail(String to, String otp) throws MessagingException;
    EmailVerification createOne(String to, String otp);
}
