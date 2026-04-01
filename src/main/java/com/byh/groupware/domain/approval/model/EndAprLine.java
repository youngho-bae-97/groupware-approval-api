package com.byh.groupware.domain.approval.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EndAprLine {
    private String docId;
    private int stepSeq;
    private String approverId;
    private String approveType;
    private String approveStatus;
    private String approveReason;
    private LocalDateTime approveDate;
}
