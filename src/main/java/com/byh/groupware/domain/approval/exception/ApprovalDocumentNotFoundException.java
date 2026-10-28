package com.byh.groupware.domain.approval.exception;

public class ApprovalDocumentNotFoundException extends RuntimeException{
    public ApprovalDocumentNotFoundException(String message){
        super(message);
    }
}
