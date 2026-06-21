package com.hyunsu.springboot.member.application.command.dto;

/**
 * 이메일 인증코드 검증 Command.
 */
public record EmailCodeVerifyCommand(
        String email,
        String code
) {
}