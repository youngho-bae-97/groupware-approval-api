package com.byh.groupware.domain.approval.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@AllArgsConstructor
public class NoMasterId implements Serializable {

    @Column(name = "YEAR")
    private String year; // CHAR(4)

    @Column(name = "APPROVAL_FORM_ID")
    private String approvalFormId; // FK이자 PK의 일부
}
