package com.spring.springbootdeveloper.config.error;

import com.spring.springbootdeveloper.config.error.exception.BusinessBaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice   // 이 어노테이션을 사용하면 모든 컨트롤러에서 발생하는 예외를 중앙에서 한꺼번에 처리할 수 있다.
public class GlobalExceptionHandler {

    // HttpRequestMethodNotSupportedException 예외 처리 메소드
    // 지원하지 않은 HTTP method 호출하면 발생 405 CODE
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handle(HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException", e);
        return createErrorResponseEntity(ErrorCode.METHOD_NOT_ALLOWED);
    }

    // BusinessBaseException 예외 처리 메소드
    @ExceptionHandler(BusinessBaseException.class)
    protected ResponseEntity<ErrorResponse> handle(BusinessBaseException e) {
        log.error("BusinessBaseException", e);
        return createErrorResponseEntity(e.getErrorCode());
    }

    // Bad Request 잘못된 인자 전송 시 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException", e);

        // 첫 번째 에러 메시지 추출
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": "+ error.getDefaultMessage())
                .findFirst()
                .orElse("올바르지 않은 입력입니다.");

        // ReseponseEntity
        return new ResponseEntity<>(
                ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, errorMessage),
                ErrorCode.INVALID_INPUT_VALUE.getStatus()
        );
    }

    // 그 외 예외처리 메소드
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handle(Exception e) {
        log.error("Exception", e);
        return createErrorResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> createErrorResponseEntity(ErrorCode errorCode) {
        return new ResponseEntity<>(
                ErrorResponse.of(errorCode),
                errorCode.getStatus());
    }


}
