package com.example.BackEnd.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentDTO {
    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;
}
