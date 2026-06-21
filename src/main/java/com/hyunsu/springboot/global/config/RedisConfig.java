package com.hyunsu.springboot.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 연결 설정.
 *
 * 용도:
 *  - 이메일 인증코드 발급/만료 처리 (TTL 3분)
 *  - 회원가입 ID 채번
 *  - Refresh Token 저장 (로그아웃/강제 만료 시 즉시 무효화)
 *  - 로그인 실패 횟수 카운트 (선택적으로 Redis 사용 가능, 현재는 DB 컬럼으로 처리 예정)
 *
 * key/value 모두 String으로 직렬화한다.
 * (JSON 직렬화가 필요해지면 - 예: RefreshToken 객체를 통째로 저장하는 경우 - 추후 Jackson2JsonRedisSerializer로 교체)
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}