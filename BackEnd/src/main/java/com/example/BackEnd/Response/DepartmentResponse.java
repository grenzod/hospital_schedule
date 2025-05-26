package com.example.BackEnd.Response;

import com.example.BackEnd.Entity.Department;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentResponse {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;

    @JsonProperty("is_available")
    private boolean isAvailable;

    public static DepartmentResponse fromDepartment(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .thumbnailUrl(department.getThumbnailUrl())
                .description(department.getDescription())
                .isAvailable(department.isAvailable())
                .build();
    }
}
