package com.byh.groupware.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // 시큐리티 설정 제어
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 이거 안 끄면 POST 요청 시 403 에러발생
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() //
                )
                .formLogin(login -> login.disable()) // 시큐리티 기본 로그인 창 끄기
                .httpBasic(basic -> basic.disable()); // 팝업(Basic Auth) 끄기

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
