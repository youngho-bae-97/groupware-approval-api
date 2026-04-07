package com.byh.groupware.domain.approval.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApprovalDetailResponseDTO {
    private String docId;
    private String docTitle;
    private String drafterId;
    private String drafterName;
    private String docStatus; // "01": 임시저장, "02": 진행, "03": 완료
    private LocalDateTime draftTime;
    private String content;   // 본문 (SEARCH_CONTENT)
    private String currApprover; // 권한 체크용 현재 결재자 ID
    // 결재선 리스트 (1:N)
    private List<ApproverInfoDTO> approverLines;

    // 버튼 활성화 여부
    private boolean canApprove;
}
