package com.dduru.gildongmu.post.service;

import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.dto.request.PostListRequest;
import com.dduru.gildongmu.post.dto.response.PostListResponse;
import com.dduru.gildongmu.post.dto.response.PostSummaryResponse;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {
    private final PostRepository postRepository;
    private final ProfileImageResolver profileImageResolver;
    private final TimeProvider timeProvider;

    public PostListResponse retrieveAllWithFilter(PostListRequest request) {
        LocalDate today = timeProvider.today();
        Pageable pageable = PageRequest.of(0, request.size() + 1);
        List<Post> posts = postRepository.findPostsWithFilters(request, pageable);

        boolean hasNext = posts.size() > request.size();

        if (hasNext) {
            posts = posts.subList(0, request.size());
        }

        List<PostSummaryResponse> summaries = posts.stream()
                .map(post -> PostSummaryResponse.from(post, profileImageResolver, today, false, null))
                .toList();

        return PostListResponse.of(summaries, hasNext);
    }
}
