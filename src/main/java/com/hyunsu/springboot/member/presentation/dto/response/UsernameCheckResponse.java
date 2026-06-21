package com.hyunsu.springboot.member.presentation.dto.response;

/**
 * 아이디 중복확인 응답.
 * available = true  -> 사용 가능한 아이디 (중복 아님)
 * available = false -> 이미 사용 중인 아이디 (중복)
 */
public record UsernameCheckResponse(boolean available) {
}