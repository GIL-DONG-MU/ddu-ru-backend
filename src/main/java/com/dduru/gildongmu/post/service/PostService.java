package com.dduru.gildongmu.post.service;

import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.destination.repository.DestinationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.post.dto.request.PostCreateRequest;
import com.dduru.gildongmu.post.dto.request.PostStatusUpdateRequest;
import com.dduru.gildongmu.post.dto.request.PostUpdateRequest;
import com.dduru.gildongmu.post.dto.response.ParticipantInfo;
import com.dduru.gildongmu.post.dto.response.PostCreateResponse;
import com.dduru.gildongmu.post.dto.response.PostDetailResponse;
import com.dduru.gildongmu.post.dto.response.MyParticipationStatus;
import com.dduru.gildongmu.post.exception.InvalidPostDateException;
import com.dduru.gildongmu.post.exception.InvalidPreferredAgeException;
import com.dduru.gildongmu.post.exception.InvalidPostStatusException;
import com.dduru.gildongmu.post.exception.PostAccessDeniedException;
import com.dduru.gildongmu.tag.service.TagValidator;
import com.dduru.gildongmu.participation.service.ParticipationService;
import com.dduru.gildongmu.like.repository.PostLikeRepository;
import com.dduru.gildongmu.chat.service.ChatRoomService;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.profile.service.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private static final int MIN_PREFERRED_AGE = 20;
    private static final int MAX_PREFERRED_AGE = 100;

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final DestinationRepository destinationRepository;
    private final ParticipationService participationService;
    private final PostLikeRepository postLikeRepository;
    private final JsonConverter jsonConverter;
    private final ProfileImageResolver profileImageResolver;
    private final ChatRoomService chatRoomService;

    public PostCreateResponse create(Long userId, PostCreateRequest request) {
        log.debug("게시글 생성 - userId={}", userId);

        validateCreateRequest(request);

        User user = userRepository.getByIdOrThrow(userId);
        Destination destination = destinationRepository.getByIdOrThrow(request.destinationId());

        Post post = createPost(user, destination, request);
        Post savedPost = postRepository.save(post);
        chatRoomService.createPendingGroupRoomForPost(savedPost, user);

        log.info("게시글 생성됨 - postId={}, userId={}", savedPost.getId(), userId);
        return new PostCreateResponse(savedPost.getId());
    }

    public void update(Long postId, Long userId, PostUpdateRequest request) {
        // TODO: 게시글 수정 시 그룹 채팅방 maxCapacity를 recruitCapacity + 1로 동기화
        log.debug("게시글 수정 - postId={}, userId={}", postId, userId);

        Post post = getOwnedPost(postId, userId);
        validateUpdateRequest(post, request);

        Destination destination = getDestinationOrNull(request);
        LocalDate recruitDeadline = calculateRecruitDeadline(getEndDateOrCurrent(post, request));

        updatePost(post, destination, request, recruitDeadline);

        log.info("게시글 수정됨 - postId={}, userId={}", postId, userId);
    }

    public void delete(Long postId, Long userId) {
        log.debug("게시글 삭제 - postId={}, userId={}", postId, userId);

        Post post = getOwnedPost(postId, userId);

        post.softDelete(userId);

        log.info("게시글 삭제됨 - postId={}, userId={}", postId, userId);
    }

    public int closeExpiredPosts() {
        log.debug("만료 게시글 상태 업데이트 - 실행");
        return postRepository.closeExpiredPostsByDate(LocalDate.now());
    }

    public PostDetailResponse recordViewAndGetDetail(Long postId, Long currentUserId) {
        log.debug("게시글 상세 조회(조회수 증가) - postId={}", postId);

        postRepository.incrementViewCount(postId);
        Post post = postRepository.getActiveByIdOrThrow(postId);

        boolean isOwner = isOwner(post, currentUserId);
        boolean hasLiked = hasLiked(postId, currentUserId);

        List<ParticipantInfo> participants = participationService.getParticipantsForPostDetail(post);
        MyParticipationStatus myParticipationStatus = participationService.getMyParticipationStatus(postId, currentUserId, isOwner);
        PostDetailResponse response = PostDetailResponse.from(
                post,
                jsonConverter,
                isOwner,
                hasLiked,
                participants,
                myParticipationStatus,
                profileImageResolver
        );
        log.debug("게시글 상세 조회 완료 - postId={}", postId);
        return response;
    }

    public void changeStatus(Long postId, Long userId, PostStatusUpdateRequest request) {
        log.debug("게시글 모집 상태 변경 - postId={}, userId={}", postId, userId);

        Post post = getOwnedPost(postId, userId);

        PostStatus newStatus = request.open() ? PostStatus.OPEN : PostStatus.CLOSED;
        try {
            post.changeStatus(newStatus);
        } catch (InvalidPostStatusException e) {
            log.warn("게시글 모집 상태 변경 불가 - postId={}, userId={}, currentStatus={}, requestedStatus={}",
                    postId, userId, post.getStatus(), newStatus);
            throw e;
        }

        log.info("게시글 모집 상태 변경됨 - postId={}, status={}", postId, newStatus);
    }

    private Post getOwnedPost(Long postId, Long userId) {
        Post post = postRepository.getActiveByIdOrThrow(postId);
        if (!post.getUser().getId().equals(userId)) {
            log.warn("게시글 권한 없음 - postId={}, userId={}", post.getId(), userId);
            throw PostAccessDeniedException.ownerOnly();
        }
        return post;
    }

    private boolean isOwner(Post post, Long currentUserId) {
        return currentUserId != null && currentUserId.equals(post.getUser().getId());
    }

    private boolean hasLiked(Long postId, Long currentUserId) {
        return currentUserId != null && postLikeRepository.existsByUserIdAndPostId(currentUserId, postId);
    }

    private void validateCreateRequest(PostCreateRequest request) {
        validateDateRange(request.startDate(), request.endDate());
        validatePreferredAge(request.isAgeAny(), request.minAge(), request.maxAge());
        TagValidator.validateOrThrow(jsonConverter.normalizeTagList(request.tags()));
    }

    private void validateUpdateRequest(Post post, PostUpdateRequest request) {
        validateDateRange(getStartDateOrCurrent(post, request), getEndDateOrCurrent(post, request));

        if (hasAgePatch(request)) {
            validatePreferredAge(Boolean.TRUE.equals(request.isAgeAny()), request.minAge(), request.maxAge());
        }

        if (request.tags() != null) {
            TagValidator.validateOrThrow(jsonConverter.normalizeTagList(request.tags()));
        }
    }

    private void validatePreferredAge(boolean isAgeAny, Integer minAge, Integer maxAge) {
        if (isAgeAny) {
            if (minAge != null || maxAge != null) {
                throw InvalidPreferredAgeException.conflictWithAgeAny();
            }
            return;
        }

        validatePreferredAgeRange(minAge, maxAge);
    }

    private void validatePreferredAgeRange(Integer minAge, Integer maxAge) {
        if (minAge == null || maxAge == null) {
            throw InvalidPreferredAgeException.incompleteRange();
        }

        if (minAge < MIN_PREFERRED_AGE || maxAge > MAX_PREFERRED_AGE || minAge > maxAge) {
            throw InvalidPreferredAgeException.outOfBounds(MIN_PREFERRED_AGE, MAX_PREFERRED_AGE);
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw InvalidPostDateException.endBeforeStart();
        }
    }

    private LocalDate getStartDateOrCurrent(Post post, PostUpdateRequest request) {
        return request.startDate() != null ? request.startDate() : post.getStartDate();
    }

    private LocalDate getEndDateOrCurrent(Post post, PostUpdateRequest request) {
        return request.endDate() != null ? request.endDate() : post.getEndDate();
    }

    private boolean hasAgePatch(PostUpdateRequest request) {
        return request.isAgeAny() != null || request.minAge() != null || request.maxAge() != null;
    }

    private LocalDate calculateRecruitDeadline(LocalDate endDate) {
        return endDate.minusDays(1);
    }

    private Destination getDestinationOrNull(PostUpdateRequest request) {
        if (request.destinationId() == null) {
            return null;
        }
        return destinationRepository.getByIdOrThrow(request.destinationId());
    }

    private Post createPost(User user, Destination destination, PostCreateRequest request) {
        boolean isAgeAny = request.isAgeAny();
        LocalDate recruitDeadline = calculateRecruitDeadline(request.endDate());

        return Post.createPost(user, destination, request.title(), request.content(),
                request.startDate(), request.endDate(), request.recruitCapacity(),
                recruitDeadline, request.preferredGender(), isAgeAny,
                isAgeAny ? null : request.minAge(),
                isAgeAny ? null : request.maxAge(),
                resolvePhotoUrl(request.photoUrl(), destination),
                tagsToJson(request.tags()),
                request.companionType()
        );
    }

    private void updatePost(Post post, Destination destination, PostUpdateRequest request, LocalDate recruitDeadline) {
        boolean applyPreferredAgePatch = hasAgePatch(request);
        boolean preferredAgeAny = applyPreferredAgePatch && Boolean.TRUE.equals(request.isAgeAny());

        Integer minAge = preferredAgeAny ? null : request.minAge();
        Integer maxAge = preferredAgeAny ? null : request.maxAge();

        boolean applyPhotoUrlPatch = request.photoUrl() != null;
        Destination destinationForPhoto = destination != null ? destination : post.getDestination();

        String photoUrl = applyPhotoUrlPatch
                ? resolvePhotoUrl(request.photoUrl(), destinationForPhoto)
                : null;

        String tagsJson = request.tags() != null ? tagsToJson(request.tags()) : null;

        post.updatePost(destination, request.title(), request.content(),
                request.startDate(), request.endDate(), request.recruitCapacity(),
                recruitDeadline, request.preferredGender(), applyPreferredAgePatch,
                preferredAgeAny, minAge, maxAge,
                applyPhotoUrlPatch, photoUrl, tagsJson, request.companionType());
    }

    private String resolvePhotoUrl(String photoUrl, Destination destination) {
        if (StringUtils.hasText(photoUrl)) {
            log.debug("이미지 소스 - 업로드");
            return photoUrl.trim();
        }

        if (destination != null && StringUtils.hasText(destination.getImage())) {
            log.debug("이미지 소스 - 목적지 기본, destination={}", destination.getCity());
            return destination.getImage();
        }

        log.debug("이미지 소스 - 없음");
        return null;
    }

    private String tagsToJson(List<String> tags) {
        return jsonConverter.convertTagListToJson(tags);
    }
}
