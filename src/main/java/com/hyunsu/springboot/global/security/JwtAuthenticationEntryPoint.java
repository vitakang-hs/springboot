package com.hyunsu.springboot.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;

/**
 * 인증되지 않은 상태로 인증이 필요한 리소스(/api/** 중 authenticated() 대상)에 접근했을 때 호출된다.
 * Spring Security 기본 응답 대신, 우리 프로젝트의 공통 ApiResponse 포맷으로 401을 내려준다.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

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

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}