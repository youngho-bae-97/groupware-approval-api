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
@Table(name = "tbl_no_master")
public class NoMaster extends BaseEntity implements Persistable<NoMasterId> {

    @EmbeddedId
    private NoMasterId id;

    @MapsId("approvalFormId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APPROVAL_FORM_ID")
    private DocForm docForm;

    private Integer lastSeq;        // 마지막 발행 번호

    public NoMaster(DocForm docForm, String year) {
        this.docForm = docForm;
        this.id = new NoMasterId(year, docForm.getId());
        this.lastSeq = 0;
    }

    @Override
    public NoMasterId getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return getCreatedDate() == null;
    }

    // 일련번호 증가 로직 (도메인 메서드)
    public void incrementSeq() {
        this.lastSeq++;
    }
}
