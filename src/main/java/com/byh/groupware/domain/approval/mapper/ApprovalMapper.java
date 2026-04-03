package com.byh.groupware.domain.approval.mapper;

import com.byh.groupware.domain.approval.dto.ApprovalDraftRequestDTO;
import com.byh.groupware.domain.approval.dto.ApproverInfoDTO;
import com.byh.groupware.domain.approval.model.ActiveDocVO;
import com.byh.groupware.domain.approval.model.DocumentMasterVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApprovalMapper {
    DocumentMasterVO selectDraftDocument();

    Integer generateDocNo(String year, String formId);

    void updateLastSeq(String year, String formId);

    void draft(DocumentMasterVO masterVO, ActiveDocVO docVO);

    void insertDocumentMaster(ApprovalDraftRequestDTO approvalDraftRequestDTO);

    void insertActiveDoc(ApprovalDraftRequestDTO approvalDraftRequestDTO);

    void insertAprLines(@Param("list") List<ApproverInfoDTO> aprLines, @Param("docId") String docId);

    void insertStatusMap(@Param("dto") ApprovalDraftRequestDTO approvalDraftRequestDTO,
                         @Param("next") ApproverInfoDTO nextApprover,
                         @Param("docStatus") String docStatus);
}
