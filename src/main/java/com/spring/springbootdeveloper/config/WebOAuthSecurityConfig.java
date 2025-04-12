package com.spring.springbootdeveloper.config;

import com.spring.springbootdeveloper.config.jwt.TokenProvider;
import com.spring.springbootdeveloper.config.oauth.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.spring.springbootdeveloper.config.oauth.OAuth2SuccessHandler;
import com.spring.springbootdeveloper.config.oauth.OAuth2UserCustomService;
import com.spring.springbootdeveloper.domain.RefreshToken;
import com.spring.springbootdeveloper.domain.User;
import com.spring.springbootdeveloper.repository.RefreshTokenRepositoty;
import com.spring.springbootdeveloper.service.UserService;
import com.spring.springbootdeveloper.util.CookieUtil;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.net.URLEncoder;
import java.util.Optional;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class WebOAuthSecurityConfig {

    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepositoty refreshTokenRepositoty;
    private final UserService userService;


    @Bean
    public WebSecurityCustomizer configure() {  // 스프링 시큐리티 기능 비활성화
        return (web) -> web.ignoring()
                .requestMatchers(toH2Console())
                .requestMatchers("/img/**", "/css/**", "/js/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 토큰 방식으로 인증을 하기 때문에 기존에 사용하면 폼로그인, 세션 비활성화
        http
                // CSRF, Basic, Form 로그인 비활성화
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
//                .logout(logout -> logout.disable())

                // 세션 비활성화
                .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 헤더를 확인할 커스텀 필터 추가
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)


                // 토큰 재발급 URL 은 인증 없이 접근 가능하도록 설정, 나머지 API URL은 인증 필요
                .authorizeHttpRequests((auth) -> auth
                            .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                            .requestMatchers("/api/token", "/login", "/oauth2/authorization/kakao", "/login/oauth2/code/kakao").permitAll()
//                            .requestMatchers("/api/**").authenticated()
                            .anyRequest().authenticated()
                )

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                            .loginPage("/login")
                            // Authorization 요청과 관련된 상태 저장
                            .authorizationEndpoint(authorization -> authorization
                                    .authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository())
                            )
                            .successHandler(oAuthSuccessHandler())  // 인증 성공시 핸들러
                            .userInfoEndpoint(userInfo -> userInfo
                                    .userService(oAuth2UserCustomService)
                            )
                            .failureHandler((request, response, exception) -> {
                                log.error("OAuth2 login failed: {}", exception.getMessage());
                                response.sendRedirect("/login?error=" + URLEncoder.encode(exception.getMessage(), "UTF-8"));
                            })
                )

                // 로그아웃 시 리다이렉트
                .logout(logout -> logout
                            .addLogoutHandler((request, response, authentication) -> {

                                // DB에서 리프레시 토큰 삭제
                                if (authentication != null) {
                                    // 인증 정보가 있으면
                                    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                                    // 메일 정보를 가져와 리프레시 토큰을 삭제한다.
                                    String email = (String) oAuth2User.getAttributes().get("email");

                                    if (email != null) {
                                        try {
                                            User user = userService.findByEmail(email);
                                            refreshTokenRepositoty.deleteByUserId(user.getId());
                                            log.info("Deleted refresh token for user: {}", email);
                                        } catch (IllegalArgumentException e) {
                                            log.error("Failed to delete refresh token: {}", e.getMessage());
                                        }
                                    }
                                    else {
                                        log.warn("Email is null in authentication attributes");
                                    }
                                }
                                else {
                                    // authentication 가 null 이면 쿠키를 통해 리프레시 토큰 삭제
                                    log.warn("Authentication is null during logout");
                                    String refreshToken = getTokenFromCookies(request, "refresh_token");
                                    log.info("Refresh token from Cookie: {}", refreshToken);
                                    if (refreshToken != null) {
                                        Optional<RefreshToken> token = refreshTokenRepositoty.findByRefreshToken(refreshToken);
                                        if (token.isPresent()) {
                                            refreshTokenRepositoty.delete(token.get());
                                            log.info("Deleted refresh token from Cookie {}", refreshToken);
                                        }
                                        else {
                                            log.warn("No refresh token found in repository for value: {}", refreshToken);
                                        }
                                    }
                                }// end of if ~ else (authentication != null)
                                // 쿠키 삭제
                                CookieUtil.deleteCookie(request, response, "access_token");
                                CookieUtil.deleteCookie(request, response, "refresh_token");
                                log.info("Deleted access_token and refresh_token cookies");

                            })
//                            .logoutUrl("/logout")
                            .logoutSuccessUrl("/login")
                            .invalidateHttpSession(true)
                            .deleteCookies("access_token", "refresh_token") // 한번 더 삭제
                )

                // /api로 시작하는 url인 경우 401 상태 코드를 반환하도록 예외 처리
                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**")
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"message:\": \"" + accessDeniedException.getMessage() + "\"}");
                        })
//                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );

        return http.build();
    }

    // 인증 성공 시 handler
    @Bean
    public OAuth2SuccessHandler oAuthSuccessHandler() {
        return new OAuth2SuccessHandler(tokenProvider, refreshTokenRepositoty
                , oAuth2AuthorizationRequestBasedOnCookieRepository()
                , userService);
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }

    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository () {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
    }

    // Cookie 추출 메소드
    private String getTokenFromCookies(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    log.info("Found cookie: {}", cookieName);
                    return cookie.getValue();
                }
            }
        }
        log.warn("No {} cookie found", cookieName);
        return null;
    }
}
