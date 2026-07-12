package com.dduru.gildongmu.admin.post.service;

import com.dduru.gildongmu.admin.post.dto.response.AdminPostDetailResponse;
import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPostService {

    private final PostRepository postRepository;
    private final JsonConverter jsonConverter;
    private final TimeProvider timeProvider;

    public AdminPostDetailResponse getDetail(Long postId) {
        Post post = postRepository.getByIdOrThrow(postId);
        return AdminPostDetailResponse.from(post, jsonConverter);
    }

    @Transactional
    public void delete(Long postId, Long adminUserId) {
        Post post = postRepository.getByIdOrThrow(postId);

        if (post.isDeleted()) {
            return;
        }

        post.softDelete(adminUserId, timeProvider.now());
        log.info("관리자 게시글 삭제됨 - postId={}, adminUserId={}", postId, adminUserId);
    }
}
