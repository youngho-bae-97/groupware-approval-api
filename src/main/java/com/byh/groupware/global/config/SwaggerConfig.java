package com.byh.groupware.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("전자결재 시스템 API 명세서")
                        .description("기안, 결재, 목록 및 상세 조회를 위한 API 문서입니다.")
                        .version("v1.0.0"));
    }
}
