package org.example.exception.advice;

import lombok.extern.slf4j.Slf4j;
import org.example.exception.EmailNotFoundException;
import org.example.exception.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice(annotations = Controller.class)
public class ViewExceptionAdvice {

    @ExceptionHandler({EntityNotFoundException.class, EmailNotFoundException.class})
    public String handleNotFoundException(RuntimeException e, Model model) {
        log.error("Not found Exception : {}", e.getMessage(), e);
        model.addAttribute("exception", e.getMessage());
        return "error";
    }
}
