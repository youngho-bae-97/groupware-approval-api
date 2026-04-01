package com.byh.groupware.domain.approval.model;

import lombok.Data;

@Data
public class DocForm {
    private String approvalFormId;
    private String approvalFormType;
    private String approvalFormTitle;
    private String approvalFormContent;
    private String approvalFormStatus;
    private String approvalFormBookmark;
}
