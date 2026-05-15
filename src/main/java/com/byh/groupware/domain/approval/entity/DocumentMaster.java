package com.byh.groupware.domain.approval.entity;

import com.byh.groupware.domain.approval.dto.ApprovalDraftRequestDTO;
import com.byh.groupware.domain.user.entity.UserMaster;
import com.byh.groupware.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tbl_document_master")
public class DocumentMaster extends BaseEntity implements Persistable<String> {

    @Id
    @Column(name = "doc_id")
    private String id;              // 문서번호 (PK)

//    private LocalDateTime draftTime;    // 기안 일시
    private String drafterDept;        // 기안 당시 부서 (박제)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_form_id")
    private DocForm docForm;     // 결재 양식 ID (FK)
    private String securityLevel;      // 보안 등급 (S, A, B...)
    private String preserveYear;       // 보존 연한
    private String rootDocId;          // 원문서 번호 (재기안/수정기안용)
    private int version;               // 문서 버전 (기본값 0)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mem_id")
    private UserMaster userMaster;          // 기안자 사번 (FK)
    private String drafterName;        // 기안자 성명 (박제)
    private String approvalFormTitle;  // 양식 명칭 (박제 - ex: 연차신청서)

    @OneToOne(mappedBy = "documentMaster",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    private StatusMap statusMap;

    @OneToOne(mappedBy = "documentMaster",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    private ActiveDoc activeDoc;

    @OneToMany(mappedBy = "documentMaster",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<AprLine> aprLines = new ArrayList<>();

    private DocumentMaster(ApprovalDraftRequestDTO approvalDraftRequestDTO, UserMaster userMaster, DocForm docForm){
        this.id = approvalDraftRequestDTO.getDocId();
        this.securityLevel = approvalDraftRequestDTO.getSecurityLevel();
        this.preserveYear = approvalDraftRequestDTO.getPreserveYear();
        this.rootDocId = approvalDraftRequestDTO.getRootDocId();
        this.version = approvalDraftRequestDTO.getVersion();
        this.drafterDept = approvalDraftRequestDTO.getDrafterDept();
        this.drafterName = approvalDraftRequestDTO.getDrafterName();
        this.approvalFormTitle = approvalDraftRequestDTO.getApprovalFormTitle();
        // 아래필드는 연관관계 걸려있는 엔티티들(정합성 위해서 편의 메소드 추가해줘야됨!!!)
        this.userMaster = userMaster;
        this.docForm = docForm;

    }

    public static DocumentMaster createDocumentMaster(ApprovalDraftRequestDTO approvalDraftRequestDTO,UserMaster userMaster, DocForm docForm) {
        return new DocumentMaster(approvalDraftRequestDTO, userMaster,docForm);
    }

    @Override
    public String getId(){
        return id;
    }

    @Override
    public boolean isNew() {
        return getCreatedDate() == null;
    }
    public void addStatusMap(StatusMap statusMap){
        this.statusMap = statusMap;

        if(statusMap.getDocumentMaster() != this){
            statusMap.confirmMaster(this);
        }
    }
    public void addActiveDoc(ActiveDoc activeDoc){
        this.activeDoc = activeDoc;

        if(activeDoc.getDocumentMaster() != this){
            activeDoc.confirmMaster(this);
        }
    }

    public void addAprLine(AprLine aprLine){
        this.aprLines.add(aprLine);

        if(aprLine.getDocumentMaster() != this){
            aprLine.confirmMaster(this);
        }
    }

    public void removeStatusMap(){

        if(this.statusMap != null){
            this.statusMap.confirmMaster(null);
        }

        this.statusMap = null;
    }

    public void removeActiveDoc(){

        if(this.activeDoc != null){
            this.activeDoc.confirmMaster(null);
        }

        this.activeDoc = null;
    }

    public void removeAprLines(){

        for(AprLine aprLine : this.aprLines){
            aprLine.confirmMaster(null);
        }

        this.aprLines.clear();
    }


}