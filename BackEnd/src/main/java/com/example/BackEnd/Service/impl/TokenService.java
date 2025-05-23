package com.example.BackEnd.Service.impl;

import com.example.BackEnd.Entity.Token;
import com.example.BackEnd.Entity.User;
import com.example.BackEnd.Repositories.mysql.TokenRepository;
import com.example.BackEnd.Service.ITokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenService implements ITokenService {
    private final TokenRepository tokenRepository;
    @Value("${jwt.expiration}")
    private int expiration;

    @Override
    public void addToken(User user, String token, boolean isMobile){
        List<Token> userTokens = tokenRepository.findByUser(user);
        int countToken = userTokens.size();

        int maxToken = 3;
        if(countToken > maxToken){
            Token tokenToDelete = userTokens.getFirst();
            tokenRepository.delete(tokenToDelete);
        }

        LocalDateTime expirationDateTime = LocalDateTime.now().plusSeconds(expiration);
        Token newToken = Token.builder()
                .user(user)
                .token(token)
                .revoked(false)
                .expired(false)
                .tokenType("Bearer")
                .expirationDate(expirationDateTime)
                .isMobile(isMobile)
                .build();
        tokenRepository.save(newToken);
    }

    @Override
    public Token deleteToken(String token) {
        Token tokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));
        tokenRepository.delete(tokenEntity);
        return tokenEntity;
    }
}

