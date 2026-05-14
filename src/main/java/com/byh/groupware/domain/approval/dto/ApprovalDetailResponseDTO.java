package com.byh.groupware.domain.approval.dto;

import com.byh.groupware.domain.approval.entity.DocumentMaster;
import com.byh.groupware.domain.approval.entity.EndDocumentMaster;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@Schema(description = "결재 상세 응답 정보")
public class ApprovalDetailResponseDTO {
    @Schema(description = "문서 번호", example = "2026FORM-0100001")
    private String docId;
    @Schema(description = "문서 제목", example = "2026년 상반기 휴가 신청")
    private String docTitle;
    @Schema(description = "최초 기안자 사번")
    private String drafterId;
    @Schema(description = "최초 기안자 이름")
    private String drafterName;
    @Schema(description = "문서 상태", allowableValues = {"01", "02", "03"})
    private String docStatus; // "01": 임시저장, "02": 진행, "03": 완료
    @Schema(description = "최초 기안일자", example="2026-04-06 20:49:26")
    private LocalDateTime draftTime;
    @Schema(description = "문서 본문(검색을 위한 데이터)")
    private String content;   // 본문 (SEARCH_CONTENT)
    @Schema(description = "현재 결재처리할 사람의 사번")
    private String currApprover; // 권한 체크용 현재 결재자 ID
    // 결재선 리스트 (1:N)
    @Schema(description = "결재선 리스트")
    private List<ApproverInfoDTO> approverLines;

    // 버튼 활성화 여부
    @Schema(description = "진행문서에서 결재버튼 활성화 여부")
    private boolean canApprove;

    public ApprovalDetailResponseDTO(EndDocumentMaster entity, String currentUserId) {
        // 1. 기본 마스터 정보 (EndDocumentMaster)
        this.docId = entity.getId();
        this.drafterId = entity.getMemId();
        this.drafterName = entity.getDrafterName();
        this.draftTime = entity.getCreatedDate(); // BaseEntity의 생성일자 활용

        // 2. 상태 정보 (EndStatusMap - OneToOne)
        if (entity.getEndStatusMap() != null) {
            this.docTitle = entity.getEndStatusMap().getDocTitle();
            this.docStatus = entity.getEndStatusMap().getDocStatus();
            this.currApprover = entity.getEndStatusMap().getCurrApproverId();
        }

        // 3. 본문 정보 (EndDoc - OneToOne)
        if (entity.getEndDoc() != null) {
            this.content = entity.getEndDoc().getSearchContent();
        }

        // 4. 결재선 정보 (AprLines - OneToMany)
        this.approverLines = entity.getEndAprLines().stream()
                .map(ApproverInfoDTO::new) // ApproverInfoDTO에도 엔티티를 받는 생성자가 있다고 가정
                .toList();

        // 5. 버튼 활성화 여부 로직 (현재 로그인한 사용자와 현재 결재자 비교)
        this.canApprove = currentUserId != null && currentUserId.equals(this.currApprover);
    }

    public ApprovalDetailResponseDTO(DocumentMaster entity, String currentUserId) {
        // 1. 기본 마스터 정보 (DocumentMaster)
        this.docId = entity.getId();
        this.drafterId = entity.getUserMaster().getLoginId();
        this.drafterName = entity.getDrafterName();
        this.draftTime = entity.getCreatedDate(); // BaseEntity의 생성일자 활용

        // 2. 상태 정보 (StatusMap - OneToOne)
        if (entity.getStatusMap() != null) {
            this.docTitle = entity.getStatusMap().getDocTitle();
            this.docStatus = entity.getStatusMap().getDocStatus();
            this.currApprover = entity.getStatusMap().getUserMaster().getLoginId();
        }

        // 3. 본문 정보 (ActiveDoc - OneToOne)
        if (entity.getActiveDoc() != null) {
            this.content = entity.getActiveDoc().getSearchContent();
        }

        // 4. 결재선 정보 (AprLines - OneToMany)
        this.approverLines = entity.getAprLines().stream()
                .map(ApproverInfoDTO::new) // ApproverInfoDTO에도 엔티티를 받는 생성자가 있다고 가정
                .toList();

        // 5. 버튼 활성화 여부 로직 (현재 로그인한 사용자와 현재 결재자 비교)
        this.canApprove = currentUserId != null && currentUserId.equals(this.currApprover);
    }
}
