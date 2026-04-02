package com.byh.groupware.domain.company.model;

import lombok.Data;

@Data
public class CompanyVO {
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
}
