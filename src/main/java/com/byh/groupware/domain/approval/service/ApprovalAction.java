package com.byh.groupware.domain.approval.service;

import com.byh.groupware.domain.approval.dto.ApprovalProcessRequestDTO;
import com.byh.groupware.domain.approval.type.ApprovalType;

public interface ApprovalAction {

    ApprovalType getActionType();

    void doProcess(ApprovalProcessRequestDTO dto);
}
