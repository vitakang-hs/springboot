package com.hyunsu.springboot.auth.infrastructure.jwt;

import com.hyunsu.springboot.global.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

import lombok.Getter;

/**
 * JWT 발급/검증을 담당.
 *
 * Access Token payload에는 최소 정보(memberId, role)만 담는다.
 * 이메일 등 개인정보는 절대 넣지 않는다.
 */
@Component
public class JwtProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtProvider.class);

    private static final String CLAIM_MEMBER_ID = "memberId";
    private static final String CLAIM_ROLE = "role";

    private final SecretKey secretKey;
    @Getter
    private final long accessTokenValidityMillis;
    @Getter
    private final long refreshTokenValidityMillis;

    public JwtProvider(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes());
        this.accessTokenValidityMillis = jwtProperties.accessTokenValidityMinutes() * 60 * 1000;
        this.refreshTokenValidityMillis = jwtProperties.refreshTokenValidityDays() * 24 * 60 * 60 * 1000;
    }

    public String createAccessToken(Long memberId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidityMillis);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(CLAIM_MEMBER_ID, memberId)
                .claim(CLAIM_ROLE, role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(Long memberId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValidityMillis);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(CLAIM_MEMBER_ID, memberId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getMemberId(String token) {
        return parseClaims(token).get(CLAIM_MEMBER_ID, Long.class);
    }

    public String getRole(String token) {
        return parseClaims(token).get(CLAIM_ROLE, String.class);
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("만료된 JWT: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 JWT: {}", e.getMessage());
        }
        return false;
    }
}