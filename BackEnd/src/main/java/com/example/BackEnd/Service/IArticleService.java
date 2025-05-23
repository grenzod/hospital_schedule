package com.example.BackEnd.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import com.example.BackEnd.DTO.ArticleDTO;
import com.example.BackEnd.Entity.Article;
import com.example.BackEnd.Model.ContentBlock;

public interface IArticleService {
   Page<Article> getAllArticles(PageRequest pageRequest, String keyword, boolean status);
   Article createArticle(ArticleDTO articleDTO, MultipartFile thumbnail, List<MultipartFile> files) throws Exception;
   Article addBlockToArticle(String articleId, ContentBlock block);
   Optional<Article> getArticleById(String articleId);
}
