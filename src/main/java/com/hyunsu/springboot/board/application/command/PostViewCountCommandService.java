package com.hyunsu.springboot.board.application.command;

import com.hyunsu.springboot.board.domain.repository.PostRepository;
import com.hyunsu.springboot.global.exception.CustomException;
import com.hyunsu.springboot.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게시글 조회수 증가.
 *
 * 상세 조회(Query)는 상태를 바꾸지 않아야 하므로, 조회수 증가는 별도 Command로 분리했다.
 * Controller가 상세 조회 직전에 호출한다.
 * (현재는 새로고침할 때마다 증가한다. 중복 조회 방지가 필요해지면 여기서 Redis 등으로 처리한다)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PostViewCountCommandService {

    private final PostRepository postRepository;

    public void increase(Long postId) {
        int updated = postRepository.increaseViewCount(postId);
        if (updated == 0) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
    }
}
