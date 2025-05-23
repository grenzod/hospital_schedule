package com.example.BackEnd.Response;

import com.example.BackEnd.Entity.Doctor;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorResponse {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("department_name")
    private String departmentName;

    @JsonProperty("license_number")
    private String licenseNumber;

    @JsonProperty("experience_years")
    private Integer experienceYears;

    @JsonProperty("description")
    private String description;

    @JsonProperty("is_available")
    private Boolean isAvailable;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    public static DoctorResponse fromDoctor(Doctor doctor){
        return DoctorResponse.builder()
                .id(doctor.getId())
                .fullName(doctor.getUser().getFullName())
                .departmentName(doctor.getDepartment().getName())
                .licenseNumber(doctor.getLicenseNumber())
                .experienceYears(doctor.getExperienceYears())
                .description(doctor.getDescription())
                .isAvailable(doctor.getIsAvailable())
                .avatarUrl(doctor.getAvatarUrl())
                .build();
    }
}
