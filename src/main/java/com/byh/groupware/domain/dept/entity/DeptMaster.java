package com.byh.groupware.domain.dept.entity;

import com.byh.groupware.domain.company.entity.Company;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tbl_dept_master")
public class DeptMaster {

    @Id @GeneratedValue
    @Column(name = "dept_id")
    private Long id;
    private String deptCode;
    private String deptName;
    private String deptDirect;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "co_id")
    private Company company;

    public DeptMaster(String deptCode, String deptName, Company company){
        this.deptCode = deptCode;
        this.deptName = deptName;
        this.company = company;
    }
}
