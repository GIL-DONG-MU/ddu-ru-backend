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
import com.dduru.gildongmu.post.dto.PostUpdateRequest;
import com.dduru.gildongmu.post.dto.PostUpdateResponse;
import com.dduru.gildongmu.post.enums.Destination;
import com.dduru.gildongmu.post.exception.*;
import com.dduru.gildongmu.post.repository.DestinationRepository;
import com.dduru.gildongmu.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final DestinationRepository destinationRepository;
    private final JsonConverter jsonConverter;

    public PostCreateResponse create(Long userId, PostCreateRequest request) {
        log.debug("게시글 생성 시작 - userId: {}, request: {}", userId, request);

        validateBusinessRules(request.startDate(), request.endDate(), request.recruitDeadline(),
                request.budgetMin(), request.budgetMax(),
                request.preferredAgeMin(), request.preferredAgeMax());

        User user = findUserById(userId);
        Destination destination = findDestinationById(request.destinationId());

        Gender preferredGender = parsePreferredGender(request.preferredGender());
        AgeRange preferredAgeMin = parsePreferredAgeMin(request.preferredAgeMin());
        AgeRange preferredAgeMax = parsePreferredAgeMax(request.preferredAgeMax());
        String photoUrlsJson = convertPhotoUrlsToJson(request.photoUrls());
        String tagsJson = convertTagsToJson(request.tags());

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

    public PostUpdateResponse update(Long postId, Long userId, PostUpdateRequest request) {
        log.debug("게시글 수정 시작 - postId: {}, userId: {}, request: {}", postId, userId, request);

        validateBusinessRules(request.startDate(), request.endDate(), request.recruitDeadline(),
                request.budgetMin(), request.budgetMax(),
                request.preferredAgeMin(), request.preferredAgeMax());

        Post post = findActivePostById(postId);
        validatePermission(post, userId);

        Destination destination = findDestinationById(request.destinationId());

        Gender preferredGender = parsePreferredGender(request.preferredGender());
        AgeRange preferredAgeMin = parsePreferredAgeMin(request.preferredAgeMin());
        AgeRange preferredAgeMax = parsePreferredAgeMax(request.preferredAgeMax());
        String photoUrlsJson = convertPhotoUrlsToJson(request.photoUrls());
        String tagsJson = convertTagsToJson(request.tags());

        try {
            post.updatePost(destination, request.title(), request.content(),
                    request.startDate(), request.endDate(), request.recruitCapacity(),
                    request.recruitDeadline(), preferredGender, preferredAgeMin, preferredAgeMax,
                    request.budgetMin(), request.budgetMax(), photoUrlsJson, tagsJson);


            log.info("게시글 수정 완료 - postId: {}, userId: {}, title: {}",
                    post.getId(), userId, post.getTitle());

            return PostUpdateResponse.of(
                    post,
                    post.getUser(),
                    destination,
                    request.photoUrls(),
                    request.tags()
            );

        } catch (InvalidRecruitCapacityException | TravelAlreadyStartedException e) {
            log.error("게시글 수정 중 비즈니스 규칙 위반 - postId: {}, error: {}", postId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("게시글 수정 중 예상치 못한 오류 발생 - postId: {}, error: {}", postId, e.getMessage(), e);
            throw new RuntimeException("게시글 수정 중 오류가 발생했습니다", e);
        }
    }

    public void delete(Long postId, Long userId) {
        log.debug("게시글 삭제 시작 - postId: {}, userId: {}", postId, userId);

        Post post = findActivePostById(postId);
        validatePermission(post, userId);

        try {
            post.softDelete(userId);
            postRepository.save(post);
            log.info("게시글 삭제 완료 - postId: {}, userId: {}, title: {}",
                    postId, userId, post.getTitle());

        } catch (Exception e) {
            log.error("게시글 삭제 중 오류 발생 - postId: {}, error: {}", postId, e.getMessage(), e);
            throw new RuntimeException("게시글 삭제 중 오류가 발생했습니다", e);
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다. userId: {}", userId);
                    return new UserNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId);
                });
    }

    private Destination findDestinationById(Long destinationId) {
        return destinationRepository.findById(destinationId)
                .orElseThrow(() -> {
                    log.error("여행지를 찾을 수 없습니다. destinationId: {}", destinationId);
                    return new DestinationNotFoundException("여행지를 찾을 수 없습니다. destinationId: " + destinationId);
                });
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("게시글을 찾을 수 없습니다. postId: {}", postId);
                    return new PostNotFoundException("게시글을 찾을 수 없습니다. postId: " + postId);
                });
    }

    private void validatePermission(Post post, Long userId) {
        if (!post.getUser().getId().equals(userId)) {
            log.error("게시글 권한이 없습니다. postId: {}, userId: {}, ownerId: {}",
                    post.getId(), userId, post.getUser().getId());
            throw new PostAccessDeniedException(
                    String.format("게시글 권한이 없습니다. postId: %d, userId: %d", post.getId(), userId)
            );
        }
    }

    private void validateBusinessRules(LocalDate startDate, LocalDate endDate, LocalDate recruitDeadline,
                                       Integer budgetMin, Integer budgetMax,
                                       String preferredAgeMin, String preferredAgeMax) {

        if (endDate.isBefore(startDate)) {
            throw new InvalidPostDateException("여행 종료일은 시작일 이후여야 합니다");
        }

        if (!recruitDeadline.isBefore(startDate)) {
            throw new InvalidPostDateException("모집 마감일은 여행 시작일 이전이어야 합니다");
        }

        if (budgetMin != null && budgetMax != null && budgetMax < budgetMin) {
            throw new InvalidBudgetRangeException("최대 예산은 최소 예산보다 커야 합니다");
        }

        if (preferredAgeMin != null && preferredAgeMax != null) {
            validateAgeRange(preferredAgeMin, preferredAgeMax);
        }
    }

    private void validateAgeRange(String ageMin, String ageMax) {
        try {
            AgeRange minAge = AgeRange.valueOf(ageMin);
            AgeRange maxAge = AgeRange.valueOf(ageMax);
            if (minAge.ordinal() > maxAge.ordinal()) {
                throw new InvalidAgeRangeException("최대 연령은 최소 연령보다 크거나 같아야 합니다");
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidAgeRangeException("연령대 값이 올바르지 않습니다");
        }
    }
    private Gender parsePreferredGender(String preferredGender) {
        return preferredGender != null
                ? Gender.from(preferredGender)
                : Gender.U;
    }

    private AgeRange parsePreferredAgeMin(String preferredAgeMin) {
        return preferredAgeMin != null
                ? AgeRange.from(preferredAgeMin)
                : null;
    }

    private AgeRange parsePreferredAgeMax(String preferredAgeMax) {
        return preferredAgeMax != null
                ? AgeRange.from(preferredAgeMax)
                : null;
    }

    private String convertPhotoUrlsToJson(List<String> photoUrls) {
        return jsonConverter.convertListToJson(photoUrls);
    }

    private String convertTagsToJson(List<String> tags) {
        return jsonConverter.convertListToJson(tags);
    }

    private Post findActivePostById(Long postId) {
        return postRepository.findActiveById(postId)
                .orElseThrow(() -> {
                    log.error("활성 게시글을 찾을 수 없습니다. postId: {}", postId);
                    return new PostNotFoundException("게시글을 찾을 수 없습니다. postId: " + postId);
                });
    }
}
