package com.byh.groupware.domain.approval.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApprovalDraftRequestDTO {
    // 1. 문서 마스터 관련 정보
    private String drafterId;         // 기안자 사번 (EMP001)
    private String approvalFormId;    // 양식 ID (FORM-01)
    private String preserveYear;      // 보존 연한 (5년 등)
    private String securityLevel;     // 보안 등급 (A, B, C)

    // 2. 문서 상세 내용 관련 (ActiveDoc용)
    private String docTitle;          // 문서 제목
    private String docContent;        // 실제 HTML 본문 내용
    private String urgentYn;          // 긴급 여부 (Y/N)
    private String attachYn;          // 첨부파일 여부 (Y/N)

    // 3. 결재선 정보 (동적 리스트)
    // 결재선 테이블(TBL_APRLINE)에 순서대로 들어갈 정보들입니다.
    private List<ApproverInfoDTO> approvalLines;
}
