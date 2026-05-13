package com.byh.groupware.domain.company.repository;

import com.byh.groupware.domain.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company,Long> {
    Company findByCoCode(String co001);
}
