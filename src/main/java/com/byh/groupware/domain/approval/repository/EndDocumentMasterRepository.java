package com.byh.groupware.domain.approval.repository;

import com.byh.groupware.domain.approval.entity.EndDoc;
import com.byh.groupware.domain.approval.entity.EndDocumentMaster;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EndDocumentMasterRepository extends JpaRepository<EndDocumentMaster, String> {

    @EntityGraph(attributePaths = {"endDoc", "endStatusMap", "aprLines"})
    Optional<EndDocumentMaster> findWithDetailsById(String id);

}
