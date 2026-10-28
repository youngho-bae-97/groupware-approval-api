package com.byh.groupware.domain.approval.service;

import com.byh.groupware.domain.approval.dto.ApprovalDraftRequestDTO;
import com.byh.groupware.domain.approval.dto.ApprovalProcessRequestDTO;
import com.byh.groupware.domain.approval.dto.ApproverInfoDTO;
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
import org.springframework.core.NestedExceptionUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 서로 다른 문서에 대한 최종결재 동시 실행 시 데드락 재현 테스트
 *
 * 시나리오
 * - 각 트랜잭션은 자기 결재선 행을 UPDATE(X락)한 뒤 hot 데이터를 벌크 삭제함
 * - tbl_aprline.activedoc_id 에 인덱스가 없으면 DELETE가 풀 스캔을 하며
 *   다른 트랜잭션이 잡고 있는 결재선 행까지 next-key lock 을 걸려고 해서 서로 대기 -> 데드락
 *
 * 주의
 * - @Transactional 을 붙이지 않음
 *   테스트 트랜잭션 안에서 기안하면 커밋 전이라 다른 스레드(별도 커넥션)가 문서를 볼 수 없고,
 *   락 경합도 실제 운영과 달라져 데드락이 재현되지 않음
 *   대신 @AfterEach 에서 이번 실행에서 만든 데이터만 골라 삭제함 (채번 테이블의 시퀀스 증가분은 남음)
 * - tbl_aprline.activedoc_id 인덱스(idx_aprline_activedoc_id)가 없으면 실패함 -> 인덱스 누락 회귀 방지용
 * - 인덱스 추가 전 InnoDB 데드락 로그: docs/deadlock/innodb-deadlock-before-index.txt
 * - 동시 스레드 수가 운영 최대치와 같으므로 커넥션 풀 크기 산정 검증도 겸함
 */
@Slf4j
@SpringBootTest
class FinalApprovalDeadlockTest {

    // 운영 최대 동시성(server.tomcat.threads.max)과 동일하게 맞춤
    // -> 커넥션 풀 크기(hikari.maximum-pool-size)가 부족하면 커넥션 고갈로 실패함
    private static final int DOC_COUNT = 20;

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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 정리 대상 (이번 실행에서 생성한 데이터)
    private final List<String> createdLoginIds = new ArrayList<>();
    private final List<String> createdDocIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        transactionTemplate.executeWithoutResult(status -> {
            if (!createdDocIds.isEmpty()) {
                // cold 데이터 (최종결재 성공분)
                em.createQuery("""
                        delete from EndAprLine e
                        where e.endDoc.id in (
                            select d.id from EndDoc d
                            where d.documentMaster.id in (
                                select m.id from DocumentMaster m where m.docId in :docIds))
                        """).setParameter("docIds", createdDocIds).executeUpdate();
                em.createQuery("""
                        delete from EndDoc d
                        where d.documentMaster.id in (
                            select m.id from DocumentMaster m where m.docId in :docIds)
                        """).setParameter("docIds", createdDocIds).executeUpdate();

                // hot 데이터 (최종결재 실패분)
                em.createQuery("""
                        delete from AprLine a
                        where a.activeDoc.id in (
                            select d.id from ActiveDoc d
                            where d.documentMaster.id in (
                                select m.id from DocumentMaster m where m.docId in :docIds))
                        """).setParameter("docIds", createdDocIds).executeUpdate();
                em.createQuery("""
                        delete from ActiveDoc d
                        where d.documentMaster.id in (
                            select m.id from DocumentMaster m where m.docId in :docIds)
                        """).setParameter("docIds", createdDocIds).executeUpdate();
                em.createQuery("delete from DocumentMaster m where m.docId in :docIds")
                        .setParameter("docIds", createdDocIds).executeUpdate();
            }

            if (!createdLoginIds.isEmpty()) {
                em.createQuery("delete from UserMaster u where u.loginId in :loginIds")
                        .setParameter("loginIds", createdLoginIds).executeUpdate();
            }
        });
    }

    @Test
    public void 서로_다른_문서_최종결재_동시실행() throws Exception {
        //given
        // 실행할 때마다 사용자 ID가 겹치지 않도록 실행 태그 부여
        String runTag = String.valueOf(System.currentTimeMillis() % 1_000_000);
        Long docFormId = docFormRepository.findAll().get(0).getId();

        String drafterId = "dl" + runTag + "_drafter";
        registerUser(drafterId, "기안자");
        UserMaster drafter = userRepository.findByLoginId(drafterId);

        // 문서마다 결재자 1명(최종결재자)만 지정 -> 결재자의 결재가 곧 최종결재
        List<ApprovalProcessRequestDTO> requests = new ArrayList<>();
        List<UserMaster> approvers = new ArrayList<>();
        for (int i = 0; i < DOC_COUNT; i++) {
            String approverId = "dl" + runTag + "_apr" + i;
            registerUser(approverId, "결재자" + i);
            UserMaster approver = userRepository.findByLoginId(approverId);

            String docId = draft(drafter, approver, docFormId, i);
            requests.add(createProcessRequest(docId));
            approvers.add(approver);
        }

        ExecutorService executor = Executors.newFixedThreadPool(DOC_COUNT);
        CountDownLatch ready = new CountDownLatch(DOC_COUNT);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(DOC_COUNT);

        AtomicInteger successCount = new AtomicInteger();
        List<Throwable> failures = new ArrayList<>();
        ConcurrentHashMap<String, AtomicInteger> failureTypes = new ConcurrentHashMap<>();

        //when
        for (int i = 0; i < DOC_COUNT; i++) {
            ApprovalProcessRequestDTO request = requests.get(i);
            UserMaster approver = approvers.get(i);

            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    jpaApprovalService.doProcess(request, approver);
                    successCount.incrementAndGet();
                } catch (Throwable e) {
                    synchronized (failures) {
                        failures.add(e);
                    }
                    failureTypes.computeIfAbsent(e.getClass().getSimpleName(), k -> new AtomicInteger())
                            .incrementAndGet();
                    log.error("최종결재 실패 - activeDocId: {}", request.getActiveDocId(), e);
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown(); // 모든 스레드 동시 출발
        done.await(2, TimeUnit.MINUTES);
        executor.shutdown();

        //then
        logLatestDeadlock();
        log.info("최종결재 결과 - 성공: {}, 실패: {}, 실패 유형: {}", successCount.get(), failures.size(), failureTypes);
        assertThat(failures).as("서로 다른 문서의 최종결재는 서로 영향을 주지 않아야 함").isEmpty();
        assertThat(successCount.get()).isEqualTo(DOC_COUNT);
    }

    /**
     * InnoDB 가 마지막으로 감지한 데드락 정보 출력 (PROCESS 권한 필요)
     */
    private void logLatestDeadlock() {
        try {
            String status = jdbcTemplate.queryForObject("SHOW ENGINE INNODB STATUS", (rs, rowNum) -> rs.getString("Status"));
            int begin = status.indexOf("LATEST DETECTED DEADLOCK");
            if (begin < 0) {
                log.info("감지된 데드락 없음");
                return;
            }
            int end = status.indexOf("------------\nTRANSACTIONS", begin);
            log.info("InnoDB 데드락 로그\n{}", end < 0 ? status.substring(begin) : status.substring(begin, end));
        } catch (Exception e) {
            log.warn("InnoDB 상태 조회 실패: {}", NestedExceptionUtils.getMostSpecificCause(e).getMessage());
        }
    }

    private void registerUser(String loginId, String name) {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setMemId(loginId);
        dto.setMemPass("1234");
        dto.setMemName(name);
        dto.setDeptCode("DEPT01");
        dto.setCoCode("CO001");
        jpaUserService.register(dto);
        createdLoginIds.add(loginId);
    }

    /**
     * 기안자(1단계) -> 결재자(2단계) 결재선으로 기안하고, 채번된 문서번호를 반환
     */
    private String draft(UserMaster drafter, UserMaster approver, Long docFormId, int seq) {
        ApproverInfoDTO drafterLine = new ApproverInfoDTO();
        drafterLine.setStepSeq(1);
        drafterLine.setApproverId(drafter.getLoginId());
        drafterLine.setApproverName(drafter.getMemName());
        drafterLine.setApproveType("01");

        ApproverInfoDTO approverLine = new ApproverInfoDTO();
        approverLine.setStepSeq(2);
        approverLine.setApproverId(approver.getLoginId());
        approverLine.setApproverName(approver.getMemName());
        approverLine.setApproveType("02");

        ApprovalDraftRequestDTO dto = new ApprovalDraftRequestDTO();
        dto.setDocFormId(docFormId);
        dto.setDocTitle("데드락 테스트 문서 " + seq);
        dto.setDocContent("<p>데드락 테스트 본문</p>");
        dto.setApprovalLines(List.of(drafterLine, approverLine));

        jpaApprovalService.draft(dto, drafter);
        createdDocIds.add(dto.getDocId());

        // draft 내부에서 채번된 문서번호가 DTO에 세팅됨
        return dto.getDocId();
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
}
