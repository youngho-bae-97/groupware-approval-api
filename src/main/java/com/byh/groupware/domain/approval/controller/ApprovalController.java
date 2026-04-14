package com.byh.groupware.domain.approval.controller;

import com.byh.groupware.domain.approval.dto.*;
import com.byh.groupware.domain.approval.service.ApprovalAction;
import com.byh.groupware.domain.approval.service.ApprovalService;
import com.byh.groupware.domain.user.exception.MissingLoginUserException;
import com.byh.groupware.domain.user.model.UserMasterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "Approval", description = "전자결재 관련 API")
@RestController
@Validated
@RequestMapping("/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;



    @Operation(summary = "문서 기안", description = "클라이언트로부터 전달받은 값들로 기안 처리")
    @PostMapping("/draft")
    public void doDraft(@Parameter(description = "문서 기안에 필요한 데이터(json)", example = "") @RequestBody @Valid ApprovalDraftRequestDTO approvalDraftRequestDTO, HttpSession session){

        // 로그인 성공 시 세션에 저장해둔 객체를 꺼내서 사용
        UserMasterVO loginUser = (UserMasterVO) session.getAttribute("loginUser");

        if (loginUser == null) {
            log.warn("기안 요청 failed!! - 로그인 사용자 없음");
            throw new MissingLoginUserException("로그인이 필요합니다.");
        }

        log.info("기안 요청 started!! - userId: {}, title: {}", loginUser.getMemId(), approvalDraftRequestDTO.getDocTitle());

        approvalService.draft(approvalDraftRequestDTO,loginUser);

        log.info("기안 요청 ended!! - userId: {}, docId: {}", loginUser.getMemId() , approvalDraftRequestDTO.getDocId());
    }

    @Operation(summary = "문서에 대한 결재/합의/반려/검토 등의 처리 진행", description = "클라이언트로부터 전달받은 결재유형값에 알맞은 처리 진행")
    @PostMapping("/process")
    public void doProcess(@Parameter(description = "결재유형 / 문서번호(json데이터)", example = "") @RequestBody @Valid ApprovalProcessRequestDTO approvalProcessRequestDTO, HttpSession session){
        UserMasterVO loginUser = (UserMasterVO) session.getAttribute("loginUser");
        if (loginUser == null) {
            log.warn("결재 요청 failed!! - 로그인 사용자 없음");
            throw new MissingLoginUserException("로그인이 필요합니다.");
        }
        log.info("결재 요청 started!! - userId: {}, docId: {}", loginUser.getMemId(), approvalProcessRequestDTO.getDocId());
        approvalService.doProcess(approvalProcessRequestDTO,loginUser);
        log.info("결재 요청 ended!! - userId: {}, docId: {}", loginUser.getMemId(), approvalProcessRequestDTO.getDocId());
    }

    @Operation(summary = "문서 목록 조회", description = "클라이언트로부터 전달받은 viewType값에 따라 필요한 문서목록을 조회")
    @GetMapping("docList")
    public List<ApprovalListResponseDTO> getDocList(@Parameter(description = "결재유형 및 검색키워드를 쿼리파라미터로 전달", example = "http://localhost:8080/approval/docList?viewType=TODO&searchKeyword=2026") @ModelAttribute ApprovalSearchDTO dto, HttpSession session){
        UserMasterVO loginUser = (UserMasterVO) session.getAttribute("loginUser");

        return approvalService.getApprovalList(dto, loginUser);
    }

    @Operation(summary = "문서 상세 조회", description = "문서 ID와 상태값을 받아 상세 정보를 조회")
    @GetMapping("docDetail")
    public ApprovalDetailResponseDTO getDocDetail(
            @Parameter(description = "문서 번호", example = "2026FORM-0100001") @RequestParam("docId") @NotBlank(message = "문서 번호는 필수입니다.") String docId,
            @Parameter(description = "문서 상태(01:임시저장, 02:진행중, 03:완료, 04:반려)", example = "01")
            @RequestParam("docStatus")
            @NotBlank(message = "문서 상태는 필수입니다.")
            @Pattern(regexp = "^(01|02|03|04)$", message = "유효하지 않은 문서 상태입니다.")String docStatus,
            HttpSession session) throws AccessDeniedException {

        UserMasterVO loginUser = (UserMasterVO) session.getAttribute("loginUser");
        return approvalService.getApprovalDetail(docId, docStatus, loginUser.getMemId());
    }





}
