package com.spring.springbootdeveloper.config.error.exception;

import com.spring.springbootdeveloper.config.error.ErrorCode;

public class ArticleNotFoundException extends NotFoundException {

    public ArticleNotFoundException () {
        super(ErrorCode.ARTICLE_NOT_FOUND);
    }
}
