package com.byh.groupware.global.config;

import com.byh.groupware.domain.approval.entity.DocForm;
import com.byh.groupware.domain.approval.repository.DocFormRepository;
import com.byh.groupware.domain.company.entity.Company;
import com.byh.groupware.domain.company.repository.CompanyRepository;
import com.byh.groupware.domain.dept.entity.DeptMaster;
import com.byh.groupware.domain.dept.repository.DeptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitConfig implements CommandLineRunner {

    private final CompanyRepository companyRepository;
    private final DeptRepository deptRepository;
    private final DocFormRepository docFormRepository;


    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 이미 데이터가 있는지 확인 후 없으면 삽입 (중복 방지)
        Company company;
        if (companyRepository.count() == 0) {
            company = companyRepository.save(new Company("CO001", "(주)코딩파크","김영호","02-1234-5678"));
        }else{
            company = companyRepository.findByCoCode("CO001");
        }

        if (deptRepository.count() == 0) {
            deptRepository.save(new DeptMaster("DEPT01", "백엔드개발팀", company));
            deptRepository.save(new DeptMaster("DEPT02", "인사총무팀", company));
        }

        if(docFormRepository.count() == 0){
            docFormRepository.save(new DocForm("FORM-01","연차신청서","Y"));
            docFormRepository.save(new DocForm("FORM-02","지출결의서","Y"));
        }
    }
}
