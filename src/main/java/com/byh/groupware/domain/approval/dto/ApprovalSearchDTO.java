package com.byh.groupware.domain.approval.dto;

import lombok.Data;

@Data
public class ApprovalSearchDTO {
    private String loginMemId;   // 로그인한 사용자 ID
    private String loginDeptCode; // 로그인한 사용자의 부서 코드
    private String viewType;     // 조회 타입: TODO(결재대기), DRAFT(기안함), DEPT(부서함)
    private String docStatus;    // 문서 상태 (01:임시저장, 02:진행중, 03:완료)
    private String searchKeyword; // 제목 검색어
}
