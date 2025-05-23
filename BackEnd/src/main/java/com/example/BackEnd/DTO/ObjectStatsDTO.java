package com.example.BackEnd.DTO;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ObjectStatsDTO {
    private int year;
    private int month;
    private long count;
}
