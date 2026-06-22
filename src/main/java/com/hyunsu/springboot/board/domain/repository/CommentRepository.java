package com.hyunsu.springboot.board.domain.repository;

import com.hyunsu.springboot.board.domain.model.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 게시글의 삭제되지 않은 댓글 목록 (작성 순).
     */
    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findActiveByPostId(Long postId);

    /**
     * 게시글의 삭제되지 않은 댓글 수.
     * 목록 페이지에서 [N] 표시에 사용.
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId AND c.deletedAt IS NULL")
    long countActiveByPostId(Long postId);
}