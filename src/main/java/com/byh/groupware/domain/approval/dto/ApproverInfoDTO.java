package com.byh.groupware.domain.approval.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApproverInfoDTO {
    private Integer stepSeq;
    private String docId;
    private String approverId;
    private String approverName; // 성명
    private String approverJob;  // 추가: 직급 (ex: 과장, 차장)
    private String approveType;
    private String approveStatus;
    private String approveReason;
    private LocalDateTime approveDate;
    private String approverDeptId;
    private String approverDeptName;
}
