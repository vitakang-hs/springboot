package com.hyunsu.springboot.member.application.command.dto;

/**
 * OAuthLoginCommand 처리 결과.
 * 처리 후 확보된 회원의 ID만 담는다 (엔티티를 그대로 반환하지 않음).
 */
public record OAuthLoginCommandResult(
        Long memberId
) {
}