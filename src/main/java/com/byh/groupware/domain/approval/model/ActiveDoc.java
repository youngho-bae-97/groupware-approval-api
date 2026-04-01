package com.byh.groupware.domain.approval.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActiveDoc {
    private String docId;
    private String attachYn;
    private String urgentYn;
    private LocalDateTime lastModDate;
    private String docTitle;
    private String searchContent;
    private String filePath;
    private String fileName;
    private String contentType;
}
