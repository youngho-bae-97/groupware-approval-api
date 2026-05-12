package com.byh.groupware.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tbl_user_master")
public class UserMaster {

    @Id
    @GeneratedValue
    @Column(name = "mem_id")
    private Long id;

    private String loginId;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private String deptCode;

    private String useYn;

    private String deptName;
}
