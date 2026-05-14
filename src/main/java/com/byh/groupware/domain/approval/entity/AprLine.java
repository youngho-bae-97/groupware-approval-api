package com.byh.groupware.domain.approval.entity;

import com.byh.groupware.domain.approval.dto.ApproverInfoDTO;
import com.byh.groupware.domain.user.entity.UserMaster;
import com.byh.groupware.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tbl_aprline")
public class AprLine extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "APRLINE_ID")
    private Long id;

    private Integer stepSeq;       // STEP_SEQ: 결재 순번 (1: 기안자, 2: 1차결재자...)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id")
    private DocumentMaster documentMaster;

    private String approveType;    // APPROVE_TYPE: 결재 유형 (01:기안, 02:결재, 03:합의 등)
    private String approveStatus;  // APPROVE_STATUS: 결재 상태 (01:대기, 02:승인, 03:반려)
    private String approveReason;  // APPROVE_REASON: 반려 사유

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mem_id")
    private UserMaster userMaster;     // APPROVER_ID: 결재자 사번/ID

    private String approverName;   // APPROVER_NAME: 결재자 성명
    private String approverJob;  // APPROVER_Job: 결재자 직급
    private String approverDeptId;  // APPROVER_DEPT: 결재자 부서번호
    private String approverDeptName;  // APPROVER_DEPT: 결재자 부서명

    private AprLine(ApproverInfoDTO lineDto, int firstApproverSeq,UserMaster userMaster){
        this.stepSeq = lineDto.getStepSeq();
        this.userMaster = userMaster;
        this.approverName = lineDto.getApproverName();
        this.approverJob = lineDto.getApproverJob();
        this.approveType = lineDto.getApproveType();
        this.approverDeptId = lineDto.getApproverDeptId();
        this.approverDeptName = lineDto.getApproverDeptName();

        if(lineDto.getStepSeq() == 1){
            this.approveStatus = "03";
        } else if (lineDto.getStepSeq() == firstApproverSeq && lineDto.getApproveType() == "02") {
            this.approveStatus = "02";
        }else{
            this.approveStatus = "01";

        }

    }

    public static AprLine createAprLine(ApproverInfoDTO lineDto, int firstApproverSeq,UserMaster userMaster) {

        return new AprLine(lineDto,firstApproverSeq, userMaster);

    }

    public void confirmMaster(DocumentMaster documentMaster) {
        this.documentMaster = documentMaster;
    }
}
