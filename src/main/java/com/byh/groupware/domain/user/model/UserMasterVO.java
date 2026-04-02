package com.byh.groupware.domain.user.model;

import lombok.Data;

@Data
public class UserMasterVO {
    private String memId;
    private String memName;
    private String memPass;
    private String jobGrade;
    private String memEmail;
    private String edu;
    private String tel;
    private String profileUrl;
    private String memSign;
    private String memSecpass;
    private String memEnName;
    private String memDeptTel;
    private java.sql.Date memBirth; // 날짜만 필요할 땐 sql.Date
    private String memWorktype;
    private String memIntro;
    private String zipcode;
    private String memAddr;
    private String memAddrDetail;
    private String deptCode;
    private String coCode;
    private String useYn;
}
