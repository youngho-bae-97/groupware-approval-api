package com.byh.groupware.domain.user.controller;

import com.byh.groupware.domain.user.dto.UserLoginDTO;
import com.byh.groupware.domain.user.dto.UserRegisterDTO;
import com.byh.groupware.global.security.LoginUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Spring Security 세션 인증 테스트
 * - 로그인 성공 시 세션에는 SecurityContext 만 저장되고, principal 은 최소 정보(LoginUser)이며 비밀번호 해시가 없음
 * - 인증 없이 보호된 URL 접근 시 401 (ErrorResponse 형식)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void 로그인하면_세션에_최소정보만_저장되고_보호된_API_접근가능() throws Exception {
        //given
        String loginId = "auth" + System.currentTimeMillis() % 1_000_000;
        register(loginId);
        MockHttpSession session = new MockHttpSession();

        //when
        mockMvc.perform(post("/api/auth/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(loginId, "1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value(loginId))
                .andExpect(jsonPath("$.name").value("인증사용자"));

        //then: 세션에는 엔티티가 아닌 SecurityContext(LoginUser) 만 있음
        assertThat(session.getAttribute("loginUser")).isNull();
        SecurityContext context = (SecurityContext) session.getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        LoginUser principal = (LoginUser) context.getAuthentication().getPrincipal();
        assertThat(principal.getLoginId()).isEqualTo(loginId);
        assertThat(principal.getPassword()).as("비밀번호 해시는 세션에 남지 않음").isNull();

        // 같은 세션으로 보호된 API 접근 가능
        mockMvc.perform(get("/api/approvals").session(session))
                .andExpect(status().isOk());

        // 로그아웃 후에는 다시 401
        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isNoContent());
        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    public void 인증_실패_401() throws Exception {
        String loginId = "auth" + System.currentTimeMillis() % 1_000_000;
        register(loginId);

        // 비밀번호 불일치
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(loginId, "wrong")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다."));

        // 존재하지 않는 아이디도 같은 응답 (아이디 존재 여부 노출 방지)
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("no_such_user_" + loginId, "1234")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다."));

        // 로그인 없이 보호된 API -> 401 (필터 단계에서 ErrorResponse 형식으로 응답)
        mockMvc.perform(get("/api/approvals/{id}", 1L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
    }

    private void register(String loginId) throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setMemId(loginId);
        dto.setMemPass("1234");
        dto.setMemName("인증사용자");
        dto.setDeptCode("DEPT01");
        dto.setCoCode("CO001");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    private String loginBody(String loginId, String password) throws Exception {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setMemId(loginId);
        dto.setMemPass(password);
        return objectMapper.writeValueAsString(dto);
    }
}
