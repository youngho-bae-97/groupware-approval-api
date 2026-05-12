package com.byh.groupware.domain.approval.service;

import com.byh.groupware.domain.approval.dto.*;
import com.byh.groupware.domain.approval.entity.DocumentMaster;
import com.byh.groupware.domain.user.entity.UserMaster;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface ApprovalService {
        DocumentMaster selectDraftDocument();

        String generateDocNo(String formId);

        void draft(ApprovalDraftRequestDTO approvalDraftRequestDTO, UserMaster loginUser);

        void doProcess(ApprovalProcessRequestDTO approvalProcessRequestDTO, UserMaster loginUser);

        List<ApprovalListResponseDTO> getApprovalList(ApprovalSearchDTO dto, UserMaster loginUser);

    ApprovalDetailResponseDTO getApprovalDetail(String docId, String docStatus, String memId) throws AccessDeniedException;
}
