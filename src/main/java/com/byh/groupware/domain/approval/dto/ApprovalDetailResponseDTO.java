package com.byh.groupware.domain.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
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
}
