package com.byh.groupware.domain.user.controller;

import com.byh.groupware.domain.user.dto.UserRegisterDTO;
import com.byh.groupware.domain.user.exception.InvalidLoginException;
import com.byh.groupware.domain.user.service.JPAUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "회원 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final JPAUserService jpaUserService;

    // 회원가입 = 회원 리소스 생성
    @Operation(summary = "회원가입", description = "사용자 비밀번호는 Spring Security의 BCryptPasswordEncoder를 활용하여 해싱 후 저장")
    @PostMapping
    public ResponseEntity<String> join(@Valid @RequestBody UserRegisterDTO userRegisterDTO, BindingResult result) {
        if(result.hasErrors()){
            throw new InvalidLoginException("잘못된 회원가입 정보");
        }

        jpaUserService.register(userRegisterDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
    }
}
