package com.byh.groupware.domain.approval.entity;

import com.byh.groupware.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "tbl_no_master",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_no_master_year_docform",
                        columnNames = {"year", "docform_id"}
                )
        }
)
public class NoMaster extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "no_master_id")
    private Long id;


    private String year;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docform_id")
    private DocForm docForm;

    private Integer lastSeq;        // 마지막 발행 번호

    private NoMaster(DocForm docForm, String year) {
        this.docForm = docForm;
//        this.id = new NoMasterId(year, docForm.getId());
        this.year = year;
        this.lastSeq = 0;
    }

    public static NoMaster createDocNo(String currentYear, DocForm docForm) {
        return new NoMaster(docForm,currentYear);
    }

//    @Override
//    public NoMasterId getId() {
//        return id;
//    }
//
//    @Override
//    public boolean isNew() {
//        return getCreatedDate() == null;
//    }


    public Integer updateSeq() {
        this.lastSeq += 1;
        return this.lastSeq;
    }
}
