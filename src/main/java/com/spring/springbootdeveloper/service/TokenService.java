package com.spring.springbootdeveloper.service;

import com.spring.springbootdeveloper.config.jwt.TokenProvider;
import com.spring.springbootdeveloper.domain.User;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    public TokenService(TokenProvider tokenProvider, RefreshTokenService refreshTokenService, UserService userService) {
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
    }

    public String createNewAccessToken(String refreshToken) {
        // 토큰 유효성 검시에 실패하면 예외 발생
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Unexpected token: " + refreshToken);
        }

        Long userId = refreshTokenService.findByRefreshToken(refreshToken).getUserId();
        User user = userService.findById(userId);

        return tokenProvider.generateToken(user, Duration.ofHours(2));  // 2시간 유효한 토큰 반환
    }
}
