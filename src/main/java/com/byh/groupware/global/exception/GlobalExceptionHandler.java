package com.byh.groupware.global.exception;

import com.byh.groupware.domain.approval.exception.*;
import com.byh.groupware.domain.user.exception.InvalidLoginException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 1. approval 관련 예외 처리
    @ExceptionHandler(ApprovalNotFoundFormException.class)
    public ResponseEntity<ErrorResponse> handleApprovalNotFound(ApprovalNotFoundFormException e) {

        ErrorResponse response = new ErrorResponse(
                400,
                e.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ApprovalAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleApprovalAccessDenied(ApprovalAccessDeniedException e) {

        ErrorResponse response = new ErrorResponse(
                403,
                e.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(ApprovalInvalidTypeException.class)
    public ResponseEntity<ErrorResponse> handleApprovalInvalidType(ApprovalInvalidTypeException e) {

        ErrorResponse response = new ErrorResponse(
                400,
                e.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(DocStatusMissMatchException.class)
    public ResponseEntity<ErrorResponse> handleDocStatusMissMatch(DocStatusMissMatchException e) {

        ErrorResponse response = new ErrorResponse(
                400,
                e.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MissingNextApproverException.class)
    public ResponseEntity<ErrorResponse> handleMissingNextApprover(MissingNextApproverException e) {

        ErrorResponse response = new ErrorResponse(
                400,
                e.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingApprovalLineException.class)
    public ResponseEntity<ErrorResponse> handleMissingNextApprover(MissingApprovalLineException e) {

        ErrorResponse response = new ErrorResponse(
                400,
                e.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 2. 잘못된 요청
    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(InvalidLoginException e) {

        ErrorResponse response = new ErrorResponse(
                401,
                e.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // 3. 모든 예외 (최종 방어)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {

        ErrorResponse response = new ErrorResponse(
                500,
                "서버 내부 오류 발생"
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
