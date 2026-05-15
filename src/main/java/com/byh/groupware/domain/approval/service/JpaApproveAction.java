package com.byh.groupware.domain.approval.service;

import com.byh.groupware.domain.approval.dto.ApprovalProcessRequestDTO;
import com.byh.groupware.domain.approval.entity.*;
import com.byh.groupware.domain.approval.exception.ApprovalAccessDeniedException;
import com.byh.groupware.domain.approval.exception.DocStatusMissMatchException;
import com.byh.groupware.domain.approval.repository.*;
import com.byh.groupware.domain.approval.type.ApprovalType;
import com.byh.groupware.domain.user.entity.UserMaster;
import com.byh.groupware.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JpaApproveAction implements ApprovalAction{

    private final StatusMapRepository statusMapRepository;

    private final AprLineRepository aprLineRepository;

    private final UserRepository userRepository;

    private final ActiveDocRepository activeDocRepository;

    private final DocumentMasterRepository documentMasterRepository;

    private final EndDocumentMasterRepository endDocumentMasterRepository;
    @Override
    public ApprovalType getActionType() {
        return ApprovalType.APPROVE;
    }

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void doProcess(ApprovalProcessRequestDTO dto, UserMaster loginUser) {
//
//        dto.setProcessorId(loginUser.getLoginId());
//
//        // 결재가능상태확인ㅌ₩
//        StatusMap statusMap = statusMapRepository.findById(dto.getDocId()).get();
//        if(!"02".equals(statusMap.getDocStatus())){
//            throw new DocStatusMissMatchException("결재 가능한 상태가 아닙니다.");
//        }
//
//        // 결재권한 확인
//        long cnt = aprLineRepository.countApproverLine(dto.getDocId(), loginUser.getLoginId(), "02", Arrays.asList("02", "03"));
//        if (cnt == 0) {
//            throw new ApprovalAccessDeniedException("해당 문서에 대한 결재 권한이 없거나 현재 결재 순서가 아닙니다.");
//        }
//
////        boolean isFinal =
//        long remainCnt = aprLineRepository.countRemainingApprovers(dto.getDocId(), dto.getProcessorId());
//        boolean isFinal = remainCnt == 0;
//
//        // 문서상태관리 업데이트
//        String docStatus = isFinal ? "03" : "02";
//
//        if(isFinal){
//            dto.setCurrApprover(aprLineRepository.findFinalAprLineById(dto.getDocId(),dto.getProcessorId(), PageRequest.of(0, 1)));
//        }else{
//            dto.setCurrApprover(aprLineRepository.findNextAprLineById(dto.getDocId(),dto.getProcessorId(),PageRequest.of(0, 1)));
//        }
//
//        UserMaster currApprover = userRepository.findById(dto.getCurrApprover().getUserMaster().getId()).get();
//
//        statusMap.doApprove(dto, currApprover,docStatus);
//
//        AprLine currLine = aprLineRepository.findCurrAprLine(dto.getDocId(),dto.getProcessorId());
//        currLine.doApprove("03");
//
//        if(!isFinal && dto.getCurrApprover() != null){
//            AprLine nextLine = aprLineRepository.findNextAprLineById(dto.getDocId(), dto.getProcessorId(), PageRequest.of(0, 1));
//            nextLine.doApprove("02");
//        }
//
//        ActiveDoc activeDoc = activeDocRepository.findById(dto.getDocId()).get();
//        activeDoc.doApprove(dto.getDocTitle(), dto.getDocContent());
//
//
//        // hot=>cold 이관
//        if(isFinal){
//            DocumentMaster documentMaster = documentMasterRepository.findFetchedDocumentMasterById(dto.getDocId());
//            EndDocumentMaster endDocumentMaster = EndDocumentMaster.createEndDocumentMaster(documentMaster);
//
//            endDocumentMaster.addEndDoc(EndDoc.createEndDoc(documentMaster.getActiveDoc()));
//            endDocumentMaster.addEndStatusMap(EndStatusMap.createEndStatusMap(documentMaster.getStatusMap()));
//
//            for (AprLine line : documentMaster.getAprLines()) {
//                endDocumentMaster.addEndAprLine(EndAprLine.createEndAprLine(line));
//            }
//
//            endDocumentMasterRepository.save(endDocumentMaster);
//
//            documentMaster.removeStatusMap();
//            documentMaster.removeActiveDoc();
//            documentMaster.removeAprLines();
//
//
//
//
//        }
//
//    }

@Override
@Transactional(rollbackFor = Exception.class)
public void doProcess(
        ApprovalProcessRequestDTO dto,
        UserMaster loginUser
) {

    dto.setProcessorId(loginUser.getLoginId());

    // 공통 검증
    validateApproval(dto, loginUser);

    // 최종결재 여부 판단
    boolean isFinal = isFinalApproval(dto);

    // 일반결재
    if (!isFinal) {
        processNormalApproval(dto);
        return;
    }

    // 최종결재
    processFinalApproval(dto);
}

    /**
     * 공통 검증
     */
    private void validateApproval(
            ApprovalProcessRequestDTO dto,
            UserMaster loginUser
    ) {

        StatusMap statusMap =
                statusMapRepository.findById(dto.getDocId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("문서가 존재하지 않습니다."));

        // 결재 가능 상태 확인
        if (!"02".equals(statusMap.getDocStatus())) {
            throw new DocStatusMissMatchException(
                    "결재 가능한 상태가 아닙니다."
            );
        }

        // 결재 권한 확인
        long cnt =
                aprLineRepository.countApproverLine(
                        dto.getDocId(),
                        loginUser.getLoginId(),
                        "02",
                        Arrays.asList("02", "03")
                );

        if (cnt == 0) {
            throw new ApprovalAccessDeniedException(
                    "해당 문서에 대한 결재 권한이 없거나 현재 결재 순서가 아닙니다."
            );
        }
    }

    /**
     * 최종결재 여부 확인
     */
    private boolean isFinalApproval(
            ApprovalProcessRequestDTO dto
    ) {

        long remainCnt =
                aprLineRepository.countRemainingApprovers(
                        dto.getDocId(),
                        dto.getProcessorId()
                );

        return remainCnt == 0;
    }

    /**
     * 일반결재
     * -> 낙관적 락(@Version)
     */
    private void processNormalApproval(
            ApprovalProcessRequestDTO dto
    ) {

        // StatusMap 조회
        StatusMap statusMap =
                statusMapRepository.findById(dto.getDocId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("상태 정보가 존재하지 않습니다."));

        // 다음 결재자 조회
        Slice<AprLine> nextLineSlice =
                aprLineRepository.findNextAprLineById(
                        dto.getDocId(),
                        dto.getProcessorId(),
                        PageRequest.of(0, 1)
                );

        AprLine nextLine =
                nextLineSlice.stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "다음 결재자가 존재하지 않습니다."
                                ));

        dto.setCurrApprover(nextLine);

        UserMaster currApprover =
                userRepository.findById(
                        nextLine.getUserMaster().getId()
                ).orElseThrow(() ->
                        new IllegalArgumentException("다음 결재자가 존재하지 않습니다."));

        // 상태 변경
        statusMap.doApprove(dto, currApprover, "02");

        // 현재 결재선 승인 처리
        AprLine currLine =
                aprLineRepository.findCurrAprLine(
                        dto.getDocId(),
                        dto.getProcessorId()
                );

        currLine.doApprove("03");

        // 다음 결재선 활성화
        nextLine.doApprove("02");

        // ActiveDoc 수정
        updateActiveDoc(dto);
    }

    /**
     * 최종결재
     * -> 비관적 락
     */
    private void processFinalApproval(
            ApprovalProcessRequestDTO dto
    ) {

        // 비관적 락으로 상태 조회
        StatusMap statusMap =
                statusMapRepository.findForFinalApprove(
                        dto.getDocId()
                );

        // 락 획득 후 상태 재검증
        if (!"02".equals(statusMap.getDocStatus())) {
            throw new DocStatusMissMatchException(
                    "이미 처리된 문서입니다."
            );
        }

        // 현재 결재선 조회
        AprLine currLine =
                aprLineRepository.findCurrAprLine(
                        dto.getDocId(),
                        dto.getProcessorId()
                );

        // 현재 결재자
        UserMaster currApprover =
                currLine.getUserMaster();

        dto.setCurrApprover(currLine);
        // 최종 상태 변경
        statusMap.doApprove(dto, currApprover, "03");

        // 현재 결재선 완료
        currLine.doApprove("03");

        // ActiveDoc 수정
        updateActiveDoc(dto);

        // hot -> cold 이관
        archiveDocument(dto);
    }

    /**
     * ActiveDoc 수정
     */
    private void updateActiveDoc(
            ApprovalProcessRequestDTO dto
    ) {

        ActiveDoc activeDoc =
                activeDocRepository.findById(dto.getDocId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("문서 본문이 존재하지 않습니다."));

        activeDoc.doApprove(
                dto.getDocTitle(),
                dto.getDocContent()
        );
    }

    /**
     * hot -> cold 이관
     */
    private void archiveDocument(
            ApprovalProcessRequestDTO dto
    ) {

        DocumentMaster documentMaster =
                documentMasterRepository.findFetchedDocumentMasterById(
                        dto.getDocId()
                );

        EndDocumentMaster endDocumentMaster =
                EndDocumentMaster.createEndDocumentMaster(
                        documentMaster
                );

        // ActiveDoc 복사
        endDocumentMaster.addEndDoc(
                EndDoc.createEndDoc(
                        documentMaster.getActiveDoc()
                )
        );

        // StatusMap 복사
        endDocumentMaster.addEndStatusMap(
                EndStatusMap.createEndStatusMap(
                        documentMaster.getStatusMap()
                )
        );

        // AprLine 복사
        for (AprLine line : documentMaster.getAprLines()) {

            endDocumentMaster.addEndAprLine(
                    EndAprLine.createEndAprLine(line)
            );
        }

        // cold 저장
        endDocumentMasterRepository.save(endDocumentMaster);

        // hot 제거 (orphanRemoval)
        documentMaster.removeStatusMap();
        documentMaster.removeActiveDoc();
        documentMaster.removeAprLines();
    }

}
