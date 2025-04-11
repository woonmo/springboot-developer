package com.spring.springbootdeveloper.controller;

import com.spring.springbootdeveloper.domain.Article;
import com.spring.springbootdeveloper.dto.ArticleListViewResponse;
import com.spring.springbootdeveloper.dto.ArticleViewResponse;
import com.spring.springbootdeveloper.service.BlogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class BlogViewController {

    private final BlogService blogService;

    // Dependency Injection
    public BlogViewController(BlogService blogService) {
        this.blogService = blogService;
    }

    // 글 목록 조회
    @GetMapping("/articles")
    public String getArticles(Model model) {
        List<ArticleListViewResponse> articles = blogService.findAll().stream()
                .map(ArticleListViewResponse :: new)
                .toList();

        model.addAttribute("articles", articles);
        return "articleList";
    }// end of public String getArticles(Model model) -----------------

    // 글 한 개 조회
    @GetMapping("/articles/{id}")
    public String getArticle(@PathVariable Long id, Model model) {
        Article article = blogService.findById(id);
        model.addAttribute("article", new ArticleViewResponse(article));

        return "article";
    }// end of public String getArticle(@PathVariable Long id, Model model) ------------

    // 글 생성 or 수정
    @GetMapping("/new-article")
    // id 를 가진 쿼리 파라미터의 값을 id 변수에 매핑(신규 작성일 경우 없을 수 있음)
    public String newArticle(@RequestParam(required = false) Long id, Model model) {
        if (id == null) { // id가 Null 이라면 신규 글 작성
            model.addAttribute("article", new ArticleViewResponse());
        }
        else {  // 수정
            Article article = blogService.findById(id);
            model.addAttribute("article", new ArticleViewResponse(article));
        }
        return "newArticle";
    }// end of public String newArticle(@RequestParam(required = false) Long id, Model model) -----------
}
