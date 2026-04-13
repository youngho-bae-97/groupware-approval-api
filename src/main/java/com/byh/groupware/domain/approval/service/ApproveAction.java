package com.byh.groupware.domain.approval.service;

import com.byh.groupware.domain.approval.dto.ApprovalProcessRequestDTO;
import com.byh.groupware.domain.approval.dto.ApproverInfoDTO;
import com.byh.groupware.domain.approval.mapper.ApprovalMapper;
import com.byh.groupware.domain.approval.model.AprLineVO;
import com.byh.groupware.domain.approval.model.StatusMapVO;
import com.byh.groupware.domain.approval.type.ApprovalType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApproveAction implements ApprovalAction {

    private final ApprovalMapper approvalMapper;

    @Override
    public ApprovalType getActionType() {
        return ApprovalType.APPROVE;
    }


    // 일반 결재에 대한 결재로직
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doProcess(ApprovalProcessRequestDTO dto) {

        // 이미 완료된 문서인지 확인
        isCompletedDocment(dto);

        // 결재 권한 확인
        checkApprovalAuthority(dto);
        boolean isFinal = approvalMapper.isFinalApprover(dto.getDocId(), dto.getProcessorId());

        // 문서상태관리 테이블 업데이트 ( 문서상태관리테이블 업데이트는 일반결재, 최종결재 모두 해야하므로 isFinal플래그로 구분짓도록함)
        updateStatusMap(dto,isFinal);

        updateAprLine(dto, isFinal);

        updateActiveDoc(dto, isFinal);

        // 최종결재일 경우 아래의 추가작업을 더 진행하도록 함
        if(isFinal){
            doCompleteDocument(dto);
        }



    }

    private void checkApprovalAuthority(ApprovalProcessRequestDTO dto) {

        int count = approvalMapper.checkApproverAuthority(dto.getDocId(), dto.getProcessorId());

        if (count == 0) {
            throw new RuntimeException("해당 문서에 대한 결재 권한이 없거나 현재 결재 순서가 아닙니다.");
        }
    }

    private void doCompleteDocument(ApprovalProcessRequestDTO dto) {
        String docId = dto.getDocId();

        // 1. 진행 문서 본문 -> 완료 문서 본문으로 복사 (INSERT INTO ... SELECT)
        approvalMapper.insertEndDoc(docId);

        // 2. 진행 결재선 -> 완료 결재선으로 복사 (INSERT INTO ... SELECT)
        approvalMapper.insertEndAprLine(docId);

        // 3. 진행 테이블 데이터 삭제 (DELETE)
        approvalMapper.deleteActiveDoc(docId);
        approvalMapper.deleteAprLine(docId);

    }

    private void isCompletedDocment(ApprovalProcessRequestDTO dto) {
        StatusMapVO statusMapVO = approvalMapper.selectStatusForUpdate(dto.getDocId());

        if (!"02".equals(statusMapVO.getDocStatus())) {
            throw new RuntimeException("결재 가능한 상태가 아닙니다.");
        }
    }

    private void updateStatusMap(ApprovalProcessRequestDTO dto, boolean isFinal) {

        String docStatus = isFinal ? "03" : "02";


        dto.setCurrApprover(approvalMapper.selectNextApprover(dto,isFinal));


        approvalMapper.updateStatusMap(dto,docStatus);
    }

    private void updateActiveDoc(ApprovalProcessRequestDTO dto, boolean isFinal) {
        approvalMapper.updateActiveDoc(dto.getDocId(),isFinal);
    }

    private void updateAprLine(ApprovalProcessRequestDTO dto, boolean isFinal) {


        // 앞선 updateStatusMap메소드를 거치면서 dto객체의 currApprover에는 다음과 같이 데이터가 저장되어있음
        // 최종결재일 경우 => 현재 결재행위를 하는 사람의 정보
        // 중간결재일 경우 => 직후 결재자의 정보
        // 최종결재일 경우에는 현재결재자 정보만 currApprover로 업데이트만 해주면 끝
        approvalMapper.updateCurrentApproverStatus(dto);

        if (!isFinal && dto.getCurrApprover() != null) {
            // nextApprover 객체에 담긴 정보를 활용
            approvalMapper.updateNextApproverStatus(
                    dto.getDocId(),
                    dto.getCurrApprover().getStepSeq()
            );
        }

//        approvalMapper.updateAprLine(dto,isFinal);

    }

}
