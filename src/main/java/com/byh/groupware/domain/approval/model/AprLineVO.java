package com.byh.groupware.domain.approval.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AprLineVO {
    private Integer stepSeq;       // STEP_SEQ: 결재 순번 (1: 기안자, 2: 1차결재자...)
    private String docId;          // DOC_ID: 문서 번호
    private String approveType;    // APPROVE_TYPE: 결재 유형 (01:기안, 02:결재, 03:합의 등)
    private String approveStatus;  // APPROVE_STATUS: 결재 상태 (01:대기, 02:승인, 03:반려)
    private String approveReason;  // APPROVE_REASON: 반려 사유
    private LocalDateTime approveDate; // APPROVE_DATE: 결재 처리 일시
    private String approverId;     // APPROVER_ID: 결재자 사번/ID
    private String approverName;   // APPROVER_NAME: 결재자 성명
    private String approverGrade;  // APPROVER_GRADE: 결재자 직급
}
