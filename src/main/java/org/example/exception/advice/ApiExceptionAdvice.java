package org.example.exception.advice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.common.ErrorResponseDto;
import org.example.exception.EmailDuplicateException;
import org.example.exception.UsernameDuplicateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JS에서 응답을 받을때 무조건 리스트로 받는다!
 * 고로 ErrorResponseDto는 List에 담아서 반환하도록 한다.
 */

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionAdvice {

    /**
     * 회원가입시, email이나, username이 중복된 경우 예외 처리
     */
    @ExceptionHandler({EmailDuplicateException.class, UsernameDuplicateException.class})
    public ResponseEntity<List<ErrorResponseDto>> handleDuplicateExceptions(RuntimeException e) {
        String field = (e instanceof EmailDuplicateException) ? "email" : "username";

        // List.of()를 사용해서 딱 하나의 에러만 담긴 리스트 생성
        List<ErrorResponseDto> errors = List.of(new ErrorResponseDto(field, e.getMessage()));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errors);
    }

    /**
     * @Valid에 의해서 검증하다 예외가 터지면 얘가 처리함
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErrorResponseDto>> handleValidationExceptions(MethodArgumentNotValidException e) {
        List<ErrorResponseDto> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorResponseDto(error.getField(), error.getDefaultMessage()))
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * @Valided에 의해서 파라미터만 검증하다 터지는 예외 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<List<ErrorResponseDto>> handleConstraintViolationException(ConstraintViolationException e) {

        List<ErrorResponseDto> errors = e.getConstraintViolations().stream()
                .map(violation -> {
                    String propertyPath = violation.getPropertyPath().toString();
                    String fieldName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
                    return new ErrorResponseDto(fieldName, violation.getMessage());
                })
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
