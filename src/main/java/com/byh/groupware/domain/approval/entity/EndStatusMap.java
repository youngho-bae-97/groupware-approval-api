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
