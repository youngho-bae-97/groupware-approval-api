package com.byh.groupware.domain.approval.repository;

import com.byh.groupware.domain.approval.entity.DocumentMaster;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentMasterRepository extends JpaRepository<DocumentMaster,Long> {
//    @EntityGraph(attributePaths = {"activeDoc", "statusMap", "aprLines"})
//    Optional<DocumentMaster> findWithDetailsById(String id);

    @EntityGraph(attributePaths = {"activeDoc", "activeDoc.aprLines", "userMaster", "docForm"})
    DocumentMaster findFetchedDocumentMasterById(Long id);
}
