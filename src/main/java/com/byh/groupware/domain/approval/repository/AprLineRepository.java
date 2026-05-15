package com.byh.groupware.domain.approval.repository;

import com.byh.groupware.domain.approval.entity.AprLine;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface AprLineRepository extends JpaRepository<AprLine, Long> {

    @Query("SELECT COUNT(a) FROM AprLine a " +
            "WHERE a.documentMaster.id = :docId " +
            "AND a.userMaster.loginId = :processorId " +
            "AND a.approveStatus = :status " +
            "AND a.approveType IN :types")
    long countApproverLine(
            @Param("docId") String docId,
            @Param("processorId") String processorId,
            @Param("status") String status,
            @Param("types") List<String> types
    );

    @Query("SELECT COUNT(a) FROM AprLine a " +
            "WHERE a.documentMaster.id = :docId " +
            "AND a.approveType = '02' " +
            "AND a.stepSeq > (" +
            "    SELECT a2.stepSeq FROM AprLine a2 " +
            "    WHERE a2.documentMaster.id = :docId " +
            "    AND a2.userMaster.loginId = :processorId" +
            ")")
    long countRemainingApprovers(@Param("docId") String docId,
                                 @Param("processorId") String processorId);

    @Query("SELECT a FROM AprLine a " +
            "WHERE a.documentMaster.id = :docId " +
            "AND a.userMaster.loginId = :processorId " +
            "AND a.approveStatus = '02'")
    Slice<AprLine> findFinalAprLineById(@Param("docId") String docId, @Param("processorId") String processorId, Pageable pageable);

//    @Query("SELECT a FROM AprLine a " +
//            "WHERE a.documentMaster.id = :docId " +
//            "AND a.stepSeq > (" +
//            "    SELECT a2.stepSeq FROM AprLine a2 " +
//            "    WHERE a2.documentMaster.id = :docId " +
//            "    AND a2.userMaster.loginId = :processorId" +
//            ") " +
//            "AND a.approveStatus = '01' " +
//            "AND a.approveType <> '09' " +
//            "ORDER BY a.stepSeq ASC")
//    Page<AprLine> findNextAprLineById(@Param("docId") String docId, @Param("processorId") String processorId, Pageable pageable);

    @Query("""
    SELECT a
    FROM AprLine a
    WHERE a.documentMaster.id = :docId
    AND a.stepSeq > (
        SELECT MAX(a2.stepSeq)
        FROM AprLine a2
        WHERE a2.documentMaster.id = :docId
        AND a2.userMaster.loginId = :processorId
    )
    AND a.approveStatus = '01'
    AND a.approveType <> '09'
    ORDER BY a.stepSeq ASC
""")
    Slice<AprLine> findNextAprLineById(
            @Param("docId") String docId,
            @Param("processorId") String processorId,
            Pageable pageable
    );

    @Query("SELECT a FROM AprLine a " +
            "WHERE a.documentMaster.id = :docId " +
            "AND a.userMaster.loginId = :processorId " +
            "AND a.approveStatus = '02'")
    AprLine findCurrAprLine(String docId, String processorId);
}
