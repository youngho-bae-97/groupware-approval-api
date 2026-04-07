package com.byh.groupware.domain.approval.controller;

import com.byh.groupware.domain.approval.dto.*;
import com.byh.groupware.domain.approval.service.ApprovalAction;
import com.byh.groupware.domain.approval.service.ApprovalService;
import com.byh.groupware.domain.user.model.UserMasterVO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;



    @PostMapping("/draft")
    public void doDraft(@RequestBody ApprovalDraftRequestDTO approvalDraftRequestDTO, HttpSession session){

        // 로그인 성공 시 세션에 저장해둔 객체를 꺼내서 사용
        UserMasterVO loginUser = (UserMasterVO) session.getAttribute("loginUser");

        approvalService.draft(approvalDraftRequestDTO,loginUser);

    }

    @PostMapping("/process")
    public void doProcess(@RequestBody ApprovalProcessRequestDTO approvalProcessRequestDTO, HttpSession session){
        UserMasterVO loginUser = (UserMasterVO) session.getAttribute("loginUser");

        approvalService.doProcess(approvalProcessRequestDTO,loginUser);
    }

    @GetMapping("docList")
    public List<ApprovalListResponseDTO> getDocList(@ModelAttribute ApprovalSearchDTO dto, HttpSession session){
        UserMasterVO loginUser = (UserMasterVO) session.getAttribute("loginUser");

        return approvalService.getApprovalList(dto, loginUser);
    }

    @GetMapping("docDetail")
    public ApprovalDetailResponseDTO getDocDetail(
            @RequestParam("docId") String docId,
            @RequestParam("docStatus") String docStatus,
            HttpSession session) throws AccessDeniedException {

        UserMasterVO loginUser = (UserMasterVO) session.getAttribute("loginUser");
        return approvalService.getApprovalDetail(docId, docStatus, loginUser.getMemId());
    }





}
