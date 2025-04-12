package com.spring.springbootdeveloper.config.oauth;

import com.spring.springbootdeveloper.config.jwt.TokenProvider;
import com.spring.springbootdeveloper.domain.RefreshToken;
import com.spring.springbootdeveloper.domain.User;
import com.spring.springbootdeveloper.repository.RefreshTokenRepositoty;
import com.spring.springbootdeveloper.service.UserService;
import com.spring.springbootdeveloper.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";      // 재발급 토큰 쿠키명
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);   // 재발급 토큰 유효기간 (14일)
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);     // 접속 유효 기간 1일
    public static final String REDIRECT_PATH = "/articles";     // 접속 시 REDIRECT 경로

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepositoty refreshTokenRepositoty;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository;
    private final UserService userService;

    // 인증에 성공했을 시 메소드
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response
            , Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String provider = (String) attributes.get("provider");
        String email = (String) attributes.get("email");

        log.info("Provider: {}, Email: {}, Attributes: {}", provider, email, attributes);

        if ("kakao".equals(provider) && email == null) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttributes().get("kakao_account");
            log.info("kakao account : {}", kakaoAccount);
            if (kakaoAccount != null) {
                email = (String) kakaoAccount.get("email");
                log.info("kakao email : {}", email);
            }
        }
        else {
            email = (String) oAuth2User.getAttributes().get("email");
        }

        try {
            User user = userService.findByEmail(email);

            // 리프레시 토큰 생성 -> DB 저장 -> 쿠키에 저장
            String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);
            saveRefreshToken(user.getId(), refreshToken);
            addRefreshTokenToCookie(request, response, refreshToken);

            // 액세스 토큰 생성 -> 패스에 액세스 토큰을 추가
            String accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
            String targetUrl = getTargetUrl(accessToken);

            // 액세스 토큰을 HttpOnly에 추가
            addAccessTokenToCookie(response, accessToken);

            // 인증 관련 설정값, 쿠키 제거
            clearAuthenticationAttributes(request, response);

            // 리다이렉트
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } catch (IllegalArgumentException e) {
            log.error("Failed to find for email: {}. Redirecting to login {}", email, e.getMessage());
            response.sendRedirect("/login?error=user_not_found");
        }
    }// end of public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)

    // 액세스 토큰을 쿠키에 넣는 메소드
    private void addAccessTokenToCookie(HttpServletResponse response, String accessToken) {
        Cookie accessTokenCookie = new Cookie("access_token", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge((int) ACCESS_TOKEN_DURATION.getSeconds());
        response.addCookie(accessTokenCookie);
    }


    // 생성된 리프레시 토큰을 전달받아 데이터베이스에 저장
    private void saveRefreshToken(Long userId, String newRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepositoty.findByUserId(userId)
                .map(entity -> entity.update(newRefreshToken))
                .orElse(new RefreshToken(userId, newRefreshToken));
        log.info("Saved refresh token for userId: {}", userId);
        refreshTokenRepositoty.save(refreshToken);
    }

    // 생성된 리프레시 토큰을 쿠키에 저장
    private void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();
        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);  // 기존 쿠키 삭제
        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge);  // 새로운 쿠키 등록
    }

    // 인증 관련 설정값, 쿠키 제거
    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    // 액세스 토큰을 패스에 추가하는 메소드
    private String getTargetUrl(String accessToken) {
//        return UriComponentsBuilder.fromUriString(REDIRECT_PATH)
//                .queryParam("token", accessToken)   // key: token, value: accessToken
//                .build().toUriString();
        return REDIRECT_PATH;
    }
}
