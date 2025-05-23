package com.example.BackEnd.Service.impl;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;

import com.example.BackEnd.DTO.ArticleDTO;
import com.example.BackEnd.Entity.Article;
import com.example.BackEnd.Model.ContentBlock;
import com.example.BackEnd.Model.Image;
import com.example.BackEnd.Repositories.mongodb.ArticleRepository;
import com.example.BackEnd.Service.IArticleService;
import com.example.BackEnd.Utils.StoreFileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ArticleService implements IArticleService {
    private final ArticleRepository articleRepository;

    @Override
    public Page<Article> getAllArticles(PageRequest pageRequest, String keyword, boolean status) {
        if (status)
            return articleRepository.findAllArticlesStatusTrue(keyword, pageRequest);
        else
            return articleRepository.findAllArticles(keyword, pageRequest);
    }

    @Override
    public Article createArticle(ArticleDTO articleDTO, MultipartFile thumbnail, List<MultipartFile> files) throws Exception {
        if (files != null) {
            List<String> images = new ArrayList<>();
            if (files.size() > Image.MAXIMUM_IMAGES_PER_PRODUCT) {
                throw new Exception("You can only upload maximum " + Image.MAXIMUM_IMAGES_PER_PRODUCT +" images");
            }
            for (MultipartFile file : files) {
                if(StoreFileUtil.checkFile(file)){
                    String fileName = StoreFileUtil.storeFile(file, "articles");
                    images.add(fileName);
                }                
            }            
            int dem = 0;
            for (ContentBlock x : articleDTO.getContentBlocks()) {
                if (x.getType().equals("image")) {
                    x.getImage().setUrl(images.get(dem++));
                }
            }
        }

        Article article = Article.builder()
                .title(articleDTO.getTitle())
                .thumbnail(StoreFileUtil.storeFile(thumbnail, "thumbnailArticle"))
                .contentBlocks(articleDTO.getContentBlocks())
                .status(true)
                .build();
        return articleRepository.save(article);
    }

    @Override
    public Article addBlockToArticle(String articleId, ContentBlock block) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        article.getContentBlocks().add(block);
        return articleRepository.save(article);
    }

    @Override
    public Optional<Article> getArticleById(String articleId) {
        return articleRepository.findById(articleId);
    }
}
