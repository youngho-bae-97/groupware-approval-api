package com.byh.groupware.domain.approval.entity;

import com.byh.groupware.domain.user.entity.UserMaster;
import com.byh.groupware.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tbl_end_document_master")
public class EndDocumentMaster extends BaseEntity implements Persistable<String> {
    @Id
    @Column(name = "doc_id")
    private String id;              // 문서번호 (PK)

    //    private LocalDateTime draftTime;    // 기안 일시
    private String drafterDept;        // 기안 당시 부서 (박제)
    private String approvalFormId;     // 결재 양식 ID (FK)
    private String securityLevel;      // 보안 등급 (S, A, B...)
    private String preserveYear;       // 보존 연한
    private String rootDocId;          // 원문서 번호 (재기안/수정기안용)
    private int version;               // 문서 버전 (기본값 0)
    private String memId;          // 기안자 사번 (FK)
    private String drafterName;        // 기안자 성명 (박제)
    private String approvalFormTitle;  // 양식 명칭 (박제 - ex: 연차신청서)

    @OneToOne(mappedBy = "endDocumentMaster", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private EndStatusMap endStatusMap;

    @OneToOne(mappedBy = "endDocumentMaster", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private EndDoc endDoc;

    @OneToMany(mappedBy = "endDocumentMaster")
    private List<EndAprLine> endAprLines = new ArrayList<>();

    @Override
    public String getId(){
        return id;
    }

    @Override
    public boolean isNew() {
        return getCreatedDate() == null;
    }


    public void addEndStatusMap(EndStatusMap endStatusMap){
        this.endStatusMap = endStatusMap;

        if(endStatusMap.getEndDocumentMaster() != this){
            endStatusMap.confirmMaster(this);
        }
    }

    public void addEndDoc(EndDoc endDoc){
        this.endDoc = endDoc;

        if(endDoc.getEndDocumentMaster() != this){
            endDoc.confirmMaster(this);
        }
    }

    public void addEndAprLine(EndAprLine endAprLine){
        this.endAprLines.add(endAprLine);

        if(endAprLine.getEndDocumentMaster() != this){
            endAprLine.confirmMaster(this);
        }
    }
}
