package com.byh.groupware.domain.approval.repository;

import com.byh.groupware.domain.approval.entity.NoMaster;
//import com.byh.groupware.domain.approval.entity.NoMasterId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoMasterRepository extends JpaRepository<NoMaster,Long> {


    Long countByYearAndDocForm_Id(String year, Long docFormId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select n
    from NoMaster n
    where n.year = :year
      and n.docForm.id = :docFormId
    """)
    NoMaster findLockByYearAndDocFormId(
            @Param("year") String year,
            @Param("docFormId") Long docFormId
    );
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    NoMaster findLockById(String year,Long docFormId);
}
