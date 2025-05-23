package com.example.BackEnd.Repositories.mongodb;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import com.example.BackEnd.Entity.Article;

public interface ArticleRepository extends MongoRepository<Article, String> {

    @Query("{'status': true, 'title': {$regex: ?0, $options: 'i'}}")
    Page<Article> findAllArticlesStatusTrue(String keyword, PageRequest pageRequest);

    @Query("{'title': {$regex: ?0, $options: 'i'}}")
    Page<Article> findAllArticles(String keyword, PageRequest pageRequest);
}
