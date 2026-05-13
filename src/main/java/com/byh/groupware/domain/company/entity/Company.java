package com.byh.groupware.domain.company.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tbl_company")
public class Company {
    @Id @GeneratedValue
    @Column(name = "co_id")
    private Long id;

    private String coCode;
    private String coName;
    private String coCeo;
    private String coTel;
    private String coLogoUrl;
    private String coWorktype;
    private String coZipcode;
    private String coAddr;
    private String coAddrDetail;
    private String coBuyService;
    private Integer coSubsPeriod;
    private Integer coStorageSize;
    private Integer coUserSize;

    public Company(String coCode, String coName, String coCeo, String coTel){
        this.coCode = coCode;
        this.coName = coName;
        this.coCeo = coCeo;
        this.coTel = coTel;
    }
}
