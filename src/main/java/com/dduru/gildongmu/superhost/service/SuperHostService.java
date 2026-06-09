package com.dduru.gildongmu.superhost.service;

import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.post.dto.response.PostSummaryResponse;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.superhost.domain.SuperHostExposure;
import com.dduru.gildongmu.superhost.domain.SuperHostTicket;
import com.dduru.gildongmu.superhost.domain.enums.SuperHostExposureStatus;
import com.dduru.gildongmu.superhost.domain.enums.SuperHostTicketSource;
import com.dduru.gildongmu.superhost.domain.enums.SuperHostTicketStatus;
import com.dduru.gildongmu.superhost.dto.response.MySuperHostStatusResponse;
import com.dduru.gildongmu.superhost.dto.response.SuperHostApplyResponse;
import com.dduru.gildongmu.superhost.dto.response.SuperHostPostListResponse;
import com.dduru.gildongmu.superhost.exception.SuperHostAlreadyActiveException;
import com.dduru.gildongmu.superhost.exception.SuperHostPostNotApplicableException;
import com.dduru.gildongmu.superhost.exception.SuperHostTicketNotFoundException;
import com.dduru.gildongmu.superhost.repository.SuperHostExposureRepository;
import com.dduru.gildongmu.superhost.repository.SuperHostTicketRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SuperHostService {
    private static final int ONBOARDING_TICKET_BOOST_DAYS = 3;
    private static final int DEFAULT_LIST_SIZE = 10;
    private static final int MAX_LIST_SIZE = 20;

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SuperHostTicketRepository superHostTicketRepository;
    private final SuperHostExposureRepository superHostExposureRepository;
    private final ProfileImageResolver profileImageResolver;
    private final TimeProvider timeProvider;

    public void grantOnboardingRewardTicket(Long userId) {
        User user = userRepository.getByIdOrThrow(userId);
        SuperHostTicket ticket = SuperHostTicket.create(user, SuperHostTicketSource.ONBOARDING_SURVEY, ONBOARDING_TICKET_BOOST_DAYS);

        try {
            superHostTicketRepository.save(ticket);
            log.info("온보딩 보상 슈퍼호스트 티켓 지급 - userId={}", userId);
        } catch (DataIntegrityViolationException e) {
            if (isDuplicateOnboardingRewardViolation(e)) {
                log.info("온보딩 보상 슈퍼호스트 티켓이 이미 존재(동시 요청) - userId={}", userId);
                return;
            }
            throw e;
        }
    }

    public SuperHostApplyResponse applyTicketToPost(Long userId, Long postId) {
        Post post = getApplicablePostOrThrow(postId, userId);
        LocalDateTime now = timeProvider.now();
        validateNoActiveExposure(userId, now);
        SuperHostTicket ticket = getUnusedTicketOrThrow(userId);

        ticket.markUsed(now);
        LocalDateTime endsAt = now.plusDays(ticket.getBoostDurationDays());

        saveExposure(ticket, post, now, endsAt);
        log.info("슈퍼호스트 티켓 사용 완료 - userId={}, postId={}, ticketId={}", userId, postId, ticket.getId());

        int unusedTicketCount = getUnusedTicketCount(userId);
        return SuperHostApplyResponse.of(postId, ticket.getId(), now, endsAt, unusedTicketCount);
    }

    @Transactional(readOnly = true)
    public SuperHostPostListResponse retrieveSuperHostPosts(Integer size) {
        int requestSize = normalizeRequestSize(size);
        LocalDateTime now = timeProvider.now();
        LocalDate today = timeProvider.today();
        List<PostSummaryResponse> responses = superHostExposureRepository
                .findVisibleExposures(SuperHostExposureStatus.ACTIVE, now, PageRequest.of(0, requestSize))
                .stream()
                .map(exposure -> PostSummaryResponse.from(
                        exposure.getPost(),
                        profileImageResolver,
                        today,
                        true,
                        exposure.getEndedAt(),
                        false))
                .toList();

        return SuperHostPostListResponse.of(responses);
    }

    public int endExpiredExposures() {
        return superHostExposureRepository.endExpiredExposures(
                SuperHostExposureStatus.ACTIVE,
                SuperHostExposureStatus.ENDED,
                timeProvider.now()
        );
    }

    @Transactional(readOnly = true)
    public MySuperHostStatusResponse getMyStatus(Long userId) {
        LocalDateTime now = timeProvider.now();
        int unusedCount = getUnusedTicketCount(userId);

        return superHostExposureRepository
                .findFirstByUser_IdAndStatusAndEndedAtAfterOrderByStartedAtDesc(userId, SuperHostExposureStatus.ACTIVE, now)
                .map(exposure -> MySuperHostStatusResponse.of(
                        unusedCount,
                        true,
                        exposure.getPost().getId(),
                        exposure.getEndedAt()
                ))
                .orElseGet(() -> MySuperHostStatusResponse.of(unusedCount, false, null, null));
    }

    @Transactional(readOnly = true)
    public boolean isAuthorSuperHostForPost(Long postId) {
        return superHostExposureRepository.existsByPost_IdAndStatusAndEndedAtAfter(
                postId, SuperHostExposureStatus.ACTIVE, timeProvider.now()
        );
    }

    @Transactional(readOnly = true)
    public Map<Long, LocalDateTime> findActiveSuperHostExposures(Collection<Long> postIds) {
        if (postIds.isEmpty()) return Map.of();
        return superHostExposureRepository
                .findActivePostIdsWithEndedAt(postIds, SuperHostExposureStatus.ACTIVE, timeProvider.now())
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (LocalDateTime) row[1]
                ));
    }

    public void cancelActiveExposureByPostId(Long postId) {
        int cancelledCount = superHostExposureRepository.cancelActiveExposureByPostId(
                postId,
                SuperHostExposureStatus.ACTIVE,
                SuperHostExposureStatus.CANCELLED,
                timeProvider.now()
        );
        if (cancelledCount > 0) {
            log.info("게시글 상태 변경으로 슈퍼호스트 노출 취소 - postId={}, count={}", postId, cancelledCount);
        }
    }

    public void cancelActiveExposureByClosedPosts() {
        int cancelledCount = superHostExposureRepository.cancelActiveExposureByClosedPosts(
                SuperHostExposureStatus.ACTIVE,
                SuperHostExposureStatus.CANCELLED,
                timeProvider.now()
        );
        if (cancelledCount > 0) {
            log.info("자동 마감된 게시글의 슈퍼호스트 노출 취소 - count={}", cancelledCount);
        }
    }

    private Post getApplicablePostOrThrow(Long postId, Long userId) {
        return postRepository.findSuperHostApplicableByIdAndUserId(postId, userId, PostStatus.OPEN)
                .orElseThrow(SuperHostPostNotApplicableException::new);
    }

    private void validateNoActiveExposure(Long userId, LocalDateTime now) {
        boolean hasActiveExposure = superHostExposureRepository.existsByUser_IdAndStatusAndEndedAtAfter(
                userId,
                SuperHostExposureStatus.ACTIVE,
                now
        );
        if (hasActiveExposure) {
            throw new SuperHostAlreadyActiveException();
        }
    }

    private SuperHostTicket getUnusedTicketOrThrow(Long userId) {
        return superHostTicketRepository
                .findFirstByUser_IdAndStatusOrderByIdAsc(userId, SuperHostTicketStatus.UNUSED)
                .orElseThrow(SuperHostTicketNotFoundException::new);
    }

    private void saveExposure(SuperHostTicket ticket, Post post, LocalDateTime startedAt, LocalDateTime endedAt) {
        SuperHostExposure exposure = SuperHostExposure.create(ticket, post.getUser(), post, startedAt, endedAt);
        superHostExposureRepository.save(exposure);
    }

    private int getUnusedTicketCount(Long userId) {
        return Math.toIntExact(
                superHostTicketRepository.countByUser_IdAndStatus(userId, SuperHostTicketStatus.UNUSED)
        );
    }

    private int normalizeRequestSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_LIST_SIZE;
        }
        return Math.min(size, MAX_LIST_SIZE);
    }

    private static final String UK_SUPER_HOST_TICKETS_USER_SOURCE = "uk_super_host_tickets_user_source";

    private boolean isDuplicateOnboardingRewardViolation(DataIntegrityViolationException e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof DuplicateKeyException) {
                String message = t.getMessage();
                return message != null && message.contains(UK_SUPER_HOST_TICKETS_USER_SOURCE);
            }
        }
        return false;
    }
}
