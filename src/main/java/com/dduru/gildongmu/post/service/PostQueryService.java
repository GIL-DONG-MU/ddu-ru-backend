package com.dduru.gildongmu.post.service;

import com.dduru.gildongmu.auth.enums.AgeRange;
import com.dduru.gildongmu.auth.enums.Gender;
import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.dto.PostListRequest;
import com.dduru.gildongmu.post.dto.PostListResponse;
import com.dduru.gildongmu.post.dto.PostSummaryDto;
import com.dduru.gildongmu.post.enums.PostStatus;
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

        Page<Post> postPage = getPostsByCondition(request, pageable);

        List<Post> posts = postPage.getContent();
        boolean hasNext = posts.size() == request.size();

        List<PostSummaryDto> dtos = posts.stream()
                .map(post -> PostSummaryDto.from(post, jsonConverter))
                .toList();

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

    private Page<Post> getPostsByCondition(PostListRequest request, Pageable pageable) {
        Gender preferredGender = null;
        if (request.preferredGender() != null) {
            try {
                preferredGender = Gender.valueOf(request.preferredGender().toUpperCase());
            } catch (IllegalArgumentException e) {
                preferredGender = Gender.from(request.preferredGender());
                if (preferredGender == Gender.U && !request.preferredGender().equalsIgnoreCase("U")) {
                    log.warn("유효하지 않은 preferredGender 값: {}", request.preferredGender());
                    preferredGender = null;
                }
            }
        }

        AgeRange preferredAge = null;
        if (request.preferredAge() != null) {
            try {
                preferredAge = AgeRange.valueOf(request.preferredAge().toUpperCase());
            } catch (IllegalArgumentException e) {
                preferredAge = AgeRange.from(request.preferredAge());
                if (preferredAge == AgeRange.UNKNOWN) {
                    log.warn("유효하지 않은 preferredAge 값: {}", request.preferredAge());
                    preferredAge = null;
                }
            }
        }

        Boolean isRecruiting = null;
        if (request.hasRecruitmentStatusFilter()) {
            isRecruiting = request.recruitmentStatus().equals("RECRUITING");
        }

        return postRepository.findPostsWithFilters(
                request.cursor(),
                request.keyword(),
                request.startDate(),
                request.endDate(),
                preferredGender,
                preferredAge,
                request.destinationId(),
                isRecruiting,
                PostStatus.OPEN,
                PostStatus.FULL,
                PostStatus.CLOSED,
                pageable
        );
    }
}
