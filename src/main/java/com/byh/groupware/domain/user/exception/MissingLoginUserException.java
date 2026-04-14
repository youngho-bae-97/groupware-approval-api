package com.byh.groupware.domain.user.exception;

public class MissingLoginUserException extends RuntimeException {
    public MissingLoginUserException(String message) {
        super(message);
    }
}
