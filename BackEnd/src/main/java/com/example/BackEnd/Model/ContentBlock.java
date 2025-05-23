package com.example.BackEnd.Model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentBlock {
    private String type;
    private String text;
    private Image image;
}
