package com.byh.groupware.domain.user.controller;

import com.byh.groupware.domain.user.dto.UserLoginDTO;
import com.byh.groupware.domain.user.dto.UserLoginResponseDTO;
import com.byh.groupware.global.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "로그인 / 로그아웃 API (Spring Security 세션 기반)")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final SecurityContextRepository securityContextRepository;

    @Operation(summary = "로그인", description = "Spring Security(AuthenticationManager)로 아이디/비밀번호(BCrypt)를 검증하고, 인증 정보를 세션에 저장")
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDTO> login(@RequestBody UserLoginDTO loginDto,
                                                      HttpServletRequest request, HttpServletResponse response) {
        // 실패 시 AuthenticationException -> GlobalExceptionHandler 에서 401
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(loginDto.getMemId(), loginDto.getMemPass()));

        // 세션 고정 공격 방지: 로그인 전 세션이 있으면 세션 ID 를 새로 발급
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }

        // 인증 결과를 SecurityContext 에 담아 세션에 저장 (이후 요청은 세션에서 복원)
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return ResponseEntity.ok(new UserLoginResponseDTO(loginUser.getLoginId(), loginUser.getName()));
    }

    @Operation(summary = "로그아웃", description = "세션 무효화 및 SecurityContext 초기화")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);

        return ResponseEntity.noContent().build();
    }
}
