package com.dduru.gildongmu.post.service;

import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.destination.repository.DestinationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.dto.request.PostCreateRequest;
import com.dduru.gildongmu.post.dto.response.PostCreateResponse;
import com.dduru.gildongmu.post.dto.request.PostStatusUpdateRequest;
import com.dduru.gildongmu.post.dto.request.PostUpdateRequest;
import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.post.exception.InvalidAgeRangeException;
import com.dduru.gildongmu.post.exception.InvalidBudgetRangeException;
import com.dduru.gildongmu.post.exception.InvalidPostDateException;
import com.dduru.gildongmu.post.exception.PostAccessDeniedException;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.profile.domain.enums.AgeRange;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
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
        log.debug("게시글 생성 - userId={}", userId);

        validateBusinessRules(request.startDate(), request.endDate(), request.recruitDeadline(),
                request.budgetMin(), request.budgetMax(), request.preferredAgeMin(), request.preferredAgeMax());

        User user = userRepository.getByIdOrThrow(userId);
        Destination destination = destinationRepository.getByIdOrThrow(request.destinationId());

        Post post = createPost(user, destination, request, request.photoUrls());
        Post savedPost = postRepository.save(post);

        log.info("게시글 생성됨 - postId={}, userId={}", savedPost.getId(), userId);
        return new PostCreateResponse(savedPost.getId());
    }

    public void update(Long postId, Long userId, PostUpdateRequest request) {
        log.debug("게시글 수정 - postId={}, userId={}", postId, userId);

        Post post = postRepository.getActiveByIdOrThrow(postId);
        validatePermission(post, userId);
        validateBusinessRules(request.startDate(), request.endDate(), request.recruitDeadline(),
                request.budgetMin(), request.budgetMax(), request.preferredAgeMin(), request.preferredAgeMax());

        Destination destination = destinationRepository.getByIdOrThrow(request.destinationId());

        updatePost(post, destination, request, request.photoUrls());

        log.info("게시글 수정됨 - postId={}, userId={}", postId, userId);
    }

    public void delete(Long postId, Long userId) {
        log.debug("게시글 삭제 - postId={}, userId={}", postId, userId);

        Post post = postRepository.getActiveByIdOrThrow(postId);
        validatePermission(post, userId);

        post.softDelete(userId);

        log.info("게시글 삭제됨 - postId={}, userId={}", postId, userId);
    }

    public int closeExpiredPosts() {
        log.debug("만료 게시글 상태 업데이트 - 실행");
        LocalDate today = LocalDate.now();

        return postRepository.closeExpiredPostsByDate(today);
    }

    public void updateStatus(Long postId, Long userId, PostStatusUpdateRequest request) {
        log.debug("게시글 모집 상태 변경 - postId={}, userId={}", postId, userId);

        Post post = postRepository.getActiveByIdOrThrow(postId);
        validatePermission(post, userId);

        PostStatus newStatus = request.open() ? PostStatus.OPEN : PostStatus.CLOSED;
        post.updateStatus(newStatus);

        log.info("게시글 모집 상태 변경됨 - postId={}, status={}", postId, newStatus);
    }

    private void validatePermission(Post post, Long userId) {
        if (!post.getUser().getId().equals(userId)) {
            log.warn("게시글 권한 없음 - postId={}, userId={}", post.getId(), userId);
            throw PostAccessDeniedException.ownerOnly();
        }
    }

    private void validateBusinessRules(LocalDate startDate, LocalDate endDate, LocalDate recruitDeadline,
                                       Integer budgetMin, Integer budgetMax,
                                       String preferredAgeMin, String preferredAgeMax) {

        if (endDate.isBefore(startDate)) {
            throw InvalidPostDateException.endBeforeStart();
        }
        if (recruitDeadline.isAfter(startDate)) {
            throw InvalidPostDateException.deadlineAfterStart();
        }
        if (budgetMin != null && budgetMax != null && budgetMax < budgetMin) {
            throw new InvalidBudgetRangeException();
        }
        if (preferredAgeMin != null && preferredAgeMax != null) {
            validateAgeRange(preferredAgeMin, preferredAgeMax);
        }
    }

    private void validateAgeRange(String ageMin, String ageMax) {
        try {
            AgeRange minAge = AgeRange.from(ageMin);
            AgeRange maxAge = AgeRange.from(ageMax);
            if (minAge.ordinal() > maxAge.ordinal()) {
                throw InvalidAgeRangeException.maxLessThanMin();
            }
        } catch (IllegalArgumentException e) {
            throw InvalidAgeRangeException.invalidValue();
        }
    }

    private Post createPost(User user, Destination destination, PostCreateRequest request, List<String> photoUrls) {
        ParsedPostData parsed = parsePostData(request.preferredGender(), request.preferredAgeMin(),
                request.preferredAgeMax(), photoUrls, request.tags(), destination);

        return Post.createPost(user, destination, request.title(), request.content(),
                request.startDate(), request.endDate(), request.recruitCapacity(),
                request.recruitDeadline(), parsed.preferredGender(), parsed.preferredAgeMin(),
                parsed.preferredAgeMax(), request.budgetMin(), request.budgetMax(),
                parsed.photoUrlsJson(), parsed.tagsJson());
    }

    private void updatePost(Post post, Destination destination, PostUpdateRequest request, List<String> photoUrls) {
        ParsedPostData parsed = parsePostData(request.preferredGender(), request.preferredAgeMin(),
                request.preferredAgeMax(), photoUrls, request.tags(), destination);

        post.updatePost(destination, request.title(), request.content(),
                request.startDate(), request.endDate(), request.recruitCapacity(),
                request.recruitDeadline(), parsed.preferredGender(), parsed.preferredAgeMin(),
                parsed.preferredAgeMax(), request.budgetMin(), request.budgetMax(),
                parsed.photoUrlsJson(), parsed.tagsJson());
    }

    private ParsedPostData parsePostData(String preferredGender, String preferredAgeMin,
                                       String preferredAgeMax, List<String> photoUrls, List<String> tags, Destination destination) {

        List<String> finalPhotoUrls = getFinalPhotoUrls(photoUrls, destination);

        return new ParsedPostData(
                preferredGender != null ? Gender.from(preferredGender) : Gender.U,
                preferredAgeMin != null ? AgeRange.from(preferredAgeMin) : AgeRange.UNKNOWN,
                preferredAgeMax != null ? AgeRange.from(preferredAgeMax) : AgeRange.UNKNOWN,
                jsonConverter.convertListToJson(finalPhotoUrls),
                jsonConverter.convertListToJson(tags)
        );
    }

    private List<String> getFinalPhotoUrls(List<String> photoUrls, Destination destination) {
        if (photoUrls != null && !photoUrls.isEmpty()) {
            log.debug("이미지 소스 - 업로드, count={}", photoUrls.size());
            return photoUrls;
        }

        if (StringUtils.hasText(destination.getImage())) {
            log.debug("이미지 소스 - 목적지 기본, destination={}", destination.getCity());
            List<String> defaultImages = new ArrayList<>();
            defaultImages.add(destination.getImage());
            return defaultImages;
        }

        log.debug("이미지 소스 - 없음");
        return Collections.emptyList();
    }

    private record ParsedPostData(
            Gender preferredGender,
            AgeRange preferredAgeMin,
            AgeRange preferredAgeMax,
            String photoUrlsJson,
            String tagsJson
    ) {
    }
}
