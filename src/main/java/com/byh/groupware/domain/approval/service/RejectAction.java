package com.byh.groupware.domain.approval.service;

import com.byh.groupware.domain.approval.dto.ApprovalProcessRequestDTO;
import com.byh.groupware.domain.approval.mapper.ApprovalMapper;
import com.byh.groupware.domain.approval.type.ApprovalType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RejectAction implements ApprovalAction {

    private final ApprovalMapper approvalMapper;

    @Override
    public ApprovalType getActionType() {
        return ApprovalType.REJECT;
    }
    // 반려처리 시의 로직
    @Override
    public void doProcess(ApprovalProcessRequestDTO dto) {

    }
}
