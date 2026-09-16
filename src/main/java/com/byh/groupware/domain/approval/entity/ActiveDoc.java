package com.byh.groupware.domain.approval.entity;

import com.byh.groupware.domain.approval.dto.ApprovalDraftRequestDTO;
import com.byh.groupware.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.domain.Persistable;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tbl_activedoc")
public class ActiveDoc extends BaseEntity {
    @Id @GeneratedValue
    @Column(name = "activedoc_id")
    private Long id;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_master_id")
    private DocumentMaster documentMaster;


    @OneToMany(mappedBy = "activeDoc",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @BatchSize(size = 100)
    private List<AprLine> aprLines = new ArrayList<>();

    private String attachYn;
    private String urgentYn;
    private String docTitle;
    private String searchContent;
    private String filePath;
    private String fileName;
    private String contentType;
    // 2026.09.15 문서상태 컬럼 새로 추가
    private String docStatus;

    private ActiveDoc(ApprovalDraftRequestDTO approvalDraftRequestDTO){
        this.attachYn = approvalDraftRequestDTO.getAttachYn();
        this.urgentYn = approvalDraftRequestDTO.getUrgentYn();
        this.docTitle = approvalDraftRequestDTO.getDocTitle();
        this.searchContent = approvalDraftRequestDTO.getDocContent();
        this.filePath = approvalDraftRequestDTO.getFilePath();
        this.fileName = approvalDraftRequestDTO.getFileName();
        this.contentType = approvalDraftRequestDTO.getContentType();
        this.docStatus = approvalDraftRequestDTO.getDocStatus();
    }

    public static ActiveDoc createActiveDoc(ApprovalDraftRequestDTO approvalDraftRequestDTO) {

        return new ActiveDoc(approvalDraftRequestDTO);
    }

    public void confirmMaster(DocumentMaster master){
        this.documentMaster = master;
        if(master != null){
            this.id = master.getId();
        }
    }

    public void addAprLine(AprLine aprLine){
        this.aprLines.add(aprLine);

        if(aprLine.getActiveDoc() != this){
            aprLine.confirmActiveDoc(this);
        }
    }

    public void removeAprLines(){

        for(AprLine aprLine : this.aprLines){
            aprLine.confirmActiveDoc(null);
        }

        this.aprLines.clear();
    }

//    @Override
//    public Long getId(){
//        return id;
//    }
//
//    @Override
//    public boolean isNew() {
//        return getCreatedDate() == null;
//    }

    public void doApprove(String docTitle, String docContent,String docStatus) {

        this.docStatus = docStatus;

        if(!ObjectUtils.isEmpty(docTitle)){
            this.docTitle = docTitle;
        }

        if (!ObjectUtils.isEmpty(docContent)) {
            this.searchContent = docContent;
        }

    }
}
