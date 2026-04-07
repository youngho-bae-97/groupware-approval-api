package com.byh.groupware.domain.approval.dto;

import com.byh.groupware.domain.approval.model.AprLineVO;
import lombok.Data;

import java.util.List;

@Data
public class ApprovalProcessRequestDTO {
    private String docId;
    private String processorId;         // 결재자 사번
    private String processorName;       // 결재자 이름
    private String processorDept;       // 결재자 소속부서
    private String processorJobGrade;          // 결재자 직급

    private String approveType;

    private String docTitle;          // 문서 제목
    private String docContent;        // 실제 HTML 본문 내용
    private String urgentYn;          // 긴급 여부 (Y/N)
    private String attachYn;          // 첨부파일 여부 (Y/N)
    private String filePath;
    private String fileName;
    private String contentType;

    // 상세정보로 조회 후 해당문서에 대한 결재선 정보를 받았다고 가정
    private AprLineVO currApprover;

}
