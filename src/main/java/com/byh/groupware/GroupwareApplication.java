package com.byh.groupware;

import org.mybatis.spring.annotation.MapperScan;
import com.byh.groupware.global.security.LoginUser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@EnableJpaAuditing
@SpringBootApplication(
        exclude = {
                org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration.class
        }
)
public class GroupwareApplication {

	public static void main(String[] args) {
		SpringApplication.run(GroupwareApplication.class, args);
	}

//    @Bean
//    public AuditorAware<String> auditorProvider(HttpSession session) {
//        return () -> {
//            // 세션에서 로그인 정보 가져오기
//            Object loginUser = session.getAttribute("loginUser");
//
//            if (loginUser == null) {
//                return Optional.empty();
//            }
//
//
//            return Optional.of(loginUser.toString());
//        };
//    }

    // 생성자/수정자(created_by, last_modified_by): 로그인 사용자면 loginId, 아니면(회원가입, 배치 등) SYSTEM
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
                return Optional.of(loginUser.getLoginId());
            }

            return Optional.of("SYSTEM");
        };
    }
}
