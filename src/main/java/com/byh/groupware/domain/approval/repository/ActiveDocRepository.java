package com.byh.groupware.domain.approval.repository;

import com.byh.groupware.domain.approval.entity.ActiveDoc;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActiveDocRepository extends JpaRepository<ActiveDoc, String> {

}
