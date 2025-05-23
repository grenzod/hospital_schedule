package com.example.BackEnd.Repositories.mysql;

import com.example.BackEnd.Entity.Token;
import com.example.BackEnd.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {
    List<Token> findByUser(User user);
    Optional<Token> findByToken(String token);
}
