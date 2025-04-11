package com.spring.springbootdeveloper.config;

import com.spring.springbootdeveloper.config.jwt.TokenProvider;
import com.spring.springbootdeveloper.config.oauth.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.spring.springbootdeveloper.config.oauth.OAuth2SuccessHandler;
import com.spring.springbootdeveloper.config.oauth.OAuth2UserCustomService;
import com.spring.springbootdeveloper.repository.RefreshTokenRepositoty;
import com.spring.springbootdeveloper.service.UserService;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@RequiredArgsConstructor
@Configuration
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
                .logout(logout -> logout.disable())

                // 세션 비활성화
                .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 헤더를 확인할 커스텀 필터 추가
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)


                // 토큰 재발급 URL 은 인증 없이 접근 가능하도록 설정, 나머지 API URL은 인증 필요
                .authorizeHttpRequests((auth) -> auth
                            .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                            .requestMatchers("/api/token", "/login").permitAll()
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
                )

                // 로그아웃 시 리다이렉트
                .logout(logout -> logout
                        .logoutSuccessUrl("/login")
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
}
