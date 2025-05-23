package com.example.BackEnd.Model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Image {
    private String url;
    private String caption;
    public static final int MAXIMUM_IMAGES_PER_PRODUCT = 7;
}
