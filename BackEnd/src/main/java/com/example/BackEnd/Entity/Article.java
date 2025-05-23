package com.example.BackEnd.Entity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.example.BackEnd.Model.ContentBlock;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "articles")
public class Article {
   @Id
   private String id;
   
   private String thumbnail;

   private String title;

   private boolean status;

   private List<ContentBlock> contentBlocks;

   @CreatedDate
   @Field("created_at")
   private LocalDateTime createdAt;

   @LastModifiedDate
   @Field("updated_at")
   private LocalDateTime updatedAt;
}
