package com.byh.groupware.domain.approval.entity;

import com.byh.groupware.domain.approval.dto.ApprovalDraftRequestDTO;
import com.byh.groupware.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tbl_activedoc")
public class ActiveDoc extends BaseEntity implements Persistable<String> {
    @Id
    @Column(name = "doc_id")
    private String id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id")
    private DocumentMaster documentMaster;

    private String attachYn;
    private String urgentYn;
    private String docTitle;
    private String searchContent;
    private String filePath;
    private String fileName;
    private String contentType;

    private ActiveDoc(ApprovalDraftRequestDTO approvalDraftRequestDTO){
        this.attachYn = approvalDraftRequestDTO.getAttachYn();
        this.urgentYn = approvalDraftRequestDTO.getUrgentYn();
        this.docTitle = approvalDraftRequestDTO.getDocTitle();
        this.searchContent = approvalDraftRequestDTO.getDocContent();
        this.filePath = approvalDraftRequestDTO.getFilePath();
        this.fileName = approvalDraftRequestDTO.getFileName();
        this.contentType = approvalDraftRequestDTO.getContentType();
    }

    public static ActiveDoc createActiveDoc(ApprovalDraftRequestDTO approvalDraftRequestDTO) {

        return new ActiveDoc(approvalDraftRequestDTO);
    }

    public void confirmMaster(DocumentMaster master){
        this.documentMaster = master;
        this.id = master.getId();
    }

    @Override
    public String getId(){
        return id;
    }

    @Override
    public boolean isNew() {
        return getCreatedDate() == null;
    }

}
