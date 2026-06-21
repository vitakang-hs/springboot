package com.hyunsu.springboot.global.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * HttpServletRequest에서 클라이언트의 실제 IP를 추출하는 유틸.
 *
 * 로그인 이력, 정보 변경 이력 등 여러 도메인에서 공통으로 필요할 수 있어 global로 분리한다.
 * static 메서드로 두어 Bean 주입 없이 어디서든 바로 사용할 수 있게 한다.
 */
public final class IpAddressResolver {

    private IpAddressResolver() {
    }

    /**
     * 프록시/로드밸런서를 거치는 경우 X-Forwarded-For 헤더에 원본 IP가 담기므로 우선 확인하고,
     * 없으면 request.getRemoteAddr()로 폴백한다.
     */
    public static String resolve(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}