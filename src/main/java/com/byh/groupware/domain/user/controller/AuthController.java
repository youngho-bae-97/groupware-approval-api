package com.byh.groupware.domain.user.controller;

import com.byh.groupware.domain.user.dto.UserLoginDTO;
import com.byh.groupware.domain.user.entity.UserMaster;
import com.byh.groupware.domain.user.service.JPAUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "로그인 / 로그아웃 API (세션 기반)")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JPAUserService jpaUserService;

    @Operation(summary = "로그인", description = "사용자로부터 받은 비밀번호값을 해싱하여 DB의 해싱값과 비교 / 성공 시 로그인 정보는 세션에 저장")
    @PostMapping("/login")
    public ResponseEntity<UserLoginDTO> login(@RequestBody UserLoginDTO loginDto, HttpServletRequest request) {
        UserMaster loginUser = jpaUserService.login(loginDto.getMemId(), loginDto.getMemPass());

        // 세션 생성 및 저장
        HttpSession session = request.getSession();
        session.setAttribute("loginUser", loginUser);

        return ResponseEntity.ok(new UserLoginDTO(loginUser));
    }

    @Operation(summary = "로그아웃", description = "세션 초기화")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        // 세션이 없으면 새로 만들지 않음
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.noContent().build();
    }
}
