package com.byh.groupware.domain.approval.repository;

import com.byh.groupware.domain.approval.entity.EndDoc;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EndDocRepository extends JpaRepository<EndDoc, Long> {

    @EntityGraph(attributePaths = {"documentMaster", "endAprLines"})
    Optional<EndDoc> findWithDetailsByDocumentMasterId(Long documentMasterId);
}
