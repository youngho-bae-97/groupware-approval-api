package com.byh.groupware.domain.approval.repository;

import com.byh.groupware.domain.approval.entity.EndDoc;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EndDocRepository extends JpaRepository<EndDoc, String> {


}
