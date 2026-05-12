package com.byh.groupware.domain.approval.entity;

import com.byh.groupware.domain.user.entity.UserMaster;
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

    @Override
    public String getId(){
        return id;
    }

    @Override
    public boolean isNew() {
        return getCreatedDate() == null;
    }
}