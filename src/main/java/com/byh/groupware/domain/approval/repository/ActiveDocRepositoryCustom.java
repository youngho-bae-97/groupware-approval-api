package com.byh.groupware.domain.approval.repository;

import com.byh.groupware.domain.approval.entity.ActiveDoc;

import java.util.Optional;

public interface ActiveDocRepositoryCustom {

    /**
     * 진행 문서 행에 비관적 락(SELECT ... FOR UPDATE)을 잡고, 락 획득 시점의 최신 값으로 엔티티를 갱신해 반환
     * - 락 대기 중 다른 트랜잭션이 행을 삭제했으면(최종결재 이관) 빈 값 반환
     */
    Optional<ActiveDoc> findByIdForUpdate(Long id);
}
