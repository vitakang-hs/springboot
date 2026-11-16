package com.hyunsu.springboot.board.application.query;

import com.hyunsu.springboot.board.application.query.dto.PostDetailResult;
import com.hyunsu.springboot.board.domain.repository.PostQueryRepository;
import com.hyunsu.springboot.global.exception.CustomException;
import com.hyunsu.springboot.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게시글 상세 조회 (본문 + 댓글 목록).
 * 조회수 증가는 상태 변경이라 이 Query가 아닌 PostViewCountCommandService가 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostDetailQueryService {

    private final PostQueryRepository postQueryRepository;

    public PostDetailResult getPostDetail(Long postId) {
        PostDetailResult post = postQueryRepository.findPostDetail(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        return post.withComments(postQueryRepository.findComments(postId));
    }
}
