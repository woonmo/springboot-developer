package com.spring.springbootdeveloper.config;
//
//import com.spring.springbootdeveloper.service.UserDetailService;
//import jakarta.servlet.DispatcherType;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Configurable;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//import org.springframework.security.web.SecurityFilterChain;
//
//import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;
//
//@RequiredArgsConstructor
//@EnableWebSecurity
//@Configuration
//public class WebSecurityConfig {
//
//    private final UserDetailService userService;
////    private final BCryptPasswordEncoder bCryptPasswordEncoder;
//
//
//    // 스프링 시큐리티 기능 비활성화
//    @Bean
//    public WebSecurityCustomizer configure() {
//        return (web) -> web.ignoring()
//                .requestMatchers(toH2Console())     // 데이터베이스
//                .requestMatchers("/static/**");   // JS, CSS 등 폴더
//    }
//
//    // 특정 HTTP 요청에 대한 웹 기반 보안 구성
//    @Bean
//    public SecurityFilterChain filterChain (HttpSecurity http) throws Exception {
//
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                .authenticationProvider(daoAuthenticationProvider())
//                .authorizeHttpRequests((auth) -> auth
//                                .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
//                                .requestMatchers("/login", "/signup", "/user").permitAll()
//                                .anyRequest().authenticated()
//                )
//                .formLogin((login) -> login
//                                .loginPage("/login")
//                                .usernameParameter("username")
//                                .passwordParameter("password")
//                                .defaultSuccessUrl("/articles")
//                )
//                .logout((logout) -> logout
//                                .logoutSuccessUrl("/login")
//                                .invalidateHttpSession(true)
//                );
//
//        return http.build();
//    }
//
//
//    // 인증 관리자 관련 설정
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }
//
//    @Bean
//    public DaoAuthenticationProvider daoAuthenticationProvider() {
//        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//        authProvider.setUserDetailsService(userService);
//        authProvider.setPasswordEncoder(bCryptPasswordEncoder());
//        return authProvider;
//    }
//
//
//    // 패스워드 인코더로 사용할 빈 등록
//    @Bean
//    public BCryptPasswordEncoder bCryptPasswordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//}

// OAuth 와 JWT 를 사용하기 위한 별도 Security Config 파일 작성을 위해 모두 주석처리
