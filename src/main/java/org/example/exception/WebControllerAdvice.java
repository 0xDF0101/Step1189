package org.example.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class WebControllerAdvice {


    // 예외처리 이딴 식으로 거지같이 하는게 아닌 거 같은데
    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFoundException() {
        return "error";
    }

}
