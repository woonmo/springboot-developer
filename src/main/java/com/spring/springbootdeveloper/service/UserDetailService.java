package com.spring.springbootdeveloper.service;


import com.spring.springbootdeveloper.domain.User;
import com.spring.springbootdeveloper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;


    // 사용자 이름(email)으로 사용자의 정보를 가져오는 메소드
    @Override
    public User loadUserByUsername (String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(email + " not found"));
    }// end of public User loadUserByUsername (String email) --------

}
