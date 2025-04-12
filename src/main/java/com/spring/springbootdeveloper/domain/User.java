package com.spring.springbootdeveloper.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    // OAuth2 하면서 추가
    @Column(name = "nickname", unique = true)
    private String nickname;

    @Column(name = "provider")
    private String provider;


    @Builder
    public User(String email, String password, String nickname, String provider) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;   // OAuth2 하면서 추가
        this.provider = provider;   // 소셜로그인 분리용
    }

    // 사용자 이름 변경
    public User update(String nickname) {
        this.nickname = nickname;
        return this;
    }

    @Override   // 권한 반환
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("user"));
    }

    // 사용자의 id를 반환(고유한 값)
    @Override
    public String getUsername() {
        return email;
    }

    // 사용자의 패스워드를 반환
    @Override
    public String getPassword() {
        return password;
    }


    // 계정 만료 여부 반환
    @Override
    public boolean isAccountNonExpired() {
        // 만료 되었는지 확인하는 로직
        return true;    // true -> 잠금되지 않음
    }

    // 계정 잠금 여부 반환
    @Override
    public boolean isAccountNonLocked() {
        // 계정 잠금 되었는지 확인하는 로직
        return true;    // true -> 잠금되지 않음
    }


    // 패스워드의 만료 여부 반환
    @Override
    public boolean isCredentialsNonExpired() {
        // 패스워드가 만료되었는지 확인하는 로직
        return true;    // true -> 만료되지 않음
    }

    @Override
    public boolean isEnabled() {
        // 계정이 사용 가능한지 확인하는 로직
        return true; // true -> 사용 가능
    }



}
