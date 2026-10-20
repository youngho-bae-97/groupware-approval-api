package com.byh.groupware.domain.approval.service;

import com.byh.groupware.domain.approval.dto.ApprovalDraftRequestDTO;
import com.byh.groupware.domain.approval.dto.ApprovalProcessRequestDTO;
import com.byh.groupware.domain.approval.dto.ApproverInfoDTO;
import com.byh.groupware.domain.approval.entity.ActiveDoc;
import com.byh.groupware.domain.approval.entity.AprLine;
import com.byh.groupware.domain.approval.entity.DocumentMaster;
import com.byh.groupware.domain.approval.repository.DocFormRepository;
import com.byh.groupware.domain.user.dto.UserRegisterDTO;
import com.byh.groupware.domain.user.entity.UserMaster;
import com.byh.groupware.domain.user.repository.UserRepository;
import com.byh.groupware.domain.user.service.JPAUserService;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 일반결재 낙관적 락 테스트
 *
 * 시나리오
 * - 기안자 -> 결재자1 -> 결재자2 결재선 문서에서 결재자1이 결재 요청을 동시에 2번 보냄 (버튼 연타, 재시도 등)
 * - 락이 없으면 두 요청이 모두 검증을 통과해 같은 결재가 중복 처리됨
 * - ActiveDoc 의 @Version + OPTIMISTIC_FORCE_INCREMENT 로 먼저 커밋한 1건만 성공하고 나머지는 충돌 예외
 *
 * 주의
 * - 스레드마다 커밋해야 하므로 @Transactional 을 붙이지 않고 @AfterEach 에서 생성 데이터만 삭제함
 */
@Slf4j
@SpringBootTest
class NormalApprovalOptimisticLockTest {

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
    public void 같은_결재자의_일반결재_동시요청() throws Exception {
        //given
        String runTag = String.valueOf(System.currentTimeMillis() % 1_000_000);
        UserMaster drafter = registerUser("ol" + runTag + "_drafter", "기안자");
        UserMaster approver1 = registerUser("ol" + runTag + "_apr1", "결재자1");
        UserMaster approver2 = registerUser("ol" + runTag + "_apr2", "결재자2");

        createdDocId = draft(drafter, approver1, approver2);
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
                    jpaApprovalService.doProcess(copyOf(request), approver1);
                    successCount.incrementAndGet();
                } catch (Throwable e) {
                    failureTypes.computeIfAbsent(e.getClass().getSimpleName(), k -> new AtomicInteger())
                            .incrementAndGet();
                    log.info("일반결재 실패 - {}: {}", e.getClass().getSimpleName(), e.getMessage());
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
        log.info("일반결재 결과 - 성공: {}, 실패 유형: {}", successCount.get(), failureTypes);
        assertThat(successCount.get()).as("같은 결재는 한 번만 처리되어야 함").isEqualTo(1);

        ActiveDoc activeDoc = em.createQuery(
                        "select d from ActiveDoc d join fetch d.aprLines where d.id = :id", ActiveDoc.class)
                .setParameter("id", request.getActiveDocId())
                .getSingleResult();
        List<String> statuses = activeDoc.getAprLines().stream()
                .sorted(Comparator.comparing(AprLine::getStepSeq))
                .map(AprLine::getApproveStatus)
                .toList();

        // 기안자 완료, 결재자1 완료, 결재자2 진행중
        assertThat(statuses).containsExactly("03", "03", "02");
        // 성공한 1건만 version 을 올림
        assertThat(activeDoc.getVersion()).isEqualTo(1L);
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
     * 기안자(1단계) -> 결재자1(2단계) -> 결재자2(3단계) 결재선으로 기안하고, 채번된 문서번호를 반환
     */
    private String draft(UserMaster drafter, UserMaster approver1, UserMaster approver2) {
        ApprovalDraftRequestDTO dto = new ApprovalDraftRequestDTO();
        dto.setDocFormId(docFormRepository.findAll().get(0).getId());
        dto.setDocTitle("낙관적 락 테스트 문서");
        dto.setDocContent("<p>낙관적 락 테스트 본문</p>");
        dto.setApprovalLines(List.of(
                createLine(1, drafter, "01"),
                createLine(2, approver1, "02"),
                createLine(3, approver2, "02")
        ));

        jpaApprovalService.draft(dto, drafter);
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
