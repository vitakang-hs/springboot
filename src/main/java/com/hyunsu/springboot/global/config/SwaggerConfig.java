package com.hyunsu.springboot.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger(OpenAPI) 문서 설정.
 * http://localhost:8080/swagger-ui/index.html 에서 확인 가능.
 *
 * JWT Bearer 인증 스킴을 등록해두어, 로그인 API로 발급받은 Access Token을
 * Swagger UI 우측 상단 'Authorize' 버튼에 입력하면 인증이 필요한 API도 바로 테스트할 수 있다.
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(
                        SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("Springboot Portfolio API")
                .description("회원 인증(JWT/OAuth2)과 게시판 도메인을 포함한 포트폴리오 프로젝트 API 문서")
                .version("v0.0.1");
    }
}