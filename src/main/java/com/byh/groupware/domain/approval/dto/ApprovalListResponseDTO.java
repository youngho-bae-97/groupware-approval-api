package com.byh.groupware.domain.approval.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApprovalListResponseDTO {
    String docId;
    String docTitle;
//    String drafterName;
    String currApproverName;
    String docStatus;
    LocalDateTime updateDate;
}
