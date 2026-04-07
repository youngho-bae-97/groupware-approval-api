package com.byh.groupware.domain.approval.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StatusMapVO {
    private String docId;
    private String docStatus;
    private Integer currStep;
    private String currApprover;
    private String currApproverName;
    private String currApproverDept;
    private String currApproverDeptId;

    private LocalDateTime updateDate;
    private String docTitle;
    private String drafter;
    private String drafterDept;
}