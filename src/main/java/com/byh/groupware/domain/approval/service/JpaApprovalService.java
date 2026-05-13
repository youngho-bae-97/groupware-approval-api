package com.byh.groupware.domain.approval.service;

import com.byh.groupware.domain.approval.dto.*;
import com.byh.groupware.domain.approval.entity.*;
import com.byh.groupware.domain.approval.repository.*;
import com.byh.groupware.domain.user.entity.UserMaster;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JpaApprovalService {

    private final StatusMapRepository statusMapRepository;

    private final DocumentMasterRepository documentMasterRepository;

    private final EndDocumentMasterRepository endDocumentMasterRepository;

    public void draft(@Valid ApprovalDraftRequestDTO approvalDraftRequestDTO, UserMaster loginUser) {

    }

    public void doProcess(@Valid ApprovalProcessRequestDTO approvalProcessRequestDTO, UserMaster loginUser) {
    }

    public Page<StatusMap> getApprovalList(ApprovalSearchDTO dto, UserMaster loginUser, Pageable pageable) {
        dto.setLoginMemId(loginUser.getLoginId());
        //dto.setLoginDeptCode(loginUser.getDeptCode());

        Page<StatusMap> statusMaps = statusMapRepository.docToDoList(pageable,loginUser.getLoginId());

        return statusMaps;
//        return approvalMapper.selectApprovalList(dto);


    }

    public ApprovalDetailResponseDTO getApprovalDetail(String docId, String docStatus, String loginId) {

        ApprovalDetailResponseDTO detail;

        if("03".equals(docStatus)){
            EndDocumentMaster endDocmaster = endDocumentMasterRepository.findWithDetailsById(docId)
                    .orElseThrow(() -> new EntityNotFoundException("해당 문서를 찾을 수 없습니다. ID: " + docId));
            detail = new ApprovalDetailResponseDTO(endDocmaster,loginId);

        } else {
            DocumentMaster docMaster = documentMasterRepository.findWithDetailsById(docId)
                    .orElseThrow(() -> new EntityNotFoundException("해당 문서를 찾을 수 없습니다. ID: " + docId));
            detail = new ApprovalDetailResponseDTO(docMaster,loginId);
        }

        boolean isParticipant = detail.getApproverLines().stream()
                .anyMatch(line -> loginId.equals(line.getApproverId()));

        boolean isDrafter = loginId.equals(detail.getDrafterId());

        // 문서 열람 권한 체크
        if (!isDrafter && !isParticipant) {
            throw new AccessDeniedException("이 문서를 열람할 권한이 없습니다.");
        }

        // 결재 버튼 활성화 여부 판단
        if (detail != null && !"03".equals(detail.getDocStatus())) {
            // 진행 중인 문서일 경우, 내 차례(CURR_APPROVER)인 경우만 Approve가 가능하도록 세팅
            detail.setCanApprove(loginId.equals(detail.getCurrApprover()));
        }

        return detail;
    }
}
