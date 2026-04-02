package com.byh.groupware.domain.approval.dto;

import lombok.Data;

@Data
public class ApproverInfoDTO {
    private int stepSeq;          // 결재 순서 (1, 2, 3...)
    private String approverId;    // 결재자 ID
    private String approveType;   // 결재 타입 (01:결재, 02:합의 등)
}
