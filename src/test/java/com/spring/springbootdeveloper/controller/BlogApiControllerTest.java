package com.spring.springbootdeveloper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.springbootdeveloper.config.error.ErrorCode;
import com.spring.springbootdeveloper.domain.Article;
import com.spring.springbootdeveloper.domain.User;
import com.spring.springbootdeveloper.dto.AddArticleRequest;
import com.spring.springbootdeveloper.dto.UpdateArticleRequest;
import com.spring.springbootdeveloper.repository.BlogRepository;
import com.spring.springbootdeveloper.repository.UserRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.security.Principal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc   // MVC 환경 셋업
@ActiveProfiles("test")
class BlogApiControllerTest {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;    // 직렬화, 역직렬화를 위한 클래스
                                            // 직렬화: 자바 객체를 JSON 객체로 변환
                                            // 역직렬화: JSON 객체를 자바 객체로 변환
    @Autowired
    protected WebApplicationContext context;

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    UserRepository userRepository;

    User user;

    @BeforeEach
    void setSecurityContext() {
        userRepository.deleteAll();
        user = userRepository.save(User.builder()
                .email("user@gmail.com")
                .password("test")
                .build());

        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
    }

    @BeforeEach
    public void mockMvcSetup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.context)
                .build();   // MVC 환경 설정
        blogRepository.deleteAll(); // 데이터베이스 비우기
    }


    @DisplayName("addArticle: 블로그 글 추가에 성공한다.")
    @Test
    public void addArticle() throws Exception {
        // given
        final String url = "/api/articles";
        final String title = "안녕하소";
        final String content = "반갑소";
        final AddArticleRequest userRequest = new AddArticleRequest(title, content);

        // 객체 JSON 으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(userRequest);

        // OAuth2 이후 추가
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("username");

        // when
        // 설정한 내용을 바탕으로 요청 전송
        ResultActions result = mvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .principal(principal)
                        .content(requestBody));

        // than
        result.andExpect(status().isCreated());

        List<Article> articles = blogRepository.findAll();

        assertThat(articles.size()).isEqualTo(1);             // 글 개수가 1개인지 검증
        assertThat(articles.get(0).getTitle()).isEqualTo(title);        // 제목이 맞는지 검증
        assertThat(articles.get(0).getContent()).isEqualTo(content);    // 내용이 맞는지 검증
    }// p.146


    @DisplayName("findAllArticle: 블로그 글 목록 조회에 성공합니다.")
    @Test
    public void findAllArticles() throws Exception {
        // given: 블로그 글을 등록한다
        final String url = "/api/articles";
        Article savedArticle = createDefaultArticle();


        // when: 블로그 글을 전체조회한다.
        ResultActions resultActions = mvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON));   // JSON 타입으로 받겠다.

        // than: 응답코드가 200 OK 이고, 0번째 요소의 제목과 내용을 확인한다.
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(savedArticle.getTitle()))
                .andExpect(jsonPath("$[0].content").value(savedArticle.getContent()));
    }

    @DisplayName("findArticle: 블로그 글 조회에 성공한다")
    @Test
    public void findArticle() throws Exception {
        // given: 블로그 글을 저장한다.
        final String url = "/api/articles/{id}";
        Article savedArticle = createDefaultArticle();

        // when: 블로그 글 id 값으로 api를 호출한다.
        final ResultActions resultActions;
        resultActions = mvc.perform(get(url, savedArticle.getId()));


        // than: 응답 코드가 200 OK, content와 title 이 저장한 값과 일치하는지 확인.
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(savedArticle.getTitle()))
                .andExpect(jsonPath("$.content").value(savedArticle.getContent()));
    }

    @DisplayName("deleteArticle: 블로그 글 삭제에 성공한다.")
    @Test
    public void deleteArticle() throws Exception {
        // given: 블로그 글을 저장한다.
        final String url = "/api/articles/{id}";
        Article savedArticle = createDefaultArticle();

        // when: 저장한 블로그 글의 id 값으로 삭제 API 를 호출한다.
        mvc.perform(delete(url, savedArticle.getId()))
                .andExpect(status().isOk());


        // than: 응답코드가 200 OK 이고 글 리스트 전체 조회해 배열의 크기가 0인지 확인한다.
        List<Article> articles = blogRepository.findAll();
//        assertThat(articles.size()).isEqualTo(0);
        assertThat(articles).isEmpty();
    }


    @DisplayName("updateArticle: 블로그 글 수정에 성공한다.")
    @Test
    public void updateArticle() throws Exception {
        // given: 글을 등록하고 수정에 필요한 객체를 만든다.
        final String url = "/api/articles/{id}";
        Article savedArticle = createDefaultArticle();

        final String newTitle = "아니";
        final String newContent = "그건 싫어";

        UpdateArticleRequest userRequest = new UpdateArticleRequest(newTitle, newContent);  // 수정 정보 객체

        // when: UPDATE API 로 수정 요청을 보낸다. JSON 타입으로 미리 만든 객체를 전송한다.
        ResultActions resultActions = mvc.perform(put(url, savedArticle.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(userRequest)));    // body 부분에 JSON 객체 형태로 전송


        // than: 응답코드가 200 OK 인지 확인, 블로그 글 id로 조회한 후 값이 수정되었는지 확인
        resultActions.andExpect(status().isOk());
        Article updateArticle = blogRepository.findById(savedArticle.getId()).get(); // 수정한 글 객체 가져오기
        assertThat(updateArticle.getTitle()).isEqualTo(newTitle);       // 제목 비교
        assertThat(updateArticle.getContent()).isEqualTo(newContent);   // 내용 비교
    }


    @DisplayName("addArticle: 아티클 추가할 때 title이 null 이면 실패한다.")
    @Test
    public void addArticleNullValidation() throws Exception {
        // given
        final String url = "/api/articles";
        final String title = null;
        final String content = "Content";
        final AddArticleRequest userRequest = new AddArticleRequest(title, content);

        final String requestBody = objectMapper.writeValueAsString(userRequest);

        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("username");

        // when
        ResultActions result = mvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .principal(principal)
                        .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
    }

    @DisplayName("addArticle: 아티클 추가할 때 title이 10자를 넘으면 실패한다")
    @Test
    public void addArticleSizeValidation() throws Exception {
        // given
        Faker faker = new Faker();

        final String url = "/api/articles";
        final String title = faker.lorem().characters(11);
        final String content = "content";
        final AddArticleRequest userRequest = new AddArticleRequest(title, content);

        final String requestBody = objectMapper.writeValueAsString(userRequest);
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("username");

        // when
        ResultActions result = mvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .principal(principal)
                        .content(requestBody));
        // then
        result.andExpect(status().isBadRequest());
    }


    @DisplayName("findArticle: 잘못된 HTTP 메소드로 아티클을 조회하려고 하면 조회에 실패한다.")
    @Test
    public void invalidHttpMethod() throws Exception {
        // given
        final String url = "/api/articles/{id}";

        // when
        final ResultActions resultActions = mvc.perform(post(url, 1));

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").value(ErrorCode.METHOD_NOT_ALLOWED.getMessage()));
    }

    @DisplayName("findArticle: 존재하지 않는 아티클을 조회하려고 하면 조회에 실패한다.")
    @Test
    public void findArticleInvalidArticle() throws Exception {
        // given
        final String url = "/api/articles/{id}";
        final long invalidId = 1;

        // when
        final ResultActions resultActions = mvc.perform(get(url, invalidId));

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.ARTICLE_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.code").value(ErrorCode.ARTICLE_NOT_FOUND.getCode()));
    }

    // 글을 하나 저장해주는 공통 메소드
    private Article createDefaultArticle() {
        return blogRepository.save(Article.builder()
                .title("title")
                .author(user.getUsername())
                .content("content")
                .build());
    }
}