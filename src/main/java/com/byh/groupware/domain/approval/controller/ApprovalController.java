package com.byh.groupware.domain.approval.controller;

import com.byh.groupware.domain.approval.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @GetMapping("/draft")
    public String test(){
        return approvalService.selectDraftDocument().toString();



    }

}
