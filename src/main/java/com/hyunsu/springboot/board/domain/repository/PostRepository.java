package com.hyunsu.springboot.board.domain.repository;

import com.hyunsu.springboot.board.domain.model.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}