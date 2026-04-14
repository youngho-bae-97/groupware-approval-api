package com.byh.groupware.domain.user.exception;

public class InvalidLoginException extends RuntimeException{
    public InvalidLoginException(String message) {
        super(message);
    }
}
