package com.byh.groupware.domain.approval.repository;

import com.byh.groupware.domain.approval.entity.NoMaster;
import com.byh.groupware.domain.approval.entity.NoMasterId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface NoMasterRepository extends JpaRepository<NoMaster,NoMasterId> {

    Long countById(NoMasterId id);

    @Lock(LockModeType.PESSIMISTIC_READ)
    NoMaster findLockById(NoMasterId id);
}
