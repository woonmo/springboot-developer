package com.spring.springbootdeveloper.config.jwt;

import com.spring.springbootdeveloper.domain.User;
import com.spring.springbootdeveloper.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class TokenProviderTest {

    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private UserRepository userRepository;


    // generateToken 검증 테스트
    @DisplayName("generateToken(): 유저 정보와 만료 기간을 전달해 토큰을 만들 수 있다.")
    @Test
    void generateToken() {
        // given: 토큰에 유저 정보를 추가하기 위한 테스트 유저 생성
        User testUser = userRepository.save(User.builder()
                .email("user@gmail.com")
                .password("test")
                .build());

        // when: 토큰 제공자의 generateToken() 메소드를 호출해 토큰 생성
        String token = tokenProvider.generateToken(testUser, Duration.ofDays(14));

        // than: jjwt 라이브러리를 사용해 토큰을 복호화.
        // 토큰을 만들 때 클레임으로 넣어둔 id값이 given 절에서 만든 유저id 와 동일한지 확인
        Long userId = Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody()
                .get("id", Long.class);

        assertThat(userId).isEqualTo(testUser.getId());
    }


    // validToken() 검증 테스트
    @DisplayName("validToken(): 만료된 토큰인 때에 유효성 검증에 실패한다.")
    @Test
    void validToken_invalidToken() {
        // given: jjwt 라이브러리를 사용해 토큰을 생성
        String token = JwtFactory.builder()
                .expiration(new Date(new Date().getTime() - Duration.ofDays(7).toMillis()))
                // 만료 시간은 1970년 1월 1일부터 현재 시간을 밀리초 단위로 치환한 값 new Date().getTime()에서
                // 1000을 빼 이미 만료된 토큰으로 생성
                .build()
                .createToken(jwtProperties);

        // when: 토큰 제공자의 validToken() 메소드를 호출해 유효한 토큰인지 검증한 뒤 결과값을 반환
        boolean result = tokenProvider.validateToken(token);

        // then: 반환값이 false(유효하지 않은)인 것을 확인
        assertThat(result).isFalse();
    }

    // validToken() 검증 테스트
    @DisplayName("validToken(): 유효한 토큰인 때에 유효성 검증에 성공한다.")
    @Test
    void validToken_validToken() {
        // given: jjwt 라이브러리를 사용해 토큰을 생성
        String token = JwtFactory.withDefaultValues().createToken(jwtProperties);
        // 만료시간은 현재로부터 14일 뒤로 만료되지 않은 토큰을 생성

        // when: 토큰 제공자의 validToken() 메소드를 호출해 유효한 토큰인지 검증한 뒤 결과값을 반환
        boolean result = tokenProvider.validateToken(token);

        // then: 반환값이 true(유효한)인 것을 확인
        assertThat(result).isTrue();
    }


    // getAuthentication() 검증 테스트
    @DisplayName("getAuthentication(): 토큰 기반으로 인증 정보를 가져올 수있다.")
    @Test
    void getAuthentication() {
        // given: jjwt 라이브러리를 사용해 토큰을 생성
        String userEmail = "user@gmail.com";    // 토큰의 제목
        String token = JwtFactory.builder()
                .subject(userEmail)
                .build()
                .createToken(jwtProperties);

        // when: 토큰 제공자의 getAuthentication() 메소드를 사용해 인증 객체를 반환 받음
        Authentication authentication = tokenProvider.getAuthentication(token);

        // then: 반환받은 인증 객체의 유저 이름을 가져와 given 절에서 설정한 subject값인 "user@gmail.com" 와 동일한지 확인
        assertThat(((UserDetails) authentication.getPrincipal()).getUsername()).isEqualTo(userEmail);
    }


    // getUserId() 검증 테스트
    @DisplayName("getUserId(): 토큰으로 유저 ID를 가져올 수 있다.")
    @Test
    void getUserId() {
        // given: jjwt 라이브러리를 사용해 토큰을 생성
        Long userId = 1L;
        String token = JwtFactory.builder()
                .claims(Map.of("id", userId))   // 키는 id, 값은 1인 클레임 추가
                .build()
                .createToken(jwtProperties);
        // when: 토큰 제공자의 getUserId() 메소드를 호출해 유저 id 를 반환 받는다.
        Long userIdByToken = tokenProvider.getUserId(token);

        // then: 반환받은 유저id 가 given절에서 설정한 유저id 값인 1과 동일한지 확인
        assertThat(userIdByToken).isEqualTo(userId);
    }
}
