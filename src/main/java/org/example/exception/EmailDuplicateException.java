package org.example.exception;


public class EmailDuplicateException extends RuntimeException {
    public EmailDuplicateException(String msg) {
        super(msg);
    }
}
