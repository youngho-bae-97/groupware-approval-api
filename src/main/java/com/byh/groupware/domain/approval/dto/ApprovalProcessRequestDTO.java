package com.byh.groupware.domain.approval.dto;

import com.byh.groupware.domain.approval.entity.AprLine;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApprovalProcessRequestDTO {
    // 문서 식별자와 결재 유형은 URL(/api/approvals/{documentMasterId}/approve)로 받아 서버에서 세팅
    @Schema(hidden = true)
    private Long docMasterId;
    @Schema(hidden = true)
    private Long activeDocId;
    @Schema(hidden = true)
    private String processorId;         // 결재자 사번
    private String processorName;       // 결재자 이름
    private String processorDept;       // 결재자 소속부서
    private String processorJobGrade;          // 결재자 직급
    @Schema(hidden = true)
    private String approveType;
    // 결재 시 본문 수정 (선택, 비어 있으면 기존 값 유지)
    private String docTitle;          // 문서 제목
    private String docContent;        // 실제 HTML 본문 내용
    private String urgentYn;          // 긴급 여부 (Y/N)
    private String attachYn;          // 첨부파일 여부 (Y/N)
    private String filePath;
    private String fileName;
    private String contentType;

    // 상세정보로 조회 후 해당문서에 대한 결재선 정보를 받았다고 가정
    @Schema(hidden = true)
    private AprLine currApprover;

}
