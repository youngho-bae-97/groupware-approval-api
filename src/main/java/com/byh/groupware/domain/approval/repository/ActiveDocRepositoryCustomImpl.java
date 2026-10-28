package com.byh.groupware.domain.approval.repository;

import com.byh.groupware.domain.approval.entity.ActiveDoc;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class ActiveDocRepositoryCustomImpl implements ActiveDocRepositoryCustom {

    private final EntityManager em;

    /**
     * @Lock(PESSIMISTIC_WRITE) 쿼리를 쓰지 않는 이유
     * - 같은 트랜잭션에서 검증용으로 이미 조회한 엔티티가 영속성 컨텍스트에 있으면,
     *   FOR UPDATE 쿼리는 나가지만 반환되는 인스턴스의 값은 락 이전에 읽은 값 그대로임
     * - 그래서 락 대기 중 다른 트랜잭션이 상태만 바꾸고 커밋하면(반려 등) 재검증이 이를 보지 못함
     * - refresh(PESSIMISTIC_WRITE)는 FOR UPDATE 로 다시 읽어 최신 값으로 덮어씀
     *   (REPEATABLE READ 에서 락 없는 refresh 는 트랜잭션 스냅샷을 다시 읽으므로 최신 값이 아님)
     */
    @Override
    public Optional<ActiveDoc> findByIdForUpdate(Long id) {
        ActiveDoc activeDoc = em.find(ActiveDoc.class, id);
        if (activeDoc == null) {
            return Optional.empty();
        }

        try {
            em.refresh(activeDoc, LockModeType.PESSIMISTIC_WRITE);
        } catch (EntityNotFoundException e) {
            // 스냅샷에는 있었지만 락 대기 중 다른 트랜잭션이 삭제하고 커밋함
            return Optional.empty();
        }
        return Optional.of(activeDoc);
    }
}
