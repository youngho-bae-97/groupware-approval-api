package com.byh.groupware.domain.approval.dto;

import com.byh.groupware.domain.approval.entity.StatusMap;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ApprovalListResponseDTO {
    String docId;
    String docTitle;
    String drafterName;
    String currApproverName;
    String docStatus;
    LocalDateTime updateDate;


    public ApprovalListResponseDTO(StatusMap statusMap){
        this.docId = statusMap.getId();
        this.docTitle = statusMap.getDocTitle();
        this.drafterName = statusMap.getDrafter();
        this.currApproverName = statusMap.getCurrApproverName();
        this.docStatus = statusMap.getDocStatus();
        this.updateDate = statusMap.getLastModifiedDate();


    }
}
