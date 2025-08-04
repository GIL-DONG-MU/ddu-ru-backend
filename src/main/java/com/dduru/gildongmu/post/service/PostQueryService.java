package com.dduru.gildongmu.post.service;

import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.dto.PostListRequest;
import com.dduru.gildongmu.post.dto.PostListResponse;
import com.dduru.gildongmu.post.dto.PostSummaryResponse;
import com.dduru.gildongmu.post.exception.PostNotFoundException;
import com.dduru.gildongmu.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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

    public PostListResponse getPosts(PostListRequest request) {
        log.debug("게시글 목록 조회 시작 - request: {}", request);

        Pageable pageable = PageRequest.of(0, request.size());
        Page<Post> postPage = postRepository.findPostsWithFilters(request, pageable);

        List<Post> posts = postPage.getContent();
        boolean hasNext = posts.size() == request.size();

        List<PostSummaryResponse> dtos = posts.stream()
                .map(post -> PostSummaryResponse.from(post, jsonConverter))
                .toList();

        log.info("게시글 목록 조회 완료 - 결과 수: {}, hasNext: {}", dtos.size(), hasNext);

        return PostListResponse.of(dtos, hasNext);
    }

    @Transactional
    public void increaseViewCount(Long postId) {
        log.debug("조회수 증가 - postId: {}", postId);

        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException("게시글을 찾을 수 없습니다. postId: " + postId);
        }

        postRepository.incrementViewCount(postId);
        log.info("조회수 증가 완료 - postId: {}", postId);
    }
}
