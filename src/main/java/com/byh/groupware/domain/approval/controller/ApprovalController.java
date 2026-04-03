package com.byh.groupware.domain.approval.controller;

import com.byh.groupware.domain.approval.dto.ApprovalDraftRequestDTO;
import com.byh.groupware.domain.approval.service.ApprovalService;
import com.byh.groupware.domain.user.model.UserMasterVO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @PostMapping("/draft")
    public void DoDraft(@RequestBody ApprovalDraftRequestDTO approvalDraftRequestDTO, HttpSession session){

        // 로그인 성공 시 세션에 저장해둔 객체를 꺼내서 사용
        UserMasterVO loginUser = (UserMasterVO) session.getAttribute("loginUser");

        approvalService.draft(approvalDraftRequestDTO,loginUser);

    }

}
