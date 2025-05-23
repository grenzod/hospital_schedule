package com.example.BackEnd.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OtpMail {
    private final String to;
    private final String otp;
}