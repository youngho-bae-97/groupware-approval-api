package com.byh.groupware;

import jakarta.servlet.http.HttpSession;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

@EnableJpaAuditing
@SpringBootApplication
public class GroupwareApplication {

	public static void main(String[] args) {
		SpringApplication.run(GroupwareApplication.class, args);
	}

    @Bean
    public AuditorAware<String> auditorProvider(HttpSession session) {
        return () -> {
            // 세션에서 로그인 정보 가져오기
            Object loginUser = session.getAttribute("loginUser");

            if (loginUser == null) {
                return Optional.empty();
            }


            return Optional.of(loginUser.toString());
        };
    }
}
