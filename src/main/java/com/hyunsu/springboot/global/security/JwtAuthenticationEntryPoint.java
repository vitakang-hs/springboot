package com.hyunsu.springboot.global.security;

import com.hyunsu.springboot.global.common.response.ApiResponse;
import com.hyunsu.springboot.global.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;

import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

/**
 * 인증되지 않은 상태로 인증이 필요한 리소스(/api/** 중 authenticated() 대상)에 접근했을 때 호출된다.
 * Spring Security 기본 응답 대신, 우리 프로젝트의 공통 ApiResponse 포맷으로 401을 내려준다.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // 직접 new ObjectMapper()를 만들면 Spring MVC 응답과 설정(LocalDateTime 직렬화 등)이 달라진다.
    // (Jackson 2 기본 ObjectMapper는 ApiResponse.timestamp(LocalDateTime)를 직렬화하지 못해 401 대신 500이 났음)
    // 그래서 Spring Boot가 구성한 JsonMapper(Jackson 3)를 주입받아 다른 API 응답과 같은 형식으로 쓴다.
    private final JsonMapper jsonMapper;

    public JwtAuthenticationEntryPoint(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void commence(
            @NonNull HttpServletRequest request,
            HttpServletResponse response,
            @NonNull AuthenticationException authException
    ) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Void> body = ApiResponse.fail(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.INVALID_TOKEN.getCode(),
                "인증이 필요합니다. 로그인 후 다시 시도해 주세요."
        );

        response.getWriter().write(jsonMapper.writeValueAsString(body));
    }
}