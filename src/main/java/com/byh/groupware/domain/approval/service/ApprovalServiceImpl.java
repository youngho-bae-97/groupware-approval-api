package com.byh.groupware.domain.approval.service;

import com.byh.groupware.domain.approval.dto.ApprovalDraftRequestDTO;
import com.byh.groupware.domain.approval.dto.ApprovalProcessRequestDTO;
import com.byh.groupware.domain.approval.dto.ApproverInfoDTO;
import com.byh.groupware.domain.approval.mapper.ApprovalMapper;
import com.byh.groupware.domain.approval.model.ActiveDocVO;
import com.byh.groupware.domain.approval.model.DocumentMasterVO;
import com.byh.groupware.domain.approval.type.ApprovalType;
import com.byh.groupware.domain.user.model.UserMasterVO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {


    private final ApprovalMapper approvalMapper;

    private final List<ApprovalAction> actions;

    private final Map<ApprovalType, ApprovalAction> actionMap = new EnumMap<>(ApprovalType.class);

    @PostConstruct
    public void init() {
        for (ApprovalAction action : actions) {
            actionMap.put(action.getActionType(), action);
        }

    }


    @Override
    public DocumentMasterVO selectDraftDocument() {
        return approvalMapper.selectDraftDocument();
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateDocNo(String formId) {
        // 1. 현재 연도 추출 (예: "2026")
        String currentYear = String.valueOf(LocalDate.now().getYear());

        // 2. 비관적 락을 통해 현재 LAST_SEQ 조회
        Integer currentSeq = approvalMapper.generateDocNo(currentYear, formId);

        if (currentSeq == null) {
            // 만약 해당 연도/양식 데이터가 없다면 에러
            throw new RuntimeException(currentYear + "년도 " + formId + " 양식의 채번 정보가 없습니다.");
        }

        // 3. 다음 번호 계산 및 업데이트 (105 -> 106)
        int nextSeq = currentSeq + 1;
        approvalMapper.updateLastSeq(currentYear, formId);

        // 4. 문서번호 포맷팅 (연도 + 양식ID + 5자리 일련번호)
        // 예: 2026 + VACATION + 00106 => 2026VACATION00106
        String docNo = currentYear + formId + String.format("%05d", nextSeq);

        return docNo;

    }

    @Transactional(rollbackFor = Exception.class)
    public void draft(ApprovalDraftRequestDTO approvalDraftRequestDTO, UserMasterVO loginUser) {
        // 1. 문서번호 채번 (여기서부터 채번테이블의 레코드에 락 시작)
        String newDocNo = generateDocNo(approvalDraftRequestDTO.getApprovalFormId());


        // DTO객체에 기안자의 정보 매핑 (세션으로부터 꺼내서 사용함)
        approvalDraftRequestDTO.setDrafterId(loginUser.getMemId());
        approvalDraftRequestDTO.setDrafterName(loginUser.getMemName());
        approvalDraftRequestDTO.setDrafterDept(loginUser.getDeptName());

        // 채번된 문서번호 세팅
        approvalDraftRequestDTO.setDocId(newDocNo);



        // 2. 문서 마스터 처리
        processInsertMaster(approvalDraftRequestDTO);

        // 3. 진행 문서 처리 (본문 가공 및 저장)
        processInsertActiveDoc(approvalDraftRequestDTO);

        // 4. 문서상태관리 테이블 처리(현재 결재자 정보는 클라이언트로부터 받은 결재선데이터 참고)
        processInsertInitialStatus(approvalDraftRequestDTO);

        // 5. 진행문서 결재선 테이블 처리(마이바티스 foreach 태그 활용)
        processInsertApprovalLines(approvalDraftRequestDTO);




        // 메서드 종료 시점에 COMMIT 되며 락 해제 (Transactional)
    }

    // 결재처리 (복잡한 결재로직은 ApprovalAction인터페이스의 구현클래스에서 처리)
    @Override
    public void doProcess(ApprovalProcessRequestDTO approvalProcessRequestDTO, UserMasterVO loginUser) {
        // DTO객체에 결재자의 정보 매핑 (세션으로부터 꺼내서 사용함)
        approvalProcessRequestDTO.setProcessorId(loginUser.getMemId());
        approvalProcessRequestDTO.setProcessorName(loginUser.getMemName());
        approvalProcessRequestDTO.setProcessorDept(loginUser.getDeptName());

        ApprovalType type = ApprovalType.valueOf(approvalProcessRequestDTO.getApproveType().toUpperCase());

        ApprovalAction action = actionMap.get(type);

        if (action == null) {
            throw new IllegalArgumentException("지원하지 않는 결재 유형입니다: " + type);
        }

        action.doProcess(approvalProcessRequestDTO);
    }

    private void processInsertMaster(ApprovalDraftRequestDTO approvalDraftRequestDTO) {


        // 버전번호 디폴트값 1로 세팅
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

        approvalMapper.insertDocumentMaster(approvalDraftRequestDTO);
    }

    private void processInsertActiveDoc(ApprovalDraftRequestDTO approvalDraftRequestDTO) {

        // 화면으로부터 html태크형태의 문서본문 데이터를 받는다고 가정
        // 실제로 html형태의 문서데이터는 하나의 파일로 서버내 저장될 예정이며(아직 미구현)
        // plainText로 따로 추출후 DB에 저장하는 이유는 나중에 문서내용으로 검색옵션을 주기 위함
        // plainText는 html태그를 벗겨낸 텍스트 데이터
        String plainText = refineDocContent(approvalDraftRequestDTO.getDocContent());

        approvalDraftRequestDTO.setDocContent(plainText);

        // 긴급 여부가 비어있으면 기본값 'N'
        if (!StringUtils.hasText(approvalDraftRequestDTO.getUrgentYn())) {
            approvalDraftRequestDTO.setUrgentYn("N");
        }


        approvalMapper.insertActiveDoc(approvalDraftRequestDTO);

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

    private void processInsertInitialStatus(ApprovalDraftRequestDTO approvalDraftRequestDTO) {
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
            throw new RuntimeException("다음 단계 결재자가 지정되지 않았습니다.");
        }

        String initialDocStatus = "02"; // 01-임시저장, 02-결재중, 03-완료, 04-반려
        // 5. DB INSERT
        approvalMapper.insertStatusMap(approvalDraftRequestDTO, nextApprover, initialDocStatus);

    }

    private void processInsertApprovalLines(ApprovalDraftRequestDTO approvalDraftRequestDTO) {
        List<ApproverInfoDTO> aprLines = approvalDraftRequestDTO.getApprovalLines();

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


        if (aprLines != null && !aprLines.isEmpty()) {

            approvalMapper.insertAprLines(aprLines, approvalDraftRequestDTO.getDocId(),firstApproverSeq);
        } else {
            // 결재선이 없으면 기안이 불가능하므로 예외 처리
            throw new RuntimeException("결재선 정보가 없습니다. 결재자를 지정해주세요.");
        }

    }




}
