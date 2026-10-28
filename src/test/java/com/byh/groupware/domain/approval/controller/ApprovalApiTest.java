package com.byh.groupware.domain.approval.controller;

import com.byh.groupware.domain.approval.dto.ApprovalDraftRequestDTO;
import com.byh.groupware.domain.approval.dto.ApproverInfoDTO;
import com.byh.groupware.domain.approval.repository.DocFormRepository;
import com.byh.groupware.domain.user.dto.UserLoginDTO;
import com.byh.groupware.domain.user.dto.UserRegisterDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 전자결재 REST API 흐름 테스트
 * - 기안(201 + Location) -> 목록/상세 조회 -> 결재(204) -> 같은 URL 로 완료 문서 조회
 * - 문서 식별자는 문서 마스터 ID 라서, 최종결재로 완료 테이블로 이관된 뒤에도 URL 이 그대로임
 * - MockMvc 는 테스트와 같은 스레드에서 실행되므로 @Transactional 로 롤백 (개발 DB에 데이터 남지 않음)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApprovalApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DocFormRepository docFormRepository;

    @Test
    public void 기안부터_최종결재까지_API_흐름() throws Exception {
        //given
        String runTag = String.valueOf(System.currentTimeMillis() % 1_000_000);
        String drafterId = "api" + runTag + "_drafter";
        String approverId = "api" + runTag + "_apr";
        register(drafterId, "기안자");
        register(approverId, "결재자");

        MockHttpSession drafterSession = login(drafterId);
        MockHttpSession approverSession = login(approverId);

        //when: 기안 -> 201 Created + Location
        MvcResult draftResult = mockMvc.perform(post("/api/approvals")
                        .session(drafterSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(draftRequest(drafterId, approverId))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn();

        JsonNode draftBody = objectMapper.readTree(draftResult.getResponse().getContentAsString());
        long id = draftBody.get("id").asLong();
        String location = draftResult.getResponse().getHeader("Location");
        assertThat(location).endsWith("/api/approvals/" + id);

        //then: 결재자의 결재대기함에 보이고, 상세에서 결재 가능
        mockMvc.perform(get("/api/approvals").param("viewType", "TODO").session(approverSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(get("/api/approvals/{id}", id).session(approverSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.docStatus").value("02"))
                .andExpect(jsonPath("$.canApprove").value(true));

        //when: 결재 (최종결재자이므로 완료 테이블로 이관) -> 204 No Content
        mockMvc.perform(post("/api/approvals/{id}/approve", id).session(approverSession))
                .andExpect(status().isNoContent());

        //then: 같은 URL 로 완료 문서 조회
        mockMvc.perform(get("/api/approvals/{id}", id).session(drafterSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.docId").value(draftBody.get("docId").asText()))
                .andExpect(jsonPath("$.docStatus").value("03"));

        // 이미 완료된 문서를 다시 결재하면 409 Conflict
        mockMvc.perform(post("/api/approvals/{id}/approve", id).session(approverSession))
                .andExpect(status().isConflict());
    }

    @Test
    public void 상태코드_401_403_404() throws Exception {
        String runTag = String.valueOf(System.currentTimeMillis() % 1_000_000);
        String drafterId = "api" + runTag + "_drafter";
        String approverId = "api" + runTag + "_apr";
        String outsiderId = "api" + runTag + "_out";
        register(drafterId, "기안자");
        register(approverId, "결재자");
        register(outsiderId, "외부인");

        MockHttpSession drafterSession = login(drafterId);

        MvcResult draftResult = mockMvc.perform(post("/api/approvals")
                        .session(drafterSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(draftRequest(drafterId, approverId))))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(draftResult.getResponse().getContentAsString()).get("id").asLong();

        // 로그인하지 않음 -> 401
        mockMvc.perform(get("/api/approvals"))
                .andExpect(status().isUnauthorized());

        // 결재선에 없는 사용자의 상세 조회 -> 403
        mockMvc.perform(get("/api/approvals/{id}", id).session(login(outsiderId)))
                .andExpect(status().isForbidden());

        // 없는 문서 -> 404
        mockMvc.perform(get("/api/approvals/{id}", Long.MAX_VALUE).session(drafterSession))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/approvals/{id}/approve", Long.MAX_VALUE).session(drafterSession))
                .andExpect(status().isNotFound());

        // 로그아웃 -> 204
        mockMvc.perform(post("/api/auth/logout").session(drafterSession))
                .andExpect(status().isNoContent());
    }

    private void register(String loginId, String name) throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setMemId(loginId);
        dto.setMemPass("1234");
        dto.setMemName(name);
        dto.setDeptCode("DEPT01");
        dto.setCoCode("CO001");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    private MockHttpSession login(String loginId) throws Exception {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setMemId(loginId);
        dto.setMemPass("1234");

        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/auth/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
        return session;
    }

    /**
     * 기안자(1단계) -> 결재자(2단계, 최종결재자) 결재선
     */
    private ApprovalDraftRequestDTO draftRequest(String drafterId, String approverId) {
        ApprovalDraftRequestDTO dto = new ApprovalDraftRequestDTO();
        dto.setDocFormId(docFormRepository.findAll().get(0).getId());
        dto.setDocTitle("API 테스트 문서");
        dto.setDocContent("<p>API 테스트 본문</p>");
        dto.setApprovalLines(List.of(
                line(1, drafterId, "기안자", "01"),
                line(2, approverId, "결재자", "02")
        ));
        return dto;
    }

    private ApproverInfoDTO line(int stepSeq, String loginId, String name, String approveType) {
        ApproverInfoDTO line = new ApproverInfoDTO();
        line.setStepSeq(stepSeq);
        line.setApproverId(loginId);
        line.setApproverName(name);
        line.setApproveType(approveType);
        return line;
    }
}
