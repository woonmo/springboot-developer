package com.spring.springbootdeveloper.config.oauth;

import com.spring.springbootdeveloper.domain.User;
import com.spring.springbootdeveloper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthorizationException {
        // 소셜로그인 플랫폼 확인
        String provider = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 login attempt for provider: {}", provider);



        // 요청을 바탕으로 유저 정보를 담은 객체 반환
        OAuth2User user = super.loadUser(userRequest);  // loadUser(): OAuth 서비스에서 제공하는 정보를 기반으로 유저 객체를 생성해줌
        log.info("Loaded user attributes: {}", user.getAttributes());

        // 사용자 객체는 식별자, 이름, 이메일, 프로필 사진 링크 등의 정보를 담고 있다.
        User savedUser = saveOrUpdate(userRequest, user);

        // attributes에 provider 및 기타 정보 추가
        Map<String, Object> attributes = new HashMap<>(user.getAttributes());   // 유저정보가 들어있는 맵을 생성
        attributes.put("provider", provider);
        attributes.put("email", savedUser.getEmail());
        attributes.put("nickname", savedUser.getNickname());

        String nameAttributeKey = "kakao".equals(provider) ? "id" : "email";


//        return user;
        // OAuth2 인증 후 사용자 정보를 스프링 시큐리티에 전달하기 위해
        // DefaultOAuth2User 객체를 생성
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), // SimpleGrantedAuthority: 스프링 시큐리티에서 권한을 나타내는 클래스
                // Collections.singleton 은 유저가 ROLE_USER 권한만 가진다고 정의
                attributes,
                nameAttributeKey
        );
    }

    // 유저가 있으면 업데이트, 없으면 유저 생성
    private User saveOrUpdate(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        // 카카오 혹은 구글 로그인 시 분기 해야 함
        String providerId = userRequest.getClientRegistration().getRegistrationId();    // Google or KaKao
        String email;
        String name;

        if ("kakao".equals(providerId)) {
            // 카카오 로그인 로직 처리
            // Loaded user attributes: {id=4212814479, connected_at=2025-04-12T00:09:13Z
            // , properties={nickname=이원모, profile_image=http://k.kakaocdn.net/dn/btb0LS/btqH0Whz1VG/4V69ypeuj5tN0ddQLL051k/img_640x640.jpg, thumbnail_image=http://k.kakaocdn.net/dn/btb0LS/btqH0Whz1VG/4V69ypeuj5tN0ddQLL051k/img_110x110.jpg}
            // , kakao_account={profile_nickname_needs_agreement=false, profile_image_needs_agreement=false
            // , profile={nickname=이원모, thumbnail_image_url=http://k.kakaocdn.net/dn/btb0LS/btqH0Whz1VG/4V69ypeuj5tN0ddQLL051k/img_110x110.jpg, profile_image_url=http://k.kakaocdn.net/dn/btb0LS/btqH0Whz1VG/4V69ypeuj5tN0ddQLL051k/img_640x640.jpg, is_default_image=false, is_default_nickname=false}, has_email=true, email_needs_agreement=false, is_email_valid=true, is_email_verified=true, email=wonmo151@daum.net}}
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount == null) {
                log.error("kakao_account is null for attributes: {}", attributes);
                throw new OAuth2AuthenticationException("Failed to retrieve kakao_account");
            }

            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile == null) {
                log.error("profile is null for kakao_account: {}", kakaoAccount);
                throw new OAuth2AuthenticationException("Failed to retrieve profile");
            }

            email = (String) kakaoAccount.get("email");
            name = (String) profile.get("nickname");


        }
        else if ("google".equals(providerId)) {
            // 구글 로그인 로직 처리
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
        }
        else if ("naver".equals(providerId)) {
            // Loaded user attributes: {resultcode=00, message=success, response={id=C3MQPhJWHobtcZufNt4uWTojVPB1xUaodMLaky3BfKU, email=wonmo151@naver.com, name=이원모}}
            Map<String, Object> naverUserInfo = (Map<String, Object>) attributes.get("response");
            email = (String) naverUserInfo.get("email");
            name = (String) naverUserInfo.get("name");
        }
        else {
            throw new IllegalArgumentException("Unknown provider: " + providerId);
        }

        // 이메일이 null 일 경우 처리
        if (email == null) {
            email = providerId + "_" + attributes.get("id");
        }

        User user = userRepository.findByEmail(email)
                .map(entity -> entity.update(name))
                .orElse(User.builder()
                        .email(email)
                        .nickname(name)
                        .provider(providerId)
                        .build());
        return userRepository.save(user);
    }
}
