package com.example.BackEnd.Service;

import com.example.BackEnd.Entity.Token;
import com.example.BackEnd.Entity.User;

public interface ITokenService {
    void addToken(User user, String token, boolean isMobile);
    Token deleteToken(String token); 
}
