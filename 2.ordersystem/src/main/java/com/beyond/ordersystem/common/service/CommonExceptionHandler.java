package com.beyond.ordersystem.common.service;

import com.beyond.ordersystem.common.dto.CommonErrorDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

// controller어노테이션이 붙어 있는 클래스의 모든 예외를 모니터링 하여 예외를 인터셉팅
@ControllerAdvice
@Slf4j
public class CommonExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illegalException(IllegalArgumentException e) { // IllegalArgsExcep 주입
        log.error(e.getMessage());
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getMessage()), HttpStatus.BAD_REQUEST); // 400
//        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), "잘못된 입력값입니다.") <body>, HttpStatus.BAD_REQUEST<header>); // 400
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> entityNotFound(EntityNotFoundException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(
                new CommonErrorDto(HttpStatus.NOT_FOUND.value(), e.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validationError(MethodArgumentNotValidException e) {
        log.error(e.getMessage());
        String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), errorMessage), HttpStatus.BAD_REQUEST);
    }



//    이거로 다 걸리는거 아니야? -> 이것보다 낮은 계층의 디테일 에러부터 먼저 잡기 때문에 걱정 안해도 된다
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> validationError(Exception e) {
        log.error(e.getMessage());
        String errorMessage = e.getMessage();
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorMessage), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<?> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.FORBIDDEN.value(),  e.getMessage()), HttpStatus.FORBIDDEN);
    }
}


