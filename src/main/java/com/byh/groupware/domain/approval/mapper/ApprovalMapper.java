package com.byh.groupware.domain.approval.mapper;

import com.byh.groupware.domain.approval.dto.ApprovalDraftRequestDTO;
import com.byh.groupware.domain.approval.dto.ApprovalProcessRequestDTO;
import com.byh.groupware.domain.approval.dto.ApproverInfoDTO;
import com.byh.groupware.domain.approval.model.ActiveDocVO;
import com.byh.groupware.domain.approval.model.AprLineVO;
import com.byh.groupware.domain.approval.model.DocumentMasterVO;
import com.byh.groupware.domain.approval.model.StatusMapVO;
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

    void insertAprLines(@Param("list") List<ApproverInfoDTO> aprLines, @Param("docId") String docId, @Param("firstApproverSeq") int firstApproverSeq);

    void insertStatusMap(@Param("dto") ApprovalDraftRequestDTO approvalDraftRequestDTO,
                         @Param("next") ApproverInfoDTO nextApprover,
                         @Param("docStatus") String docStatus);

    boolean isFinalApprover(@Param("docId") String docId, @Param("processorId") String processorId);

    StatusMapVO selectStatusForUpdate(String docId);

    //AprLineVO selectNextApprover(ApprovalProcessRequestDTO dto, boolean isFinal);
    // Mapper Interface
    AprLineVO selectNextApprover(@Param("dto") ApprovalProcessRequestDTO dto, @Param("isFinal") boolean isFinal);

    void updateStatusMap(@Param("dto") ApprovalProcessRequestDTO dto, @Param("docStatus") String docStatus);

    void updateActiveDoc(String docId, boolean isFinal);

    void updateAprLine(@Param("dto") ApprovalProcessRequestDTO dto, @Param("isFinal") boolean isFinal);

    void updateCurrentApproverStatus(ApprovalProcessRequestDTO dto);

    void updateNextApproverStatus(String docId, Integer stepSeq);

    void insertEndDoc(String docId);

    void insertEndAprLine(String docId);

    void deleteActiveDoc(String docId);

    void deleteAprLine(String docId);

    int checkApproverAuthority(String docId, String processorId);
}
