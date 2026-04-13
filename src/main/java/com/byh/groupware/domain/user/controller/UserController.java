package com.byh.groupware.domain.user.controller;

import com.byh.groupware.domain.user.dto.UserLoginDTO;
import com.byh.groupware.domain.user.model.UserMasterVO;
import com.byh.groupware.domain.user.service.UserService;
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

@Tag(name = "User", description = "로그인 및 회원가입 관련 API")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 1. 회원가입 API
    @Operation(summary = "회원가입", description = "사용자 비밀번호는 Spring Security의 BCryptPasswordEncoder를 활용하여 해싱 후 저장")
    @PostMapping("/register")
    public ResponseEntity<String> join(@RequestBody UserMasterVO user) {
        userService.register(user);
        return ResponseEntity.ok("회원가입 성공");
    }

    // 2. 로그인 API
    @Operation(summary = "로그인", description = "사용자로부터 받은 비밀번호값을 해싱하여 DB의 해싱값과 비교 / 성공 시 로그인 정보는 세션에 저장")
    @PostMapping("/login")
    public ResponseEntity<UserMasterVO> login(@RequestBody UserLoginDTO loginDto, HttpServletRequest request) {
        // 로직 실행
        UserMasterVO loginUser = userService.login(loginDto.getMemId(), loginDto.getMemPass());

        // 세션 생성 및 저장
        HttpSession session = request.getSession();
        session.setAttribute("loginUser", loginUser);

        return ResponseEntity.ok(loginUser);
    }

    @Operation(summary = "로그아웃", description = "세션 초기화")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        // 1. 세션을 무효화하여 저장된 모든 속성(loginUser 등) 제거
        if (session != null) {
            session.invalidate();
        }

        // 2. 로그아웃 성공 메시지 반환
        return ResponseEntity.ok("로그아웃 성공");
    }
}
