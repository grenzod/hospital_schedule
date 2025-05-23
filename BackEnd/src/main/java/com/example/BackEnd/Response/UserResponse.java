package com.example.BackEnd.Response;

import com.example.BackEnd.Entity.Role;
import com.example.BackEnd.Entity.User;
import com.example.BackEnd.Enum.Gender;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private Integer id;

    @JsonProperty("full_name")
    private String fullName;

    private String email;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String address;

    private boolean active;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;

    private Role role;

    private String avatar_url;

    private LocalDateTime created_at;

    public static UserResponse fromUser(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .address(user.getAddress())
                .active(user.getIsActive())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .role(user.getRole())
                .avatar_url(user.getAvatarUrl())
                .created_at(user.getCreatedAt())
                .build();
    }
}
