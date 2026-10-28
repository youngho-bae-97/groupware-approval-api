package com.byh.groupware.domain.approval.service;

import com.byh.groupware.domain.approval.dto.ApprovalDraftRequestDTO;
import com.byh.groupware.domain.approval.dto.ApprovalProcessRequestDTO;
import com.byh.groupware.domain.approval.dto.ApproverInfoDTO;
import com.byh.groupware.domain.approval.entity.ActiveDoc;
import com.byh.groupware.domain.approval.entity.DocumentMaster;
import com.byh.groupware.domain.approval.entity.EndDoc;
import com.byh.groupware.domain.approval.exception.DocStatusMissMatchException;
import com.byh.groupware.domain.approval.repository.DocFormRepository;
import com.byh.groupware.domain.user.dto.UserRegisterDTO;
import com.byh.groupware.domain.user.entity.UserMaster;
import com.byh.groupware.domain.user.repository.UserRepository;
import com.byh.groupware.domain.user.service.JPAUserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 최종결재 비관적 락 테스트
 *
 * 시나리오
 * - 기안자 -> 결재자 결재선 문서에서 최종결재자가 결재 요청을 동시에 2번 보냄 (버튼 연타, 재시도 등)
 * - 락이 없으면 두 요청이 모두 검증을 통과해 완료 문서가 중복으로 이관됨
 * - findByIdForUpdate(PESSIMISTIC_WRITE)로 먼저 락을 잡은 1건만 이관하고,
 *   나머지는 락 대기 후 진행 문서가 삭제된 것을 확인해 "이미 처리된 문서" 예외로 끝나야 함
 *
 * 주의
 * - 스레드마다 커밋해야 하므로 @Transactional 을 붙이지 않고 @AfterEach 에서 생성 데이터만 삭제함
 */
@Slf4j
@SpringBootTest
class FinalApprovalPessimisticLockTest {

    private static final int REQUEST_COUNT = 2;

    @Autowired
    private JpaApprovalService jpaApprovalService;

    @Autowired
    private JPAUserService jpaUserService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocFormRepository docFormRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private TransactionTemplate transactionTemplate;

    // 정리 대상 (이번 실행에서 생성한 데이터)
    private final List<String> createdLoginIds = new ArrayList<>();
    private String createdDocId;

    @AfterEach
    void cleanUp() {
        transactionTemplate.executeWithoutResult(status -> {
            if (createdDocId != null) {
                // cold 데이터 (최종결재 성공분)
                em.createQuery("""
                        delete from EndAprLine e
                        where e.endDoc.id in (
                            select d.id from EndDoc d
                            where d.documentMaster.id in (
                                select m.id from DocumentMaster m where m.docId = :docId))
                        """).setParameter("docId", createdDocId).executeUpdate();
                em.createQuery("""
                        delete from EndDoc d
                        where d.documentMaster.id in (
                            select m.id from DocumentMaster m where m.docId = :docId)
                        """).setParameter("docId", createdDocId).executeUpdate();

                // hot 데이터 (테스트 실패로 이관되지 않은 경우)
                em.createQuery("""
                        delete from AprLine a
                        where a.activeDoc.id in (
                            select d.id from ActiveDoc d
                            where d.documentMaster.id in (
                                select m.id from DocumentMaster m where m.docId = :docId))
                        """).setParameter("docId", createdDocId).executeUpdate();
                em.createQuery("""
                        delete from ActiveDoc d
                        where d.documentMaster.id in (
                            select m.id from DocumentMaster m where m.docId = :docId)
                        """).setParameter("docId", createdDocId).executeUpdate();
                em.createQuery("delete from DocumentMaster m where m.docId = :docId")
                        .setParameter("docId", createdDocId).executeUpdate();
            }

            if (!createdLoginIds.isEmpty()) {
                em.createQuery("delete from UserMaster u where u.loginId in :loginIds")
                        .setParameter("loginIds", createdLoginIds).executeUpdate();
            }
        });
    }

    @Test
    public void 같은_결재자의_최종결재_동시요청() throws Exception {
        //given
        String runTag = String.valueOf(System.currentTimeMillis() % 1_000_000);
        UserMaster drafter = registerUser("pl" + runTag + "_drafter", "기안자");
        UserMaster approver = registerUser("pl" + runTag + "_apr", "결재자");

        createdDocId = draft(drafter, approver);
        ApprovalProcessRequestDTO request = createProcessRequest(createdDocId);

        ExecutorService executor = Executors.newFixedThreadPool(REQUEST_COUNT);
        CountDownLatch ready = new CountDownLatch(REQUEST_COUNT);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(REQUEST_COUNT);

        AtomicInteger successCount = new AtomicInteger();
        ConcurrentHashMap<String, AtomicInteger> failureTypes = new ConcurrentHashMap<>();

        //when
        for (int i = 0; i < REQUEST_COUNT; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    jpaApprovalService.doProcess(copyOf(request), approver.getLoginId());
                    successCount.incrementAndGet();
                } catch (Throwable e) {
                    failureTypes.computeIfAbsent(e.getClass().getSimpleName(), k -> new AtomicInteger())
                            .incrementAndGet();
                    log.info("최종결재 실패 - {}: {}", e.getClass().getSimpleName(), e.getMessage());
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown(); // 두 요청 동시 출발
        done.await(1, TimeUnit.MINUTES);
        executor.shutdown();

        //then
        log.info("최종결재 결과 - 성공: {}, 실패 유형: {}", successCount.get(), failureTypes);
        assertThat(successCount.get()).as("같은 최종결재는 한 번만 처리되어야 함").isEqualTo(1);
        // 나머지 1건은 500(NoSuchElementException 등)이 아니라 "이미 처리된 문서" 예외로 끝나야 함
        assertThat(failureTypes).containsOnlyKeys(DocStatusMissMatchException.class.getSimpleName());

        // 완료 문서는 1건만 이관되고 진행 문서는 삭제됨
        List<EndDoc> endDocs = em.createQuery(
                        "select e from EndDoc e join fetch e.endAprLines where e.documentMaster.id = :id", EndDoc.class)
                .setParameter("id", request.getDocMasterId())
                .getResultList();
        assertThat(endDocs).hasSize(1);
        assertThat(endDocs.get(0).getEndAprLines()).hasSize(2);

        Long activeDocCount = em.createQuery("select count(d) from ActiveDoc d where d.id = :id", Long.class)
                .setParameter("id", request.getActiveDocId())
                .getSingleResult();
        assertThat(activeDocCount).isZero();
    }

    /**
     * 반려처럼 진행 문서를 삭제하지 않고 상태만 바꾸는 트랜잭션과 최종결재가 경쟁하는 경우
     * - 반려 기능은 아직 없으므로, 문서 행에 락을 잡고 docStatus 를 04 로 바꾸는 트랜잭션으로 대신함
     * - 최종결재는 검증 시점에는 진행중(02)으로 보지만, 락 대기 후에는 반려(04)된 최신 상태로 재검증해야 함
     */
    @Test
    public void 최종결재_락_대기중_반려가_먼저_커밋되면_이관하지_않음() throws Exception {
        //given
        String runTag = String.valueOf(System.currentTimeMillis() % 1_000_000);
        UserMaster drafter = registerUser("pr" + runTag + "_drafter", "기안자");
        UserMaster approver = registerUser("pr" + runTag + "_apr", "결재자");

        createdDocId = draft(drafter, approver);
        ApprovalProcessRequestDTO request = createProcessRequest(createdDocId);

        CountDownLatch rejectLocked = new CountDownLatch(1);
        CountDownLatch finalStarted = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // 반려 트랜잭션: 문서 행 락 -> 상태 04 변경 -> 최종결재가 락 대기에 들어갈 때까지 커밋을 미룸
        Future<?> reject = executor.submit(() -> transactionTemplate.executeWithoutResult(status -> {
            em.createQuery("select d from ActiveDoc d where d.id = :id", ActiveDoc.class)
                    .setParameter("id", request.getActiveDocId())
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .getSingleResult();
            em.createQuery("update ActiveDoc d set d.docStatus = '04', d.version = d.version + 1 where d.id = :id")
                    .setParameter("id", request.getActiveDocId())
                    .executeUpdate();
            rejectLocked.countDown();
            try {
                finalStarted.await();
                Thread.sleep(500); // 최종결재가 FOR UPDATE 에서 대기하도록 여유를 둠
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));

        //when
        rejectLocked.await();
        finalStarted.countDown();
        Throwable thrown = null;
        try {
            jpaApprovalService.doProcess(copyOf(request), approver.getLoginId());
        } catch (Throwable e) {
            thrown = e;
            log.info("최종결재 실패 - {}: {}", e.getClass().getSimpleName(), e.getMessage());
        }
        reject.get(1, TimeUnit.MINUTES);
        executor.shutdown();

        //then
        assertThat(thrown).as("반려된 문서는 최종결재되면 안 됨").isInstanceOf(DocStatusMissMatchException.class);

        // 진행 문서는 반려 상태로 남고, 완료 문서로 이관되지 않음
        String docStatus = em.createQuery("select d.docStatus from ActiveDoc d where d.id = :id", String.class)
                .setParameter("id", request.getActiveDocId())
                .getSingleResult();
        assertThat(docStatus).isEqualTo("04");

        Long endDocCount = em.createQuery("select count(e) from EndDoc e where e.documentMaster.id = :id", Long.class)
                .setParameter("id", request.getDocMasterId())
                .getSingleResult();
        assertThat(endDocCount).isZero();
    }

    private UserMaster registerUser(String loginId, String name) {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setMemId(loginId);
        dto.setMemPass("1234");
        dto.setMemName(name);
        dto.setDeptCode("DEPT01");
        dto.setCoCode("CO001");
        jpaUserService.register(dto);
        createdLoginIds.add(loginId);
        return userRepository.findByLoginId(loginId);
    }

    /**
     * 기안자(1단계) -> 결재자(2단계, 최종결재자) 결재선으로 기안하고, 채번된 문서번호를 반환
     */
    private String draft(UserMaster drafter, UserMaster approver) {
        ApprovalDraftRequestDTO dto = new ApprovalDraftRequestDTO();
        dto.setDocFormId(docFormRepository.findAll().get(0).getId());
        dto.setDocTitle("비관적 락 테스트 문서");
        dto.setDocContent("<p>비관적 락 테스트 본문</p>");
        dto.setApprovalLines(List.of(
                createLine(1, drafter, "01"),
                createLine(2, approver, "02")
        ));

        jpaApprovalService.draft(dto, drafter.getLoginId());
        return dto.getDocId();
    }

    private ApproverInfoDTO createLine(int stepSeq, UserMaster user, String approveType) {
        ApproverInfoDTO line = new ApproverInfoDTO();
        line.setStepSeq(stepSeq);
        line.setApproverId(user.getLoginId());
        line.setApproverName(user.getMemName());
        line.setApproveType(approveType);
        return line;
    }

    private ApprovalProcessRequestDTO createProcessRequest(String docId) {
        DocumentMaster documentMaster = em.createQuery(
                        "select d from DocumentMaster d join fetch d.activeDoc where d.docId = :docId",
                        DocumentMaster.class)
                .setParameter("docId", docId)
                .getSingleResult();

        ApprovalProcessRequestDTO dto = new ApprovalProcessRequestDTO();
        dto.setDocMasterId(documentMaster.getId());
        dto.setActiveDocId(documentMaster.getActiveDoc().getId());
        dto.setApproveType("APPROVE");
        return dto;
    }

    // doProcess 가 DTO 에 값을 세팅하므로 스레드마다 별도 인스턴스 사용
    private ApprovalProcessRequestDTO copyOf(ApprovalProcessRequestDTO source) {
        ApprovalProcessRequestDTO dto = new ApprovalProcessRequestDTO();
        dto.setDocMasterId(source.getDocMasterId());
        dto.setActiveDocId(source.getActiveDocId());
        dto.setApproveType(source.getApproveType());
        return dto;
    }
}
