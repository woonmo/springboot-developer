package com.spring.springbootdeveloper.service;

import com.spring.springbootdeveloper.domain.Article;
import com.spring.springbootdeveloper.dto.AddArticleRequest;
import com.spring.springbootdeveloper.dto.UpdateArticleRequest;
import com.spring.springbootdeveloper.repository.BlogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;
import java.util.List;

@RequiredArgsConstructor    // final이 붙거나 @NotNull이 붙은 필드의 생성자 추가, Bean을 생성자로 생성자로
@Service
public class BlogService {

    private final BlogRepository blogRepository;

//    @RequiredArgsConstructor 사용
    // 또는 아래 처럼 생성자를 생성
//    @Autowired
//    public BlogService(BlogRepository blogRepository) {
//        this.blogRepository = blogRepository;
//    }

    // 블로그 글 등록
    public Article save(AddArticleRequest request, String userName) {
        return blogRepository.save(request.toEntity(userName));
    }

    // 블로그 글 전체 조회
    public List<Article> findAll() {
        return blogRepository.findAll();
    }

    // 블로그 글 한 개 조회
    public Article findById(Long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found:"+ id));
    }

    // 블로그 글 삭제
    public void delete(Long id) {
        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found:"+ id));

        authorizeArticleAuthor(article); // 작성자인지 확인
        blogRepository.delete(article); // 삭제 진행
    }

    // 블로그 글 수정
    @Transactional  // 트랜잭션 메소드 ->  처리 중간에 에러가 나더라도 데이터 보존
    public Article update(long id, UpdateArticleRequest request) {
        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found:"+ id));

        authorizeArticleAuthor(article);
        article.update(request.getTitle(), request.getContent());

        return article;
    }


    // 게시글을 작성한 유저인지 확인
    private static void authorizeArticleAuthor(Article article) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        // SecurityContextHolder 에 저장된 (로그인 된) 사람과 일치하는지 확인
        // getName() 은 로그인 id 이다.

        if (!article.getAuthor().equals(userName)) {
//            throw new IllegalArgumentException("not authorized");
            throw new AccessDeniedException("not authorized");
        }
    }
}
