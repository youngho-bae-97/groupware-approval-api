package com.byh.groupware.domain.approval.repository;

import com.byh.groupware.domain.approval.entity.StatusMap;
import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotBlank;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface StatusMapRepository extends JpaRepository<StatusMap,String> {

    @Query(value = "select s from StatusMap s left join s.userMaster m where s.docStatus = '02' and m.loginId = :loginId",
           countQuery = "select count(s) from StatusMap s")
    Page<StatusMap> docToDoList(Pageable pageable, @Param("loginId") String loginId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select s
        from StatusMap s
        where s.id = :docId
    """)
    StatusMap findForFinalApprove(
            @Param("docId") String docId
    );
}
