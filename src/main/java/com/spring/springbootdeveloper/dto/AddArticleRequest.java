package com.spring.springbootdeveloper.dto;

import com.spring.springbootdeveloper.domain.Article;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AddArticleRequest {
    // 글 작성 요청 본문을 받는 객체
    private String title;
    private String content;

    public Article toEntity(String author) {     // 생성자를 사용해 객체 생성, DTO를 Entity로 만들어 주는 메소드
        return Article.builder()
                .title(title)
                .content(content)
                .author(author)
                .build();
    }
}
