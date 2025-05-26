package com.example.BackEnd.DTO;

import com.example.BackEnd.Entity.Department;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorDTO {
    @JsonProperty("department_id")
    private Department department;

    @JsonProperty("license_number")
    private String licenseNumber;

    @JsonProperty("experience_years")
    private Integer experienceYears;

    @JsonProperty("description")
    private String description;
}
