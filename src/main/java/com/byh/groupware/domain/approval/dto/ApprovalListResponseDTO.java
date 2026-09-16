package com.byh.groupware.domain.approval.dto;

import com.byh.groupware.domain.approval.entity.ActiveDoc;
import com.byh.groupware.domain.approval.entity.AprLine;
//import com.byh.groupware.domain.approval.entity.StatusMap;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ApprovalListResponseDTO {
    Long id;
    String docTitle;
//    String drafterName;
//    String currApproverName;
    List<ApproverInfoDTO> aprLines = new ArrayList<>();
    String docStatus;
    LocalDateTime updateDate;


    public ApprovalListResponseDTO(ActiveDoc activeDoc){
        this.id = activeDoc.getId();
        this.docTitle = activeDoc.getDocTitle();


        for(AprLine aprLine : activeDoc.getAprLines()){
            aprLines.add(new ApproverInfoDTO(aprLine));
        }

//        this.drafterName = activeDoc.getDrafter();
//        this.currApproverName = activeDoc.getCurrApproverName();


        this.docStatus = activeDoc.getDocStatus();
        this.updateDate = activeDoc.getLastModifiedDate();


    }
}
