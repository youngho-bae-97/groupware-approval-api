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
public class DocForm extends BaseEntity implements Persistable<String> {
    @Id
    @Column(name = "approval_form_id")
    private String id;

    private String approvalFormType;
    private String approvalFormTitle;
    private String approvalFormContent;
    private String approvalFormStatus;
    private String approvalFormBookmark;

    @Override
    public String getId(){
        return id;
    }

    @Override
    public boolean isNew() {
        return getCreatedDate() == null;
    }
}
