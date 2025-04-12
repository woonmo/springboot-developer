package com.spring.springbootdeveloper.repository;

import com.spring.springbootdeveloper.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepositoty extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUserId(Long userId);
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
    void deleteByRefreshToken(String refreshToken);
}
