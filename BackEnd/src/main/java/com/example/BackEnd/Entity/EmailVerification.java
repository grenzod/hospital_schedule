package com.example.BackEnd.Entity;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.persistence.GeneratedValue;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "verification")
public class EmailVerification {
    @Id @GeneratedValue
    private String id;

    private String email;

    private String code;

    @CreatedDate
    @Field("created_at")
    private Instant expiresAt;
}
