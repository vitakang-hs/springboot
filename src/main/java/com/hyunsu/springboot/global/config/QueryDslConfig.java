package com.hyunsu.springboot.global.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QueryDSL 설정.
 *
 * JPAQueryFactory를 Bean으로 등록해두면, 각 도메인의 infrastructure.repository.querydsl
 * 패키지에 있는 Repository 구현체에서 생성자 주입으로 바로 사용할 수 있다.
 *
 * 사용 예 (board 도메인 검색/페이징 구현 시):
 *   @RequiredArgsConstructor
 *   public class BoardQueryRepositoryImpl implements BoardQueryRepository {
 *       private final JPQueryFactory queryFactory;
 *
 *       public List<Board> search(String keyword) {
 *           return queryFactory.selectFrom(board)
 *                   .where(board.title.contains(keyword))
 *                   .fetch();
 *       }
 *   }
 */
@Configuration
public class QueryDslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}