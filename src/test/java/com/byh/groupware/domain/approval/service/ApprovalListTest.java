package com.byh.groupware.domain.approval.service;

import com.byh.groupware.domain.approval.dto.ApprovalDraftRequestDTO;
import com.byh.groupware.domain.approval.dto.ApprovalSearchDTO;
import com.byh.groupware.domain.approval.dto.ApproverInfoDTO;
import com.byh.groupware.domain.approval.entity.ActiveDoc;
import com.byh.groupware.domain.approval.exception.ApprovalInvalidTypeException;
import com.byh.groupware.domain.approval.repository.DocFormRepository;
import com.byh.groupware.domain.user.dto.UserRegisterDTO;
import com.byh.groupware.domain.user.entity.UserMaster;
import com.byh.groupware.domain.user.repository.UserRepository;
import com.byh.groupware.domain.user.service.JPAUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 문서 목록 조회 (결재대기함 / 기안함) 테스트
 * - 단일 스레드 테스트이므로 @Transactional 로 롤백 (개발 DB에 데이터 남지 않음)
 */
@SpringBootTest
@Transactional
class ApprovalListTest {

    @Autowired
    private JpaApprovalService jpaApprovalService;

    @Autowired
    private JPAUserService jpaUserService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocFormRepository docFormRepository;

    private final Pageable pageable = PageRequest.of(0, 10);

    private UserMaster drafter;
    private UserMaster approver1;
    private UserMaster approver2;
    private Long activeDocId;

    @BeforeEach
    void setUp() {
        String runTag = String.valueOf(System.currentTimeMillis() % 1_000_000);
        drafter = registerUser("ls" + runTag + "_drafter", "기안자");
        approver1 = registerUser("ls" + runTag + "_apr1", "결재자1");
        approver2 = registerUser("ls" + runTag + "_apr2", "결재자2");

        // 기안자 -> 결재자1(현재 차례) -> 결재자2(대기)
        draft();
        activeDocId = jpaApprovalService.getApprovalList(search("DRAFT"), drafter, pageable)
                .getContent().get(0).getId();
    }

    @Test
    public void 결재대기함은_현재_내_차례인_문서만_조회() {
        assertThat(ids(jpaApprovalService.getApprovalList(search("TODO"), approver1, pageable).getContent()))
                .containsExactly(activeDocId);

        // 기안자, 아직 차례가 아닌 결재자의 결재대기함에는 나오지 않음
        assertThat(jpaApprovalService.getApprovalList(search("TODO"), drafter, pageable).getContent()).isEmpty();
        assertThat(jpaApprovalService.getApprovalList(search("TODO"), approver2, pageable).getContent()).isEmpty();
    }

    @Test
    public void 기안함은_내가_기안한_진행문서만_조회() {
        assertThat(ids(jpaApprovalService.getApprovalList(search("DRAFT"), drafter, pageable).getContent()))
                .containsExactly(activeDocId);

        assertThat(jpaApprovalService.getApprovalList(search("DRAFT"), approver1, pageable).getContent()).isEmpty();
    }

    @Test
    public void 조회타입이_없으면_결재대기함으로_조회() {
        assertThat(ids(jpaApprovalService.getApprovalList(search(null), approver1, pageable).getContent()))
                .containsExactly(activeDocId);
    }

    @Test
    public void 지원하지_않는_조회타입은_예외() {
        assertThatThrownBy(() -> jpaApprovalService.getApprovalList(search("DEPT"), approver1, pageable))
                .isInstanceOf(ApprovalInvalidTypeException.class);
    }

    private ApprovalSearchDTO search(String viewType) {
        ApprovalSearchDTO dto = new ApprovalSearchDTO();
        dto.setViewType(viewType);
        return dto;
    }

    private List<Long> ids(List<ActiveDoc> docs) {
        return docs.stream().map(ActiveDoc::getId).toList();
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

    private void draft() {
        ApprovalDraftRequestDTO dto = new ApprovalDraftRequestDTO();
        dto.setDocFormId(docFormRepository.findAll().get(0).getId());
        dto.setDocTitle("목록 조회 테스트 문서");
        dto.setDocContent("<p>목록 조회 테스트 본문</p>");
        dto.setApprovalLines(List.of(
                createLine(1, drafter, "01"),
                createLine(2, approver1, "02"),
                createLine(3, approver2, "02")
        ));
        jpaApprovalService.draft(dto, drafter);
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
