package com.byh.groupware.global.exception;

import com.byh.groupware.domain.approval.exception.*;
import com.byh.groupware.domain.user.exception.InvalidLoginException;
import com.byh.groupware.domain.user.exception.MissingLoginUserException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
    // 문서가 결재할 수 없는 상태(이미 처리됨 등) -> 현재 리소스 상태와 충돌
    @ExceptionHandler(DocStatusMissMatchException.class)
    public ResponseEntity<ErrorResponse> handleDocStatusMissMatch(DocStatusMissMatchException e) {

        ErrorResponse response = new ErrorResponse(
                409,
                e.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(ApprovalDocumentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleApprovalDocumentNotFound(ApprovalDocumentNotFoundException e) {

        ErrorResponse response = new ErrorResponse(
                404,
                e.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
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

    // 일반결재 동시 처리 충돌 (낙관적 락)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(ObjectOptimisticLockingFailureException e) {

        ErrorResponse response = new ErrorResponse(
                409,
                "다른 사용자가 먼저 처리한 문서입니다. 새로고침 후 다시 시도해주세요."
        );

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ConstraintViolationException e) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {

        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .get(0)
                .getDefaultMessage();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, errorMessage));
    }

    // 로그인 관련

    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(InvalidLoginException e) {

        ErrorResponse response = new ErrorResponse(
                401,
                e.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(MissingLoginUserException.class)
    public ResponseEntity<ErrorResponse> handleMissingLoginUser(MissingLoginUserException e) {

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
