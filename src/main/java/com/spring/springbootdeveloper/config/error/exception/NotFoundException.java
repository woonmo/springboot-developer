package com.spring.springbootdeveloper.config.error.exception;

import com.spring.springbootdeveloper.config.error.ErrorCode;

public class NotFoundException extends BusinessBaseException {

    public NotFoundException (ErrorCode errorCode) {
        super(errorCode.getMessage(), errorCode);
    }

    public NotFoundException() {
        super(ErrorCode.NOT_FOUND);
    }
}
