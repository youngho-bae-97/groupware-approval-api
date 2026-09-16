package com.byh.groupware.domain.approval.repository;

import com.byh.groupware.domain.approval.entity.ActiveDoc;
import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ActiveDocRepository extends JpaRepository<ActiveDoc, Long> {

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
    Page<ActiveDoc> docToDoList(
            Pageable pageable,
            @Param("loginId") String loginId
    );

    @EntityGraph(attributePaths = {"documentMaster", "aprLines"})
    Optional<ActiveDoc> findWithDetailsById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select d
    from ActiveDoc d
    where d.id = :id
    """)
    Optional<ActiveDoc> findByIdForUpdate(@Param("id") Long id);

//    ActiveDoc findByDocId(String docId);

//    @Query(value = "select s from StatusMap s left join s.userMaster m where s.docStatus = '02' and m.loginId = :loginId",
//           countQuery = "select count(s) from StatusMap s")
//    Page<ActiveDoc> docToDoList(Pageable pageable, @Param("loginId") String loginId);
}
