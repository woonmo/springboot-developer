package com.spring.springbootdeveloper.config.jwt;


import java.time.Duration;
import java.util.Date;
import java.util.Map;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Builder;
import lombok.Getter;
import org.springframework.test.context.ActiveProfiles;

import static java.util.Collections.emptyMap;

@Getter
@ActiveProfiles("test")
public class JwtFactory {

    // JWT 토큰 서비스를 테스트하는 mocking 용 객체
    // mocking 이란 테스트를 실행할 때 객체를 대신하는 가짜 객체

    private String subject = "test@email.com";
    private Date issuedAt = new Date();
    private Date expiration = new Date(new Date().getTime() + Duration.ofDays(14).toMillis());
    private Map<String, Object> claims = emptyMap();

    // 빌더 패턴을 사용해 설정이 필요한 데이터만 선택 결정
    @Builder
    public JwtFactory(String subject, Date issuedAt, Date expiration, Map<String, Object> claims) {
        this.subject = subject != null ? subject : this.subject;
        this.issuedAt = issuedAt != null ? issuedAt : this.issuedAt;
        this.expiration = expiration != null ? expiration : this.expiration;
        this.claims = claims != null ? claims : this.claims;
    }

    public static JwtFactory withDefaultValues() {
        return JwtFactory.builder().build();
    }

    // jjwt 라이브러리를 이용해 JWT 토큰 생성
    public String createToken(JwtProperties jwtProperties) {
        return Jwts.builder()
                .setSubject(subject)
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .addClaims(claims)
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
                .compact();
    }

}
