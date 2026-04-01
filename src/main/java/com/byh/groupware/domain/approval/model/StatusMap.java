package com.byh.groupware.domain.approval.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StatusMap {
    private String docId;
    private String currStatus;
    private Integer currStep;
    private String currApprover;
    private String currApproverName;
    private String currApproverDept;
    private LocalDateTime updateDate;
    private String docTitle;
    private String drafter;
    private String drafterDept;
}