package com.hyunsu.springboot.board.domain.repository;

import com.hyunsu.springboot.board.domain.model.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 삭제되지 않은 게시글 목록 (페이지네이션).
     * 공지(NOTICE)가 항상 위에, 나머지는 최신순.
     */
    @Query("""
            SELECT p FROM Post p
            WHERE p.deletedAt IS NULL
            ORDER BY
                CASE WHEN p.category = 'NOTICE' THEN 0 ELSE 1 END ASC,
                p.createdAt DESC
            """)
    Page<Post> findAllActive(Pageable pageable);

    /**
     * 삭제되지 않은 게시글 단건 조회 (상세보기용).
     */
    @Query("SELECT p FROM Post p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Post> findActiveById(Long id);

    /**
     * 조회수 1 증가.
     * 엔티티를 읽어 viewCount++ 후 저장하면 동시 조회 시 증가분이 유실될 수 있어,
     * DB에서 원자적으로 증가시킨다.
     *
     * @return 증가된 행 수 (0이면 존재하지 않거나 삭제된 게시글)
     */
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id AND p.deletedAt IS NULL")
    int increaseViewCount(Long id);
}