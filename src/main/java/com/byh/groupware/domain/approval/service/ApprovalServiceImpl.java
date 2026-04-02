package com.byh.groupware.domain.approval.service;

import com.byh.groupware.domain.approval.mapper.ApprovalMapper;
import com.byh.groupware.domain.approval.model.DocumentMasterVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {


    private final ApprovalMapper approvalMapper;

    @Override
    public DocumentMasterVO selectDraftDocument() {
        return approvalMapper.selectDraftDocument();
    }
}
