package com.example.BackEnd.Controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.BackEnd.DTO.ArticleDTO;
import com.example.BackEnd.Entity.Article;
import com.example.BackEnd.Model.ContentBlock;
import com.example.BackEnd.Repositories.mongodb.ArticleRepository;
import com.example.BackEnd.Response.ObjectListResponse;
import com.example.BackEnd.Service.IArticleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/articles")
@RequiredArgsConstructor
public class ArticleController {
   private final IArticleService iArticleService;
   private final ArticleRepository articleRepository;

   @GetMapping("/")
   public ResponseEntity<?> getAllArticles(
           @RequestParam(defaultValue = "0") int page,
           @RequestParam(defaultValue = "10") int limit,
           @RequestParam(defaultValue = "") String keyword,
           @RequestParam(defaultValue = "true") boolean status) {
       try {
           PageRequest pageRequest = PageRequest.of(
                   page,
                   limit,
                   Sort.by("id").descending());

           Page<Article> articles = iArticleService.getAllArticles(pageRequest, keyword, status);
           int totalPages = articles.getTotalPages();
           List<Article> articleResponseList = articles.getContent();

           return ResponseEntity.ok().body(ObjectListResponse.builder()
                   .objects(articleResponseList)
                   .total(totalPages)
                   .build());
       } catch (Exception e) {
           return ResponseEntity.badRequest().body(e.getMessage());
       }
   }

   @GetMapping("/{id}")
   public ResponseEntity<?> getArticle(@PathVariable String id) {
       return iArticleService.getArticleById(id)
               .map(ResponseEntity::ok)
               .orElse(ResponseEntity.notFound().build());
   }

   @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   public ResponseEntity<?> createArticle(
           @RequestPart("articles") @Valid ArticleDTO articleDTO,
           @RequestPart("thumbnail") MultipartFile thumbnail,
           @RequestPart("files") List<MultipartFile> files
   ) {
       try{
           Article article = iArticleService.createArticle(articleDTO, thumbnail, files);
           return ResponseEntity
                   .status(HttpStatus.CREATED)
                   .body(article);
       }
       catch (Exception e){
           return ResponseEntity.badRequest().body(e.getMessage());
       }
   }

   @PostMapping("/{id}/blocks")
   public ResponseEntity<Article> addBlock(
           @PathVariable String id,
           @RequestBody ContentBlock block) {

       Article updated = iArticleService.addBlockToArticle(id, block);
       return ResponseEntity.ok(updated);
   }

   @DeleteMapping("/{id}")
   public ResponseEntity<?> deleteArticle(
           @PathVariable String id) {

       articleRepository.deleteById(id);
       return ResponseEntity.ok().body("Xóa thành công");
   }
}
