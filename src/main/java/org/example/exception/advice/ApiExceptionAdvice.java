package org.example.exception.advice;

import lombok.extern.slf4j.Slf4j;
import org.example.exception.EmailDuplicateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionAdvice {

    @ExceptionHandler(EmailDuplicateException.class)
    public ResponseEntity<Map<String, String>> handleEmailDuplicate(EmailDuplicateException e) {
        // TODO 이런식으로 하지 말고 ErrorResponseDTO 하나 만들어도 좋다!
        Map<String, String> error = new HashMap<>();
        error.put("field", "email");
        error.put("message", e.getMessage());

        // 400이나 409 같은 에러 상태 코드와 함께 JSON 반환!
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }


}
