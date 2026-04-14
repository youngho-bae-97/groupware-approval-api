package com.byh.groupware.domain.approval.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ApprovalDraftRequestDTO {
    // 1. 문서 마스터 관련 정보
    @NotBlank(message = "문서번호는 필수입니다.")
    private String docId;
    private String drafterId;         // 기안자 사번 (EMP001)
    private String drafterName;       // 기안자 이름
    private String drafterDept;       // 기안자 소속부서
    private String jobGrade;          // 기안자 직급

    private String approvalFormId;    // 양식 ID (FORM-01)
    private String approvalFormTitle;
    private String preserveYear;      // 보존 연한 (5년 등)
    private String securityLevel;     // 보안 등급 (A, B, C)
    private String requestFlag;
    private String rootDocId;
    private Integer version;
//    private String currStatus;
//    private Integer currStep;
//    private String currApproverId;

    // 2. 문서 상세 내용 관련 (ActiveDoc용)
    @NotBlank(message = "제목은 필수입니다.")
    private String docTitle;          // 문서 제목
    private String docContent;        // 실제 HTML 본문 내용
    private String urgentYn;          // 긴급 여부 (Y/N)
    private String attachYn;          // 첨부파일 여부 (Y/N)
    private String filePath;
    private String fileName;
    private String contentType;

    // 3. 문서상태관리테이블 정보들


    // 4. 결재선 정보 (동적 리스트)
    // 결재선 테이블(TBL_APRLINE)에 순서대로 들어갈 정보들입니다.
    @NotEmpty(message = "결재선을 지정해주세요.")
    private List<ApproverInfoDTO> approvalLines;
}
