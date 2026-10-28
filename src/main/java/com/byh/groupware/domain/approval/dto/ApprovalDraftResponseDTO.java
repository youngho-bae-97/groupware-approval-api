package com.byh.groupware.domain.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "기안 응답 정보")
public class ApprovalDraftResponseDTO {
    @Schema(description = "문서 마스터 ID (API 문서 식별자)", example = "1")
    private Long id;
    @Schema(description = "채번된 문서 번호", example = "2026FORM-0100001")
    private String docId;
}
