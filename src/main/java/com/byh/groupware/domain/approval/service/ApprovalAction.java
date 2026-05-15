package com.byh.groupware.domain.approval.service;

import com.byh.groupware.domain.approval.dto.ApprovalProcessRequestDTO;
import com.byh.groupware.domain.approval.type.ApprovalType;
import com.byh.groupware.domain.user.entity.UserMaster;

public interface ApprovalAction {

    ApprovalType getActionType();

    void doProcess(ApprovalProcessRequestDTO dto, UserMaster userMaster);
}
