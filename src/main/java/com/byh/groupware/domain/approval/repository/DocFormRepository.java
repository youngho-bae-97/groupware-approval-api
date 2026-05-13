package com.byh.groupware.domain.approval.repository;

import com.byh.groupware.domain.approval.entity.DocForm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocFormRepository extends JpaRepository<DocForm,String> {

}
