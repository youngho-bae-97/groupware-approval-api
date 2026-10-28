package com.byh.groupware.domain.approval.service;

import com.byh.groupware.domain.approval.dto.ApprovalDetailResponseDTO;
import com.byh.groupware.domain.approval.dto.ApprovalDraftRequestDTO;
import com.byh.groupware.domain.approval.dto.ApprovalProcessRequestDTO;
import com.byh.groupware.domain.approval.dto.ApproverInfoDTO;
import com.byh.groupware.domain.approval.entity.DocumentMaster;
import com.byh.groupware.domain.approval.entity.EndDoc;
import com.byh.groupware.domain.approval.repository.DocFormRepository;
import com.byh.groupware.domain.user.dto.UserRegisterDTO;
import com.byh.groupware.domain.user.entity.UserMaster;
import com.byh.groupware.domain.user.repository.UserRepository;
import com.byh.groupware.domain.user.service.JPAUserService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 최종결재 시 진행(hot) -> 완료(cold) 이관 테스트
 * - 문서 마스터는 이관하지 않고 남으며, 완료 문서가 같은 마스터를 참조해야 함
 * - 단일 스레드 테스트이므로 @Transactional 로 롤백 (개발 DB에 데이터 남지 않음)
 */
@SpringBootTest
@Transactional
class ApprovalArchiveTest {

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

    @Test
    public void 최종결재시_완료문서는_기존_문서마스터를_참조() {
        //given
        String runTag = String.valueOf(System.currentTimeMillis() % 1_000_000);
        UserMaster drafter = registerUser("ar" + runTag + "_drafter", "기안자");
        UserMaster approver = registerUser("ar" + runTag + "_apr", "결재자");

        String docId = draft(drafter, approver);
        DocumentMaster master = findMaster(docId);
        Long masterId = master.getId();

        ApprovalProcessRequestDTO request = new ApprovalProcessRequestDTO();
        request.setDocMasterId(masterId);
        request.setActiveDocId(master.getActiveDoc().getId());
        request.setApproveType("APPROVE");

        //when
        jpaApprovalService.doProcess(request, approver);
        em.flush();
        em.clear();

        //then
        // 문서 마스터는 남아 있고 진행 문서는 삭제됨
        DocumentMaster remained = em.find(DocumentMaster.class, masterId);
        assertThat(remained).isNotNull();
        assertThat(em.createQuery("select count(d) from ActiveDoc d where d.documentMaster.id = :id", Long.class)
                .setParameter("id", masterId).getSingleResult()).isZero();

        // 완료 문서가 같은 마스터를 참조
        EndDoc endDoc = em.createQuery("select e from EndDoc e where e.documentMaster.id = :id", EndDoc.class)
                .setParameter("id", masterId)
                .getSingleResult();
        assertThat(endDoc.getEndAprLines()).hasSize(2);

        // 완료 문서 상세 조회 (마스터 정보는 DocumentMaster 에서)
        ApprovalDetailResponseDTO detail = jpaApprovalService.getApprovalDetail(masterId, drafter.getLoginId());
        assertThat(detail.getDocId()).isEqualTo(docId);
        assertThat(detail.getDrafterName()).isEqualTo("기안자");
        assertThat(detail.getDocStatus()).isEqualTo("03");
    }

    private DocumentMaster findMaster(String docId) {
        return em.createQuery("select d from DocumentMaster d join fetch d.activeDoc where d.docId = :docId",
                        DocumentMaster.class)
                .setParameter("docId", docId)
                .getSingleResult();
    }

    private UserMaster registerUser(String loginId, String name) {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setMemId(loginId);
        dto.setMemPass("1234");
        dto.setMemName(name);
        dto.setDeptCode("DEPT01");
        dto.setCoCode("CO001");
        jpaUserService.register(dto);
        return userRepository.findByLoginId(loginId);
    }

    /**
     * 기안자(1단계) -> 결재자(2단계, 최종결재자) 결재선으로 기안하고, 채번된 문서번호를 반환
     */
    private String draft(UserMaster drafter, UserMaster approver) {
        ApprovalDraftRequestDTO dto = new ApprovalDraftRequestDTO();
        dto.setDocFormId(docFormRepository.findAll().get(0).getId());
        dto.setDocTitle("이관 테스트 문서");
        dto.setDocContent("<p>이관 테스트 본문</p>");
        dto.setApprovalLines(List.of(
                createLine(1, drafter, "01"),
                createLine(2, approver, "02")
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
}
