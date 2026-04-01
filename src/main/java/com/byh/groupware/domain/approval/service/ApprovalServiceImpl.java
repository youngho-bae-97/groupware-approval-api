package com.byh.groupware.domain.approval.service;

import com.byh.groupware.domain.approval.mapper.ApprovalMapper;
import com.byh.groupware.domain.approval.model.DocumentMaster;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {


    private final ApprovalMapper approvalMapper;

    @Override
    public DocumentMaster selectDraftDocument() {
        return approvalMapper.selectDraftDocument();
    }
}
