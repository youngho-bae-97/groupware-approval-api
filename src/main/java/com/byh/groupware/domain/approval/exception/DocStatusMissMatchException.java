package com.byh.groupware.domain.approval.exception;

public class DocStatusMissMatchException extends RuntimeException{
    public DocStatusMissMatchException(String message){
        super(message);
    }
}
