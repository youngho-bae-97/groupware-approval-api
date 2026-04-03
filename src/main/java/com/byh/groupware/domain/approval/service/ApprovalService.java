package com.byh.groupware.domain.approval.service;

import com.byh.groupware.domain.approval.dto.ApprovalDraftRequestDTO;
import com.byh.groupware.domain.approval.model.ActiveDocVO;
import com.byh.groupware.domain.approval.model.DocumentMasterVO;
import com.byh.groupware.domain.user.model.UserMasterVO;

public interface ApprovalService {
        DocumentMasterVO selectDraftDocument();

        String generateDocNo(String formId);

        void draft(ApprovalDraftRequestDTO approvalDraftRequestDTO, UserMasterVO loginUser);

}
