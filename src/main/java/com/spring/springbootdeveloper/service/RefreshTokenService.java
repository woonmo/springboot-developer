package com.spring.springbootdeveloper.service;

import com.spring.springbootdeveloper.domain.RefreshToken;
import com.spring.springbootdeveloper.repository.RefreshTokenRepositoty;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepositoty refreshTokenRepositoty;

    public RefreshTokenService(RefreshTokenRepositoty refreshTokenRepositoty) {
        this.refreshTokenRepositoty = refreshTokenRepositoty;
    }

    public RefreshToken findByRefreshToken(String refreshToken) {
        return refreshTokenRepositoty.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected token: " + refreshToken));
    }
}
