package com.spring.springbootdeveloper.config.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("jwt")     // 자바 클래스에서 property 값을 가져와 사용하는 어노테이션 application.yml 에서 세팅함
public class JwtProperties {

    private String issuer;
    private String secretKey;
}
