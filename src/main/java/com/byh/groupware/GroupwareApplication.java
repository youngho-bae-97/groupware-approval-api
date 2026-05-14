package com.byh.groupware;

import com.byh.groupware.domain.user.entity.UserMaster;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

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

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            // 현재 요청에 대한 속성을 가져옴
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            // 현재 요청(Request)이 없으면 에러를 내지 말고 빈 값을 반환함
            if (attributes == null) {
                return Optional.empty();
            }

            HttpServletRequest request = attributes.getRequest();
            HttpSession session = request.getSession(false);

            if (session != null && session.getAttribute("loginUser") != null) {
                // 세션에서 로그인한 사용자 ID를 추출 (예시)
                 UserMaster loginMember = (UserMaster) session.getAttribute("loginUser");
                 return Optional.of(loginMember.getLoginId());
            }

            return Optional.of("SYSTEM"); // 로그인 정보가 없으면 시스템으로 기록
        };
    }
}
