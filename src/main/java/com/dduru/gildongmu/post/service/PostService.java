package com.dduru.gildongmu.post.service;

import com.dduru.gildongmu.auth.domain.User;
import com.dduru.gildongmu.auth.enums.AgeRange;
import com.dduru.gildongmu.auth.enums.Gender;
import com.dduru.gildongmu.auth.exception.UserNotFoundException;
import com.dduru.gildongmu.auth.repository.UserRepository;
import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
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

        Destination destination = destinationRepository.findById(request.getDestinationId())
                .orElseThrow(() -> {
                    log.error("여행지를 찾을 수 없습니다. destinationId: {}", request.getDestinationId());
                    return new DestinationNotFoundException(request.getDestinationId());
                });

        Gender preferredGender = request.getPreferredGender() != null
                ? Gender.from(request.getPreferredGender())
                : Gender.U;

        AgeRange preferredAgeMin = request.getPreferredAgeMin() != null
                ? AgeRange.from(request.getPreferredAgeMin())
                : null;

        AgeRange preferredAgeMax = request.getPreferredAgeMax() != null
                ? AgeRange.from(request.getPreferredAgeMax())
                : null;

        String photoUrlsJson = jsonConverter.convertListToJson(request.getPhotoUrls());
        String tagsJson = jsonConverter.convertListToJson(request.getTags());

        Post post = Post.builder()
                .user(user)
                .destination(destination)
                .title(request.getTitle())
                .content(request.getContent())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .recruitCapacity(request.getRecruitCapacity())
                .recruitDeadline(request.getRecruitDeadline())
                .preferredGender(preferredGender)
                .preferredAgeMin(preferredAgeMin)
                .preferredAgeMax(preferredAgeMax)
                .budgetMin(request.getBudgetMin())
                .budgetMax(request.getBudgetMax())
                .photoUrls(photoUrlsJson)
                .tags(tagsJson)
                .build();

        Post savedPost = postRepository.save(post);
        log.info("게시글 생성 완료 - postId: {}, userId: {}, title: {}",
                savedPost.getId(), userId, savedPost.getTitle());

        return PostCreateResponse.from(
                savedPost,
                request.getPhotoUrls(),
                request.getTags()
        );
    }
}
