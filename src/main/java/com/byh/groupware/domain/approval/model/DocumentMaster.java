package com.byh.groupware.domain.approval.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentMaster {
    private String docId;              // 문서번호 (PK)
    private LocalDateTime draftTime;    // 기안 일시
    private String drafterDept;        // 기안 당시 부서 (박제)
    private String approvalFormId;     // 결재 양식 ID (FK)
    private String securityLevel;      // 보안 등급 (S, A, B...)
    private String preserveYear;       // 보존 연한
    private String rootDocId;          // 원문서 번호 (재기안/수정기안용)
    private int version;               // 문서 버전 (기본값 0)
    private String drafterId;          // 기안자 사번 (FK)
    private String drafterName;        // 기안자 성명 (박제)
    private String approvalFormTitle;  // 양식 명칭 (박제 - ex: 연차신청서)
}