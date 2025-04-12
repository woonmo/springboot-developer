package com.spring.springbootdeveloper.config;

import com.spring.springbootdeveloper.config.jwt.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer ";


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        log.info("Processing request: {}", path);

        // OAuth2 및 정적 리소스 경로 제외
        if (path.startsWith("/oauth2/") || path.startsWith("/login/oauth2/") || path.equals("/favicon.ico")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 가져온 값에서 접두사 제거
        String token = getAccessToken(request);

        // 가져온 토큰이 유효한지 확인하고 유효한 때는 인증 정보를 설정
        if (token != null && tokenProvider.validateToken(token)) {
            Authentication authentication = tokenProvider.getAuthentication(token);
            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Set authentication for user: {}", authentication.getName());
            }
        }
        else {
            log.debug("No valid token found for user: {}", path);
        }
        filterChain.doFilter(request, response);
    }


    private String getAccessToken(HttpServletRequest request) {
        // 요청 헤더의 Authorization 키의 값 조회
        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            return authorizationHeader.substring(TOKEN_PREFIX.length());
        }

        // 쿠키에서 access_token 추출
        if (request.getCookies() != null) { // 쿠키가 들어 있다면
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
