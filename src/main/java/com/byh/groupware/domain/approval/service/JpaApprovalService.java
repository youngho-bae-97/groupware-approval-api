package com.byh.groupware.domain.approval.service;

import com.byh.groupware.domain.approval.dto.*;
import com.byh.groupware.domain.approval.entity.*;
import com.byh.groupware.domain.approval.exception.ApprovalInvalidTypeException;
import com.byh.groupware.domain.approval.exception.MissingApprovalLineException;
import com.byh.groupware.domain.approval.exception.MissingNextApproverException;
import com.byh.groupware.domain.approval.repository.*;
import com.byh.groupware.domain.approval.type.ApprovalType;
import com.byh.groupware.domain.user.entity.UserMaster;
import com.byh.groupware.domain.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
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
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.swing.text.Document;
import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JpaApprovalService {

    private final StatusMapRepository statusMapRepository;

    private final DocumentMasterRepository documentMasterRepository;

    private final EndDocumentMasterRepository endDocumentMasterRepository;

    private final DocFormRepository docFormRepository;

    private final NoMasterRepository noMasterRepository;

    private final UserRepository userRepository;

    private final ActiveDocRepository activeDocRepository;

    private final List<ApprovalAction> actions;

    private final Map<ApprovalType, ApprovalAction> actionMap = new EnumMap<>(ApprovalType.class);

    @PostConstruct
    public void init() {
        for (ApprovalAction action : actions) {
            actionMap.put(action.getActionType(), action);
        }

    }

    @Transactional
    public void draft(ApprovalDraftRequestDTO approvalDraftRequestDTO, UserMaster loginUser) {

        loginUser = userRepository.findByLoginId(loginUser.getLoginId());

        Optional<DocForm> docForm = docFormRepository.findById(approvalDraftRequestDTO.getApprovalFormId());

        String currentYear = String.valueOf(LocalDate.now().getYear());

        Long exist = noMasterRepository.countById(new NoMasterId(currentYear, docForm.get().getId()));

        // 새로운 채번의 경우 연도와 양식명으로 번호가0인 새로운 레코드를 추가해야됨
        NoMaster noMaster;
        if(exist == 0){
             ;
            noMasterRepository.save(NoMaster.createDocNo(currentYear, docForm.get()));
            noMaster = noMasterRepository.findLockById(new NoMasterId(currentYear, docForm.get().getId()));
        }else{
             noMaster = noMasterRepository.findLockById(new NoMasterId(currentYear, docForm.get().getId()));
        }


        Integer updatedSeq = noMaster.updateSeq();

        String newDocNo = currentYear + docForm.get().getId() + String.format("%05d", updatedSeq);

//        approvalDraftRequestDTO.setDrafterId(loginUser.getLoginId());
//        approvalDraftRequestDTO.setDrafterName(loginUser.getMemName());
//        approvalDraftRequestDTO.setDrafterDept(loginUser.getDeptName());

        // 채번된 문서번호 세팅
        approvalDraftRequestDTO.setDocId(newDocNo);


       // 문서 마스터 관련 처리 시작=========================
        if(ObjectUtils.isEmpty(approvalDraftRequestDTO.getVersion())){
            approvalDraftRequestDTO.setVersion(1);
        }

        // 재기안 문서인지 확인 (재기안 문서일 경우 프론트로부터 "REDRAFT"를 전달받고, ROOTDOCID를 전달받는 것으로 가정)
        if("REDRAFT".equals(approvalDraftRequestDTO.getRequestFlag()) && !ObjectUtils.isEmpty(approvalDraftRequestDTO.getRootDocId())){
            approvalDraftRequestDTO.setVersion(approvalDraftRequestDTO.getVersion()+1);

        }else{
            // 재기안 문서가 아닐 경우 ROOT DOC ID는 자기자신의 문서번호로 설정(이렇게 설정해야 이후 재기안문서의 히스토리 추적시 복잡한 재귀쿼리 없이 조회 가능)
            approvalDraftRequestDTO.setRootDocId(approvalDraftRequestDTO.getDocId());
        }

        DocumentMaster documentMaster = DocumentMaster.createDocumentMaster(approvalDraftRequestDTO, loginUser,docForm.get());
        // 문서 마스터 관련 처리 끝=========================

        // 진행문서 생성 시작 =======================
        String plainText = refineDocContent(approvalDraftRequestDTO.getDocContent());

        approvalDraftRequestDTO.setDocContent(plainText);

        // 긴급 여부가 비어있으면 기본값 'N'
        if (!StringUtils.hasText(approvalDraftRequestDTO.getUrgentYn())) {
            approvalDraftRequestDTO.setUrgentYn("N");
        }

        ActiveDoc activeDoc = ActiveDoc.createActiveDoc(approvalDraftRequestDTO);
        documentMaster.addActiveDoc(activeDoc);
        // 진행문서 생성 끝 =======================

        // ====문서상태관리 엔티티 관련 처리 시작 ======================

        List<ApproverInfoDTO> aprLines = approvalDraftRequestDTO.getApprovalLines();
        ApproverInfoDTO nextApprover = null;

        // 1. 반복문을 돌며 STEP_SEQ가 2인 '다음 결재자' 찾기
        for (ApproverInfoDTO line : aprLines) {
            if (line.getStepSeq() > 1 && !"09".equals(line.getApproveType())) { // 기안자 직후의 결재자여야하는데 그 결재자의 결재유형은 미결이 아니어야됨
                nextApprover = line;
                break;
            }
        }

        // 2. 만약 1인 결재(기안 후 바로 전결 등)라 2번이 없을 경우 대비
        if (nextApprover == null) {
            // 결재선이 기안자뿐이거나 특이케이스일 때 처리
            throw new MissingNextApproverException("다음 단계 결재자가 지정되지 않았습니다.");
        }

        String initialDocStatus = "02"; // 01-임시저장, 02-결재중, 03-완료, 04-반려

        UserMaster nextMember = userRepository.findByLoginId(nextApprover.getApproverId());

        StatusMap statusMap = StatusMap.createStatusMap(approvalDraftRequestDTO, nextMember, nextApprover, initialDocStatus);
        documentMaster.addStatusMap(statusMap);
        // ====문서상태관리 엔티티 관련 처리 끝 ======================

        // === 진행문서 결재선 엔티티 관련 처리 시작 =============
        //List<ApproverInfoDTO> aprLines2 = approvalDraftRequestDTO.getApprovalLines();

        // 첫 번째 결재자 순번을 찾기 위한 변수 (초기값은 아주 큰 값으로 설정)
        int firstApproverSeq = Integer.MAX_VALUE;

        for (ApproverInfoDTO line : aprLines) {
            // 1. 기안자(1번)는 제외
            // 2. 결재 유형이 '02(결재)'인 경우만 체크
            if (line.getStepSeq() > 1 && "02".equals(line.getApproveType())) {

                // 그중 가장 작은(빠른) 순번을 찾음
                if (line.getStepSeq() < firstApproverSeq) {
                    firstApproverSeq = line.getStepSeq();
                }
            }
        }

        for(ApproverInfoDTO line : aprLines){
            UserMaster approver = userRepository.findByLoginId(line.getApproverId());
            AprLine aprLine = AprLine.createAprLine(line, firstApproverSeq, approver);
            documentMaster.addAprLine(aprLine);
        }


        // === 진행문서 결재선 엔티티 관련 처리 끝 =============


        documentMasterRepository.save(documentMaster);

    }


    @Transactional
    public void doProcess(ApprovalProcessRequestDTO approvalProcessRequestDTO, UserMaster loginUser) {

        loginUser = userRepository.findByLoginId(loginUser.getLoginId());
        ApprovalType type = ApprovalType.valueOf(approvalProcessRequestDTO.getApproveType().toUpperCase());

        ApprovalAction action = actionMap.get(type);

        if (action == null) {
            throw new ApprovalInvalidTypeException("지원하지 않는 결재 유형입니다: " + type);
        }

        action.doProcess(approvalProcessRequestDTO,loginUser);
    }

    public Page<StatusMap> getApprovalList(ApprovalSearchDTO dto, UserMaster loginUser, Pageable pageable) {
        dto.setLoginMemId(loginUser.getLoginId());
        //dto.setLoginDeptCode(loginUser.getDeptCode());

        Page<StatusMap> statusMaps = statusMapRepository.docToDoList(pageable,loginUser.getLoginId());

        return statusMaps;
//        return approvalMapper.selectApprovalList(dto);


    }

    private String refineDocContent(String docContent) {
        String plainText = "";

        // 1. 태그 제거
        plainText = docContent.replaceAll("<[^>]*>", "").trim();

        // 2. 특수문자 엔티티 처리 (&nbsp; &lt; 등)
        return plainText.replaceAll("&nbsp;", " ")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&");

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
