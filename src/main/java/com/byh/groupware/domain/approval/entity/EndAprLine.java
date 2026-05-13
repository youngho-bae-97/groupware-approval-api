package com.byh.groupware.domain.approval.entity;

import com.byh.groupware.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tbl_endaprline")
public class EndAprLine extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "endaprline_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id")
    private EndDocumentMaster endDocumentMaster;          // DOC_ID: 문서 번호

    private Integer stepSeq;       // STEP_SEQ: 결재 순번 (1: 기안자, 2: 1차결재자...)
    private String approveType;    // APPROVE_TYPE: 결재 유형 (01:기안, 02:결재, 03:합의 등)
    private String approveStatus;  // APPROVE_STATUS: 결재 상태 (01:대기, 02:승인, 03:반려)
    private String approveReason;  // APPROVE_REASON: 반려 사유
    private String approverId;     // APPROVER_ID: 결재자 사번/ID
    private String approverName;   // APPROVER_NAME: 결재자 성명
    private String approverJob;  // APPROVER_JOB: 결재자 직급
    private String approverDeptId;  // APPROVER_DEPT: 결재자 부서번호
    private String approverDeptName;  // APPROVER_DEPT: 결재자 부서명

    public void confirmMaster(EndDocumentMaster master) {
        this.endDocumentMaster = master;
    }
}
