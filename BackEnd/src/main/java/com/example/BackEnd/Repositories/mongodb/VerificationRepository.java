package com.example.BackEnd.Repositories.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.BackEnd.Entity.EmailVerification;

public interface VerificationRepository extends MongoRepository<EmailVerification, String>{
    EmailVerification findByEmail(String email);
    EmailVerification findByCode(String code);
}
