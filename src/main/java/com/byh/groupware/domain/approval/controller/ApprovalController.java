package com.byh.groupware.domain.approval.controller;

import com.byh.groupware.domain.approval.dto.*;
import com.byh.groupware.domain.approval.entity.ActiveDoc;
import com.byh.groupware.domain.approval.service.JpaApprovalService;
import com.byh.groupware.domain.user.exception.MissingLoginUserException;
import com.byh.groupware.domain.user.entity.UserMaster;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * 전자결재 문서 API
 * - 문서 리소스 식별자는 문서 마스터 ID 를 사용함
 *   (문서 마스터는 진행/완료 구분 없이 유지되므로, 최종결재로 완료 테이블로 이관되어도 URL 이 바뀌지 않음)
 */
@Slf4j
@Tag(name = "Approval", description = "전자결재 관련 API")
@RestController
@Validated
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final JpaApprovalService jpaApprovalService;

    @Operation(summary = "문서 기안", description = "문서를 기안하고, 생성된 문서의 위치를 Location 헤더로 반환")
    @PostMapping
    public ResponseEntity<ApprovalDraftResponseDTO> draft(
            @Parameter(description = "문서 기안에 필요한 데이터(json)") @RequestBody @Valid ApprovalDraftRequestDTO approvalDraftRequestDTO,
            HttpSession session) {

        UserMaster loginUser = getLoginUser(session);

        log.info("기안 요청 started!! - userId: {}, title: {}", loginUser.getLoginId(), approvalDraftRequestDTO.getDocTitle());

        Long documentMasterId = jpaApprovalService.draft(approvalDraftRequestDTO, loginUser);

        log.info("기안 요청 ended!! - userId: {}, docId: {}", loginUser.getLoginId(), approvalDraftRequestDTO.getDocId());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(documentMasterId)
                .toUri();

        return ResponseEntity.created(location)
                .body(new ApprovalDraftResponseDTO(documentMasterId, approvalDraftRequestDTO.getDocId()));
    }

    @Operation(summary = "문서 목록 조회", description = "viewType(TODO: 결재대기함, DRAFT: 기안함)에 따라 문서 목록을 조회. 생략하면 결재대기함")
    @GetMapping
    public Page<ApprovalListResponseDTO> getDocList(
            @Parameter(description = "조회 타입 및 검색 키워드 (예: /api/approvals?viewType=TODO&searchKeyword=2026)")
            @ModelAttribute ApprovalSearchDTO dto,
            @PageableDefault(size = 5, sort = "createdDate") Pageable pageable,
            HttpSession session) {

        UserMaster loginUser = getLoginUser(session);

        Page<ActiveDoc> page = jpaApprovalService.getApprovalList(dto, loginUser, pageable);

        return page.map(ApprovalListResponseDTO::new);
    }

    @Operation(summary = "문서 상세 조회", description = "진행/완료 여부와 관계없이 문서 마스터 ID 로 상세 정보를 조회")
    @GetMapping("/{documentMasterId}")
    public ApprovalDetailResponseDTO getDocDetail(
            @Parameter(description = "문서 마스터 ID", example = "1") @PathVariable Long documentMasterId,
            HttpSession session) {

        UserMaster loginUser = getLoginUser(session);

        return jpaApprovalService.getApprovalDetail(documentMasterId, loginUser.getLoginId());
    }

    @Operation(summary = "결재", description = "현재 결재 차례인 사용자가 문서를 결재. 마지막 결재자면 최종결재 후 완료 문서로 이관. 본문 수정이 없으면 요청 본문 생략 가능")
    @PostMapping("/{documentMasterId}/approve")
    public ResponseEntity<Void> approve(
            @Parameter(description = "문서 마스터 ID", example = "1") @PathVariable Long documentMasterId,
            @Parameter(description = "결재 시 본문 수정 내용(선택)") @RequestBody(required = false) @Valid ApprovalProcessRequestDTO approvalProcessRequestDTO,
            HttpSession session) {

        UserMaster loginUser = getLoginUser(session);
        ApprovalProcessRequestDTO request = approvalProcessRequestDTO != null ? approvalProcessRequestDTO : new ApprovalProcessRequestDTO();

        log.info("결재 요청 started!! - userId: {}, documentMasterId: {}", loginUser.getLoginId(), documentMasterId);
        jpaApprovalService.approve(documentMasterId, request, loginUser);
        log.info("결재 요청 ended!! - userId: {}, documentMasterId: {}", loginUser.getLoginId(), documentMasterId);

        return ResponseEntity.noContent().build();
    }

    // 로그인 성공 시 세션에 저장해둔 사용자 정보를 꺼냄 (없으면 401)
    private UserMaster getLoginUser(HttpSession session) {
        UserMaster loginUser = (UserMaster) session.getAttribute("loginUser");

        if (loginUser == null) {
            throw new MissingLoginUserException("로그인이 필요합니다.");
        }
        return loginUser;
    }
}
