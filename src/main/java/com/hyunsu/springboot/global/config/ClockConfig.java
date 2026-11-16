package com.hyunsu.springboot.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * 애플리케이션 공용 Clock.
 *
 * LocalDateTime.now()처럼 인자 없이 현재 시각을 구하면 "어느 시간대 기준인지"가 코드에 드러나지 않고,
 * 테스트에서 시각을 고정할 수도 없다. 그래서 현재 시각이 필요한 곳은 이 Clock을 주입받아
 * LocalDateTime.now(clock)으로 구한다 (테스트에서는 Clock.fixed()로 교체 가능).
 *
 * 시간대는 서버(JVM) 기본 시간대를 따른다 - JPA Auditing(@CreatedDate 등)도 같은 기준이라 값이 섞이지 않는다.
 * 운영 서버의 시간대 고정(Asia/Seoul)은 배포 환경(Docker TZ 등)에서 맞춘다.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
