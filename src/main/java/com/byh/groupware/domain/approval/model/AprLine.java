package com.byh.groupware.domain.approval.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AprLine {
    private int stepSeq;
    private String docId;
    private String approveType;
    private String approveStatus;
    private String approveReason;
    private LocalDateTime approveDate;
    private String approverId;
}
