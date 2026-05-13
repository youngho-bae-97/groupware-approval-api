package com.byh.groupware.domain.dept.repository;

import com.byh.groupware.domain.dept.entity.DeptMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeptRepository extends JpaRepository<DeptMaster,Long> {

    DeptMaster findByDeptCode(String deptCode);
}
