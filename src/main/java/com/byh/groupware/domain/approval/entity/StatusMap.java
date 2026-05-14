package com.byh.groupware.domain.approval.entity;

import com.byh.groupware.domain.approval.dto.ApprovalDraftRequestDTO;
import com.byh.groupware.domain.approval.dto.ApproverInfoDTO;
import com.byh.groupware.domain.approval.exception.MissingNextApproverException;
import com.byh.groupware.domain.user.entity.UserMaster;
import com.byh.groupware.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import javax.print.Doc;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tbl_status_map")
public class StatusMap extends BaseEntity implements Persistable<String> {

    @Id
    @Column(name = "doc_id")
    private String id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id")
    private DocumentMaster documentMaster;

    private String docStatus;
    private Integer currStep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mem_id")
    private UserMaster userMaster;

    private String currApproverName;
    private String currApproverDept;
    private String currApproverDeptId;

    private String docTitle;
    private String drafter;
    private String drafterDept;


    private StatusMap(ApprovalDraftRequestDTO approvalDraftRequestDTO, UserMaster nextMember,ApproverInfoDTO approverInfoDTO, String docStatus){
        this.docStatus = docStatus;
        this.currStep = approverInfoDTO.getStepSeq();
        this.userMaster = nextMember;
        this.docTitle = approvalDraftRequestDTO.getDocTitle();
        this.drafter = approvalDraftRequestDTO.getDrafterId();
        this.drafterDept = approvalDraftRequestDTO.getDrafterDept();
        this.currApproverName = approverInfoDTO.getApproverName();
        this.currApproverDept = approverInfoDTO.getApproverDeptName();
        this.currApproverDeptId = approverInfoDTO.getApproverDeptId();


    }

    public static StatusMap createStatusMap(ApprovalDraftRequestDTO approvalDraftRequestDTO,UserMaster nextMember, ApproverInfoDTO approverInfoDTO, String docStatus) {


        return new StatusMap(approvalDraftRequestDTO,nextMember, approverInfoDTO, docStatus);
    }

    public void confirmMaster(DocumentMaster master){
        this.documentMaster = master;
        this.id = master.getId();
    }

    @Override
    public String getId(){
        return id;
    }

    @Override
    public boolean isNew() {
        return getCreatedDate() == null;
    }
}