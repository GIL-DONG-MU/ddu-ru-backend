package com.dduru.gildongmu.post.service;

import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.like.repository.PostLikeRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.PostSortType;
import com.dduru.gildongmu.post.dto.request.PostListRequest;
import com.dduru.gildongmu.post.dto.response.PostListResponse;
import com.dduru.gildongmu.post.dto.response.PostSummaryResponse;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.superhost.service.SuperHostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final ProfileImageResolver profileImageResolver;
    private final SuperHostService superHostService;
    private final TimeProvider timeProvider;

    public PostListResponse retrieveAllWithFilter(PostListRequest request, Long userId) {
        LocalDate today = timeProvider.today();
        Pageable pageable = PageRequest.of(0, request.size() + 1);
        Post cursorPost = fetchCursorPost(request);
        List<Post> posts = postRepository.findPostsWithFilters(request, cursorPost, pageable);

        boolean hasNext = posts.size() > request.size();
        if (hasNext) {
            posts = posts.subList(0, request.size());
        }

        if (posts.isEmpty()) {
            return PostListResponse.of(List.of(), false);
        }

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Set<Long> likedPostIds = fetchLikedPostIds(userId, postIds);
        Set<Long> superHostPostIds = superHostService.findActiveSuperHostPostIds(postIds);

        List<PostSummaryResponse> summaries = posts.stream()
                .map(post -> PostSummaryResponse.from(
                        post, profileImageResolver, today,
                        superHostPostIds.contains(post.getId()),
                        null,
                        likedPostIds.contains(post.getId())
                ))
                .toList();

        return PostListResponse.of(summaries, hasNext);
    }

    private Post fetchCursorPost(PostListRequest request) {
        if (request.cursor() == null || request.sort() == PostSortType.LATEST) {
            return null;
        }
        return postRepository.findById(request.cursor()).orElse(null);
    }

    private Set<Long> fetchLikedPostIds(Long userId, List<Long> postIds) {
        if (userId == null) return Set.of();
        return postLikeRepository.findLikedPostIdsByUserId(userId, postIds);
    }
}
