package com.hyunsu.springboot.board.application.query;

import com.hyunsu.springboot.board.application.query.dto.PostListResult;
import com.hyunsu.springboot.board.application.query.dto.PostSearchCondition;
import com.hyunsu.springboot.board.domain.repository.PostQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostListQueryService {

    //private final PostQueryRepository postQueryRepository;

    public Page<PostListResult> getPostList(PostSearchCondition condition, Pageable pageable) {
        return null;//postQueryRepository.findPostList(condition, pageable);
    }
}