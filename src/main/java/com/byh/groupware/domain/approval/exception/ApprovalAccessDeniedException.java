package com.byh.groupware.domain.approval.exception;

public class ApprovalAccessDeniedException extends RuntimeException {
    public ApprovalAccessDeniedException(String message) {
        super(message);
    }
}
