package com.byh.groupware.domain.approval.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EndDocVO {
    private String docId;
    private LocalDateTime completedDate;
    private LocalDateTime archiveDate;
    private String attachYn;
    private String urgentYn;
    private String docTitle;
    private String searchContent;
    private String filePath;
    private String fileName;
    private String contentType;
}
