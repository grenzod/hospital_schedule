package com.example.BackEnd.DTO;

import java.util.List;

import com.example.BackEnd.Model.ContentBlock;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleDTO {
    private String title;
    // private String content;

    @JsonProperty("content_block")
    private List<ContentBlock> contentBlocks;
}
