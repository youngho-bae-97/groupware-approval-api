package com.byh.groupware.domain.approval.exception;

public class MissingApprovalLineException extends RuntimeException {
    public MissingApprovalLineException(String message) {
        super(message);
    }
}
