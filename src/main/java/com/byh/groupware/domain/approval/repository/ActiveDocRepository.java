package com.byh.groupware.domain.approval.repository;

import com.byh.groupware.domain.approval.entity.ActiveDoc;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ActiveDocRepository extends JpaRepository<ActiveDoc, Long>, ActiveDocRepositoryCustom {

    // 결재대기함(TODO): 내 결재선이 진행중(approveStatus 02 = 현재 내 차례)인 진행 문서
    // 한 문서에서 현재 차례인 결재선은 한 줄이므로 distinct 불필요
    @Query(
            value = """
        select d
        from ActiveDoc d
        join d.aprLines filterLine
        where d.docStatus = '02'
          and filterLine.approveStatus = '02'
          and filterLine.userMaster.loginId = :loginId
        """,
            countQuery = """
        select count(d)
        from ActiveDoc d
        join d.aprLines filterLine
        where d.docStatus = '02'
          and filterLine.approveStatus = '02'
          and filterLine.userMaster.loginId = :loginId
        """
    )
    Page<ActiveDoc> docToDoList(
            Pageable pageable,
            @Param("loginId") String loginId
    );

    // 기안함(DRAFT): 내가 기안한(stepSeq 1 = 기안자) 진행 문서
    @Query(
            value = """
        select distinct d
        from ActiveDoc d
        join d.aprLines filterLine
        where d.docStatus = '02'
          and filterLine.stepSeq = 1
          and filterLine.userMaster.loginId = :loginId
        """,
            countQuery = """
        select count(distinct d.id)
        from ActiveDoc d
        join d.aprLines filterLine
        where d.docStatus = '02'
          and filterLine.stepSeq = 1
          and filterLine.userMaster.loginId = :loginId
        """
    )
    Page<ActiveDoc> docDraftList(
            Pageable pageable,
            @Param("loginId") String loginId
    );

    // API 의 문서 식별자는 진행/완료 구분 없는 문서 마스터 ID 이므로, 마스터 ID 로 진행 문서를 찾음
    @EntityGraph(attributePaths = {"documentMaster", "aprLines"})
    Optional<ActiveDoc> findWithDetailsByDocumentMasterId(Long documentMasterId);

    // 엔티티를 영속성 컨텍스트에 올리지 않도록 ID 만 조회 (결재 처리 시 락 재검증에 영향 주지 않음)
    @Query("select d.id from ActiveDoc d where d.documentMaster.id = :documentMasterId")
    Optional<Long> findIdByDocumentMasterId(@Param("documentMasterId") Long documentMasterId);

    // 최종결재 시 hot 데이터 제거용 벌크 삭제 (결재선 삭제 후 호출)
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ActiveDoc d where d.id = :id")
    int deleteActiveDocById(@Param("id") Long id);

//    ActiveDoc findByDocId(String docId);

//    @Query(value = "select s from StatusMap s left join s.userMaster m where s.docStatus = '02' and m.loginId = :loginId",
//           countQuery = "select count(s) from StatusMap s")
//    Page<ActiveDoc> docToDoList(Pageable pageable, @Param("loginId") String loginId);
}
