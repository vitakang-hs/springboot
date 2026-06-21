package com.hyunsu.springboot.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 활성화.
 *
 * CreatedDate, @LastModifiedDate 어노테이션을 Entity 필드에 붙이면
 * INSERT/UPDATE 시점에 자동으로 값이 채워진다.
 * (member 테이블의 created_at, updated_at 컬럼에 대응)
 *
 * 사용 예 (BaseEntity 같은 공통 상위 클래스에 적용 권장):
 *   CreatedDate
 *   Column(name = "created_at", updatable = false)
 *   private LocalDateTime createdAt;
 *
 *   LastModifiedDate
 *   Column(name = "updated_at")
 *   private LocalDateTime updatedAt;
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}