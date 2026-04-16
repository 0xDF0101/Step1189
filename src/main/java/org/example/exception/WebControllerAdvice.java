package org.example.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class WebControllerAdvice {

    // 예외처리 이딴 식으로 거지같이 하는게 아닌 거 같은데
    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFoundException() {
        return "error";
    }

    // 얘도
    @ExceptionHandler(EmailNotFoundException.class)
    public String handleEmailNotFoundException() {
        return "error";
    }

    // 얘도
    @ExceptionHandler(EmailDuplicateException.class)
    public ResponseEntity<Map<String, String>> handleEmailDuplicate(EmailDuplicateException e) {
        Map<String, String> error = new HashMap<>();
        error.put("field", "email");
        error.put("message", "이미 존재하는 이메일입니다.");

        // 400이나 409 같은 에러 상태 코드와 함께 JSON 반환!
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }



}
