package com.dduru.gildongmu.post.service;

import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.destination.repository.DestinationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.post.domain.enums.RecruitMethod;
import com.dduru.gildongmu.post.domain.enums.RecruitType;
import com.dduru.gildongmu.post.dto.ParsedPostData;
import com.dduru.gildongmu.post.dto.request.PostCreateRequest;
import com.dduru.gildongmu.post.dto.request.PostStatusUpdateRequest;
import com.dduru.gildongmu.post.dto.request.PostUpdateRequest;
import com.dduru.gildongmu.post.dto.response.ParticipantInfo;
import com.dduru.gildongmu.post.dto.response.PostCreateResponse;
import com.dduru.gildongmu.post.dto.response.PostDetailResponse;
import com.dduru.gildongmu.post.dto.response.MyParticipationStatus;
import com.dduru.gildongmu.post.exception.InvalidPostDateException;
import com.dduru.gildongmu.post.exception.InvalidPostStatusException;
import com.dduru.gildongmu.post.exception.InvalidRecruitSettingsException;
import com.dduru.gildongmu.post.exception.PostAccessDeniedException;
import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.repository.ParticipationRepository;
import com.dduru.gildongmu.like.repository.PostLikeRepository;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    private final ParticipationRepository participationRepository;
    private final PostLikeRepository postLikeRepository;
    private final JsonConverter jsonConverter;

    public PostCreateResponse create(Long userId, PostCreateRequest request) {
        log.debug("게시글 생성 - userId={}", userId);

        validateBusinessRules(request.startDate(), request.endDate(), request.recruitDeadline(),
                request.recruitMethod(), request.recruitType(), request.companionType());

        User user = userRepository.getByIdOrThrow(userId);
        Destination destination = destinationRepository.getByIdOrThrow(request.destinationId());

        Post post = createPost(user, destination, request, request.photoUrls());
        Post savedPost = postRepository.save(post);

        log.info("게시글 생성됨 - postId={}, userId={}", savedPost.getId(), userId);
        return new PostCreateResponse(savedPost.getId());
    }

    public void update(Long postId, Long userId, PostUpdateRequest request) {
        log.debug("게시글 수정 - postId={}, userId={}", postId, userId);

        Post post = getPostAndValidateOwner(postId, userId);

        LocalDate effectiveStartDate = request.startDate() != null ? request.startDate() : post.getStartDate();
        LocalDate effectiveEndDate = request.endDate() != null ? request.endDate() : post.getEndDate();
        LocalDate effectiveRecruitDeadline = request.recruitDeadline() != null ? request.recruitDeadline() : post.getRecruitDeadline();
        RecruitMethod effectiveRecruitMethod = request.recruitMethod() != null ? request.recruitMethod() : post.getRecruitMethod();
        RecruitType effectiveRecruitType = request.recruitType() != null ? request.recruitType() : post.getRecruitType();
        CompanionType effectiveCompanionType = request.companionType() != null ? request.companionType() : post.getCompanionType();

        validateBusinessRules(effectiveStartDate, effectiveEndDate, effectiveRecruitDeadline,
                effectiveRecruitMethod, effectiveRecruitType, effectiveCompanionType);

        LocalDate recruitDeadlineToSave = resolveRecruitDeadline(
                effectiveRecruitMethod, effectiveRecruitDeadline, effectiveStartDate);

        Destination destination = request.destinationId() != null
                ? destinationRepository.getByIdOrThrow(request.destinationId())
                : null;

        updatePost(post, destination, request, recruitDeadlineToSave);

        log.info("게시글 수정됨 - postId={}, userId={}", postId, userId);
    }

    public void delete(Long postId, Long userId) {
        log.debug("게시글 삭제 - postId={}, userId={}", postId, userId);

        Post post = getPostAndValidateOwner(postId, userId);

        post.softDelete(userId);

        log.info("게시글 삭제됨 - postId={}, userId={}", postId, userId);
    }

    public int closeExpiredPosts() {
        log.debug("만료 게시글 상태 업데이트 - 실행");
        LocalDate today = LocalDate.now();

        return postRepository.closeExpiredPostsByDate(today);
    }

    public PostDetailResponse recordViewAndGetDetail(Long postId, Long currentUserId) {
        log.debug("게시글 상세 조회(조회수 증가) - postId={}", postId);

        postRepository.incrementViewCount(postId);
        Post post = postRepository.getActiveByIdOrThrow(postId);

        boolean isOwner = currentUserId != null && currentUserId.equals(post.getUser().getId());
        boolean hasLiked = currentUserId != null && postLikeRepository.existsByUserIdAndPostId(currentUserId, postId);
        List<ParticipantInfo> participants = buildParticipants(post);
        MyParticipationStatus myParticipationStatus = resolveMyParticipationStatus(postId, currentUserId, isOwner);
        PostDetailResponse response = PostDetailResponse.from(
                post,
                jsonConverter,
                isOwner,
                hasLiked,
                participants,
                myParticipationStatus
        );
        log.debug("게시글 상세 조회 완료 - postId={}", postId);
        return response;
    }

    public void updateStatus(Long postId, Long userId, PostStatusUpdateRequest request) {
        log.debug("게시글 모집 상태 변경 - postId={}, userId={}", postId, userId);

        Post post = getPostAndValidateOwner(postId, userId);

        PostStatus newStatus = request.open() ? PostStatus.OPEN : PostStatus.CLOSED;
        try {
            post.updateStatus(newStatus);
        } catch (InvalidPostStatusException e) {
            log.warn("게시글 모집 상태 변경 불가 - postId={}, userId={}, currentStatus={}, requestedStatus={}",
                    postId, userId, post.getStatus(), newStatus);
            throw e;
        }

        log.info("게시글 모집 상태 변경됨 - postId={}, status={}", postId, newStatus);
    }

    private Post getPostAndValidateOwner(Long postId, Long userId) {
        Post post = postRepository.getActiveByIdOrThrow(postId);
        if (!post.getUser().getId().equals(userId)) {
            log.warn("게시글 권한 없음 - postId={}, userId={}", post.getId(), userId);
            throw PostAccessDeniedException.ownerOnly();
        }
        return post;
    }

    private MyParticipationStatus resolveMyParticipationStatus(Long postId, Long currentUserId, boolean isOwner) {
        if (currentUserId == null || isOwner) {
            return MyParticipationStatus.NONE;
        }

        return participationRepository.findByPostIdAndUserId(postId, currentUserId)
                .map(Participation::getStatus)
                .map(status -> switch (status) {
                    case PENDING -> MyParticipationStatus.PENDING;
                    case APPROVED -> MyParticipationStatus.APPROVED;
                    case REJECTED -> MyParticipationStatus.REJECTED;
                })
                .orElse(MyParticipationStatus.NONE);
    }

    private List<ParticipantInfo> buildParticipants(Post post) {
        List<ParticipantInfo> result = new ArrayList<>();
        result.add(ParticipantInfo.from(post.getUser(), true));
        participationRepository.findByPostIdOrderByCreatedAtAsc(post.getId()).stream()
                .filter(Participation::isApproved)
                .map(p -> ParticipantInfo.from(p.getUser(), false))
                .forEach(result::add);
        return result;
    }

    private void validateBusinessRules(LocalDate startDate, LocalDate endDate, LocalDate recruitDeadline,
                                       RecruitMethod recruitMethod, RecruitType recruitType, CompanionType companionType) {
        if (endDate.isBefore(startDate)) {
            throw InvalidPostDateException.endBeforeStart();
        }
        if (recruitMethod == RecruitMethod.PERIOD) {
            if (recruitDeadline == null) {
                throw InvalidPostDateException.invalidRecruitPeriod();
            }
            if (recruitDeadline.isAfter(startDate)) {
                throw InvalidPostDateException.deadlineAfterStart();
            }
            long recruitPeriodDays = ChronoUnit.DAYS.between(LocalDate.now(), recruitDeadline) + 1;
            if (recruitPeriodDays < 1 || recruitPeriodDays > 30) {
                throw InvalidPostDateException.invalidRecruitPeriod();
            }
        }
        if (recruitType == RecruitType.PUBLIC && companionType == null) {
            throw new InvalidRecruitSettingsException();
        }
    }

    private Post createPost(User user, Destination destination, PostCreateRequest request, List<String> photoUrls) {
        ParsedPostData parsed = parsePostData(photoUrls, request.tags(), destination);
        LocalDate recruitDeadlineToSave = resolveRecruitDeadline(
                request.recruitMethod(), request.recruitDeadline(), request.startDate());

        return Post.createPost(user, destination, request.title(), request.content(),
                request.startDate(), request.endDate(), request.recruitCapacity(),
                recruitDeadlineToSave, request.preferredGender(), request.preferredAges(),
                parsed.photoUrlsJson(), parsed.tagsJson(), request.recruitType(), request.recruitMethod(), request.companionType());
    }

    private LocalDate resolveRecruitDeadline(RecruitMethod method, LocalDate recruitDeadline, LocalDate startDate) {
        if (method == RecruitMethod.ALWAYS && recruitDeadline == null) {
            return startDate;
        }
        return recruitDeadline;
    }

    private void updatePost(Post post, Destination destination, PostUpdateRequest request, LocalDate recruitDeadlineToSave) {
        Destination destinationForParse = destination != null ? destination : post.getDestination();
        String photoUrlsJson = request.photoUrls() != null
                ? jsonConverter.convertListToJson(getFinalPhotoUrls(request.photoUrls(), destinationForParse))
                : null;
        String tagsJson = request.tags() != null ? jsonConverter.convertListToJson(request.tags()) : null;

        post.updatePost(destination, request.title(), request.content(),
                request.startDate(), request.endDate(), request.recruitCapacity(),
                recruitDeadlineToSave, request.preferredGender(), request.preferredAges(),
                photoUrlsJson, tagsJson, request.recruitType(), request.recruitMethod(), request.companionType());
    }

    private ParsedPostData parsePostData(List<String> photoUrls, List<String> tags, Destination destination) {
        List<String> finalPhotoUrls = getFinalPhotoUrls(photoUrls, destination);

        return new ParsedPostData(
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
            return List.of(destination.getImage());
        }

        log.debug("이미지 소스 - 없음");
        return Collections.emptyList();
    }
}
