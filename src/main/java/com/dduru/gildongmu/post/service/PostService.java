package com.dduru.gildongmu.post.service;

import com.dduru.gildongmu.auth.domain.User;
import com.dduru.gildongmu.auth.enums.AgeRange;
import com.dduru.gildongmu.auth.enums.Gender;
import com.dduru.gildongmu.auth.exception.UserNotFoundException;
import com.dduru.gildongmu.auth.repository.UserRepository;
import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.dto.PostCreateRequest;
import com.dduru.gildongmu.post.dto.PostCreateResponse;
import com.dduru.gildongmu.post.enums.Destination;
import com.dduru.gildongmu.post.exception.DestinationNotFoundException;
import com.dduru.gildongmu.post.repository.DestinationRepository;
import com.dduru.gildongmu.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final DestinationRepository destinationRepository;
    private final JsonConverter jsonConverter;
    public PostCreateResponse create(Long userId,PostCreateRequest request) {
        log.debug("게시글 생성 시작 - userId: {}, request: {}", userId, request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다. userId: {}", userId);
                    return new UserNotFoundException(userId);
                });

        Destination destination = destinationRepository.findById(request.destinationId())
                .orElseThrow(() -> {
                    log.error("여행지를 찾을 수 없습니다. destinationId: {}", request.destinationId());
                    return new DestinationNotFoundException(request.destinationId());
                });

        Gender preferredGender = request.preferredGender() != null
                ? Gender.from(request.preferredGender())
                : Gender.U;

        AgeRange preferredAgeMin = request.preferredAgeMin() != null
                ? AgeRange.from(request.preferredAgeMin())
                : null;

        AgeRange preferredAgeMax = request.preferredAgeMax() != null
                ? AgeRange.from(request.preferredAgeMax())
                : null;

        String photoUrlsJson = jsonConverter.convertListToJson(request.photoUrls());
        String tagsJson = jsonConverter.convertListToJson(request.tags());

        Post post = Post.createPost(user, destination, request.title(), request.content(),
                request.startDate(), request.endDate(), request.recruitCapacity(), request.recruitDeadline(),
                preferredGender, preferredAgeMin, preferredAgeMax,
                request.budgetMin(), request.budgetMax(), photoUrlsJson, tagsJson);

        Post savedPost = postRepository.save(post);
        log.info("게시글 생성 완료 - postId: {}, userId: {}, title: {}",
                savedPost.getId(), userId, savedPost.getTitle());

        return PostCreateResponse.of(
                savedPost,
                user,
                destination,
                request.photoUrls(),
                request.tags()
        );
    }
}
