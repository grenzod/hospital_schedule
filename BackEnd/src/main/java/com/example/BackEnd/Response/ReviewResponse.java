package com.example.BackEnd.Response;

import com.example.BackEnd.Entity.Review;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewResponse {
    private Integer id;

    @JsonProperty("full_mame")
    private String fullName;

    @JsonProperty("comment")
    private String comment;

    public static ReviewResponse fromReview(Review review){
        return ReviewResponse.builder()
                .id(review.getId())
                .fullName(review.getUser().getFullName())
                .comment(extractComment(review.getComment()))
                .build();
    }

    private static String extractComment(String originalComment) {
        int colonIndex = originalComment.indexOf(":");
        if (colonIndex != -1 && colonIndex < originalComment.length() - 1) {
            return originalComment.substring(colonIndex + 1).trim();
        } else {
            return originalComment; 
        }
    }
}
