package com.byh.groupware.domain.approval.entity;

import com.byh.groupware.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tbl_docform")
public class DocForm extends BaseEntity {
    @Id @GeneratedValue
    @Column(name = "docform_id")
    private Long id;

    @Column(unique = true)
    private String approvalFormId;

    private String approvalFormType;
    private String approvalFormTitle;
    private String approvalFormContent;
    private String approvalFormStatus;
    private String approvalFormBookmark;

    public DocForm(String approvalFormId, String approvalFormTitle, String approvalFormStatus){
        this.approvalFormId = approvalFormId;
        this.approvalFormTitle = approvalFormTitle;
        this.approvalFormStatus = approvalFormStatus;
    }

//    @Override
//    public String getId(){
//        return id;
//    }
//
//    @Override
//    public boolean isNew() {
//        return getCreatedDate() == null;
//    }
}
