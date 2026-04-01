package com.byh.groupware.domain.approval.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoMaster {
    private String year;            // 채번 연도 (예: 2026)
    private String approvalFormId;  // 양식 ID (예: LEAVE_01)
    private Integer lastSeq;        // 마지막 발행 번호
    private LocalDateTime updateDate;
}
