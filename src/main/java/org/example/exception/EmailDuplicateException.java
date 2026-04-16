package org.example.exception;

import org.springframework.validation.BindingResult;

public class EmailDuplicateException extends RuntimeException {
    public EmailDuplicateException(BindingResult bindingResult) {
        super(bindingResult.getAllErrors().toString());
    }
}
