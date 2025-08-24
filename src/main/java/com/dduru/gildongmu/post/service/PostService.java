package com.dduru.gildongmu.post.service;

import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.destination.repository.DestinationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.dto.PostCreateRequest;
import com.dduru.gildongmu.post.dto.PostCreateResponse;
import com.dduru.gildongmu.post.dto.PostUpdateRequest;
import com.dduru.gildongmu.post.exception.InvalidAgeRangeException;
import com.dduru.gildongmu.post.exception.InvalidBudgetRangeException;
import com.dduru.gildongmu.post.exception.InvalidPostDateException;
import com.dduru.gildongmu.post.exception.PostAccessDeniedException;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.enums.AgeRange;
import com.dduru.gildongmu.user.enums.Gender;
import com.dduru.gildongmu.user.repository.UserRepository;
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
                request.budgetMin(), request.budgetMax(), request.preferredAgeMin(), request.preferredAgeMax());
        
        User user = userRepository.getByIdOrThrow(userId);
        Destination destination = destinationRepository.getByIdOrThrow(request.destinationId());
        
        Post post = createPost(user, destination, request);
        Post savedPost = postRepository.save(post);

        log.info("게시글 생성 완료 - postId: {}, userId: {}, title: {}",
                savedPost.getId(), userId, savedPost.getTitle());
        return new PostCreateResponse(savedPost.getId());
    }

    public void update(Long postId, Long userId, PostUpdateRequest request) {
        log.debug("게시글 수정 시작 - postId: {}, userId: {}, request: {}", postId, userId, request);

        Post post = postRepository.getActiveByIdOrThrow(postId);
        validatePermission(post, userId);
        validateBusinessRules(request.startDate(), request.endDate(), request.recruitDeadline(),
                request.budgetMin(), request.budgetMax(), request.preferredAgeMin(), request.preferredAgeMax());

        Destination destination = destinationRepository.getByIdOrThrow(request.destinationId());
        updatePost(post, destination, request);

        log.info("게시글 수정 완료 - postId: {}, userId: {}, title: {}",
                postId, userId, post.getTitle());
    }

    public void delete(Long postId, Long userId) {
        log.debug("게시글 삭제 시작 - postId: {}, userId: {}", postId, userId);

        Post post = postRepository.getActiveByIdOrThrow(postId);
        validatePermission(post, userId);

        post.softDelete(userId);

        log.info("게시글 삭제 완료 - postId: {}, userId: {}, title: {}",
                postId, userId, post.getTitle());
    }

    private void validatePermission(Post post, Long userId) {
        if (!post.getUser().getId().equals(userId)) {
            log.warn("게시글 권한 없음 - postId: {}, userId: {}, ownerId: {}", post.getId(), userId, post.getUser().getId());
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

    private Gender parsePreferredGender(String preferredGender) {
        if (preferredGender != null) {
            return Gender.from(preferredGender);
        }
        return Gender.U;
    }

    private AgeRange parsePreferredAgeMin(String preferredAgeMin) {
        if (preferredAgeMin != null) {
            return AgeRange.from(preferredAgeMin);
        }
        return null;
    }

    private AgeRange parsePreferredAgeMax(String preferredAgeMax) {
        if (preferredAgeMax != null) {
            return AgeRange.from(preferredAgeMax);
        }
        return null;
    }

    private String convertPhotoUrlsToJson(List<String> photoUrls) {
        return jsonConverter.convertListToJson(photoUrls);
    }

    private String convertTagsToJson(List<String> tags) {
        return jsonConverter.convertListToJson(tags);
    }

    public int closeExpiredPosts() {
        log.debug("만료된 게시글 상태 업데이트 시작");
        
        LocalDate today = LocalDate.now();
        List<Post> expiredPosts = postRepository.findExpiredOpenPosts(today);
        
        int updatedCount = 0;
        for (Post post : expiredPosts) {
            post.closeByRecruitmentDeadline();
            updatedCount++;
            log.debug("게시글 상태 CLOSED로 변경 - postId: {}, title: {}", post.getId(), post.getTitle());
        }
        
        log.info("만료된 게시글 {}개의 상태를 CLOSED로 변경 완료", updatedCount);
        return updatedCount;
    }


    private Post createPost(User user, Destination destination, PostCreateRequest request) {
        ParsedPostData parsed = parsePostData(request.preferredGender(), request.preferredAgeMin(), 
                request.preferredAgeMax(), request.photoUrls(), request.tags());
        
        return Post.createPost(user, destination, request.title(), request.content(),
                request.startDate(), request.endDate(), request.recruitCapacity(),
                request.recruitDeadline(), parsed.preferredGender(), parsed.preferredAgeMin(), 
                parsed.preferredAgeMax(), request.budgetMin(), request.budgetMax(),
                parsed.photoUrlsJson(), parsed.tagsJson());
    }

    private void updatePost(Post post, Destination destination, PostUpdateRequest request) {
        ParsedPostData parsed = parsePostData(request.preferredGender(), request.preferredAgeMin(),
                request.preferredAgeMax(), request.photoUrls(), request.tags());
        
        post.updatePost(destination, request.title(), request.content(),
                request.startDate(), request.endDate(), request.recruitCapacity(),
                request.recruitDeadline(), parsed.preferredGender(), parsed.preferredAgeMin(),
                parsed.preferredAgeMax(), request.budgetMin(), request.budgetMax(),
                parsed.photoUrlsJson(), parsed.tagsJson());
    }

    private ParsedPostData parsePostData(String preferredGender, String preferredAgeMin, 
                                       String preferredAgeMax, List<String> photoUrls, List<String> tags) {
        return new ParsedPostData(
                parsePreferredGender(preferredGender),
                parsePreferredAgeMin(preferredAgeMin),
                parsePreferredAgeMax(preferredAgeMax),
                convertPhotoUrlsToJson(photoUrls),
                convertTagsToJson(tags)
        );
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
