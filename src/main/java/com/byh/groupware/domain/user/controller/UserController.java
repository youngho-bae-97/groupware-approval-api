package com.byh.groupware.domain.user.controller;

import com.byh.groupware.domain.user.dto.UserLoginDTO;
import com.byh.groupware.domain.user.model.UserMasterVO;
import com.byh.groupware.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 1. 회원가입 API
    @PostMapping("/register")
    public ResponseEntity<String> join(@RequestBody UserMasterVO user) {
        userService.register(user);
        return ResponseEntity.ok("회원가입 성공");
    }

    // 2. 로그인 API
    @PostMapping("/login")
    public ResponseEntity<UserMasterVO> login(@RequestBody UserLoginDTO loginDto, HttpServletRequest request) {
        // 로직 실행
        UserMasterVO loginUser = userService.login(loginDto.getMemId(), loginDto.getMemPass());

        // 세션 생성 및 저장
        HttpSession session = request.getSession();
        session.setAttribute("loginUser", loginUser);

        return ResponseEntity.ok(loginUser);
    }

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
