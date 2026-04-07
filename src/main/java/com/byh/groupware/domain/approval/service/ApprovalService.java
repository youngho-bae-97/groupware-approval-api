package com.byh.groupware.domain.approval.service;

import com.byh.groupware.domain.approval.dto.*;
import com.byh.groupware.domain.approval.model.ActiveDocVO;
import com.byh.groupware.domain.approval.model.DocumentMasterVO;
import com.byh.groupware.domain.user.model.UserMasterVO;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface ApprovalService {
        DocumentMasterVO selectDraftDocument();

        String generateDocNo(String formId);

        void draft(ApprovalDraftRequestDTO approvalDraftRequestDTO, UserMasterVO loginUser);

        void doProcess(ApprovalProcessRequestDTO approvalProcessRequestDTO, UserMasterVO loginUser);

        List<ApprovalListResponseDTO> getApprovalList(ApprovalSearchDTO dto, UserMasterVO loginUser);

    ApprovalDetailResponseDTO getApprovalDetail(String docId, String docStatus, String memId) throws AccessDeniedException;
}
