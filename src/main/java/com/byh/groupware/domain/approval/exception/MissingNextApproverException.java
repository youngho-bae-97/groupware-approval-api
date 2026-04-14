package com.byh.groupware.domain.approval.exception;

public class MissingNextApproverException extends RuntimeException {
    public MissingNextApproverException(String message) {
        super(message);
    }
}
