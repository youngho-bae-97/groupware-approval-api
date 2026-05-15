package com.byh.groupware.domain.approval.entity;

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
@Table(name = "tbl_enddoc")
public class EndDoc extends BaseEntity implements Persistable<String> {

    @Id
    @Column(name = "doc_id")
    private String id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id")
    private EndDocumentMaster endDocumentMaster;

    private LocalDateTime archiveDate;
    private String attachYn;
    private String urgentYn;
    private String docTitle;
    private String searchContent;
    private String filePath;
    private String fileName;
    private String contentType;

    private EndDoc(ActiveDoc activeDoc){
        this.attachYn = activeDoc.getAttachYn();
        this.urgentYn = activeDoc.getUrgentYn();
        this.docTitle = activeDoc.getDocTitle();
        this.searchContent = activeDoc.getSearchContent();
        this.filePath = activeDoc.getFilePath();
        this.fileName = activeDoc.getFileName();
        this.contentType = activeDoc.getContentType();
    }

    public static EndDoc createEndDoc(ActiveDoc activeDoc) {
        return new EndDoc(activeDoc);
    }

    public void confirmMaster(EndDocumentMaster master){
        this.endDocumentMaster = master;
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
