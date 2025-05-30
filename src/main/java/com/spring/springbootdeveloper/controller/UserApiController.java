package com.spring.springbootdeveloper.controller;


import com.spring.springbootdeveloper.dto.AddUserRequest;
import com.spring.springbootdeveloper.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@Controller
public class UserApiController {
    
    private final UserService userService;
    
    @PostMapping("/user")
    public String signup(AddUserRequest request) {
        userService.save(request);  // 회원가입 메소드 호출
        return "redirect:/login";   // 회원가입 이후 로그인 페이지로 이동
    }

//    @GetMapping("/logout")
//    public String logout(HttpServletRequest request, HttpServletResponse response) {
//        new SecurityContextLogoutHandler().logout(request, response
//                , SecurityContextHolder.getContext().getAuthentication());
//
//        return "redirect:/login";
//    }

}
