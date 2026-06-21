package com.hyunsu.springboot.auth.application.command.dto;

/**
 * 로그인 Command.
 * ipAddress는 Request Body가 아니라 HttpServletRequest에서 추출하므로,
 * Mapper가 Request DTO만으로는 만들 수 없어 Controller에서 ipAddress를 별도로 채워 넣는다.
 */
public record LoginCommand(
        String username,
        String password,
        String ipAddress
) {
}