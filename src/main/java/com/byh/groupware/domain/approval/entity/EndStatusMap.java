package com.byh.groupware.domain.approval.entity;

import com.byh.groupware.domain.user.entity.UserMaster;
import com.byh.groupware.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tbl_end_status_map")
public class EndStatusMap extends BaseEntity implements Persistable<String> {
    @Id
    @Column(name = "doc_id")
    private String id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id")
    private EndDocumentMaster endDocumentMaster;

    private String docStatus;
    private Integer currStep;


    private String currApproverId;

    private String currApproverName;
    private String currApproverDept;
    private String currApproverDeptId;

    private String docTitle;
    private String drafter;
    private String drafterDept;

    private EndStatusMap(StatusMap statusMap){
        this.docStatus = statusMap.getDocStatus();
        this.currStep = statusMap.getCurrStep();
        this.currApproverId = statusMap.getUserMaster().getLoginId();
        this.docTitle = statusMap.getDocTitle();
        this.drafter = statusMap.getDrafter();
        this.drafterDept = statusMap.getDrafterDept();
        this.currApproverName = statusMap.getCurrApproverName();
        this.currApproverDept = statusMap.getCurrApproverDept();
        this.currApproverDeptId = statusMap.getCurrApproverDeptId();
    }

    public static EndStatusMap createEndStatusMap(StatusMap statusMap) {

        return new EndStatusMap(statusMap);
    }

    public void confirmMaster(EndDocumentMaster master){
        this.endDocumentMaster = master;
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
