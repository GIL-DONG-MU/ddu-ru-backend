package com.dduru.gildongmu.post.service;

import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.dto.response.PostDetailResponse;
import com.dduru.gildongmu.post.dto.request.PostListRequest;
import com.dduru.gildongmu.post.dto.response.PostListResponse;
import com.dduru.gildongmu.post.dto.response.PostSummaryResponse;
import com.dduru.gildongmu.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {
    private final PostRepository postRepository;
    private final JsonConverter jsonConverter;

    public PostListResponse retrieveAllWithFilter(PostListRequest request) {
        log.debug("게시글 목록 조회 - size={}", request.size());

        Pageable pageable = PageRequest.of(0, request.size() + 1);
        List<Post> posts = postRepository.findPostsWithFilters(request, pageable);

        boolean hasNext = posts.size() > request.size();

        if (hasNext) {
            posts = posts.subList(0, request.size());
        }

        List<PostSummaryResponse> dtos = posts.stream()
                .map(post -> PostSummaryResponse.from(post, jsonConverter))
                .toList();

        log.debug("게시글 목록 조회 완료 - count={}, hasNext={}", dtos.size(), hasNext);
        return PostListResponse.of(dtos, hasNext);
    }

    @Transactional
    public PostDetailResponse retrieveDetailWithViewCount(Long postId) {
        log.debug("게시글 상세 조회 - postId={}", postId);

        Post post = postRepository.getActiveByIdOrThrow(postId);

        postRepository.incrementViewCount(postId);

        PostDetailResponse response = PostDetailResponse.from(post, jsonConverter);
        log.debug("게시글 상세 조회 완료 - postId={}", postId);
        return response;
    }
}
