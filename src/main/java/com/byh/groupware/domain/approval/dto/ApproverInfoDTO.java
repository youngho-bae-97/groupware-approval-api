package com.byh.groupware.domain.approval.dto;

import com.byh.groupware.domain.approval.entity.AprLine;
import com.byh.groupware.domain.approval.entity.EndAprLine;
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

    // 완료 문서 결재선 엔티티용 생성자
    public ApproverInfoDTO(EndAprLine entity) {
        if (entity != null) {
            this.stepSeq = entity.getStepSeq();
            // EndDocumentMaster와의 연관관계가 있다면 getId()로 가져옵니다.
            this.docId = (entity.getEndDocumentMaster() != null) ? entity.getEndDocumentMaster().getId() : null;
            this.approverId = entity.getApproverId();
            this.approverName = entity.getApproverName();
            this.approverJob = entity.getApproverJob();
            this.approveType = entity.getApproveType();
            this.approveStatus = entity.getApproveStatus();
            this.approveReason = entity.getApproveReason();
            this.approveDate = entity.getLastModifiedDate();
            this.approverDeptId = entity.getApproverDeptId();
            this.approverDeptName = entity.getApproverDeptName();
        }
    }

    // 진행 문서 결재선 엔티티용 생성자
    public ApproverInfoDTO(AprLine entity) {
        if (entity != null) {
            this.stepSeq = entity.getStepSeq();
            // DocumentMaster와의 연관관계에서 ID 추출
            this.docId = (entity.getDocumentMaster() != null) ? entity.getDocumentMaster().getId() : null;
            this.approverId = entity.getUserMaster().getLoginId();
            this.approverName = entity.getApproverName();
            this.approverJob = entity.getApproverJob();
            this.approveType = entity.getApproveType();
            this.approveStatus = entity.getApproveStatus();
            this.approveReason = entity.getApproveReason();
            this.approveDate = entity.getLastModifiedDate();
            this.approverDeptId = entity.getApproverDeptId();
            this.approverDeptName = entity.getApproverDeptName();
        }
    }

}
