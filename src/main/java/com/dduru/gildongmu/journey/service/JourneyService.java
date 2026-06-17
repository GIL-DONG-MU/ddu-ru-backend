package com.dduru.gildongmu.journey.service;

import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.event.ChatMemberChangeType;
import com.dduru.gildongmu.chat.event.ChatMemberChangedEvent;
import com.dduru.gildongmu.chat.event.JourneyBasicInfoUpdatedEvent;
import com.dduru.gildongmu.chat.repository.ChatRoomMemberRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.chat.service.ChatMessageSendService;
import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.common.validation.InvalidImageUrlException;
import com.dduru.gildongmu.common.validation.S3ImageUrlValidator;
import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.journey.domain.JourneyMember;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.dto.request.JourneyMemberRoleUpdateRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyMemberRoleResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyUpdateResponse;
import com.dduru.gildongmu.journey.exception.InvalidJourneyBasicInfoException;
import com.dduru.gildongmu.journey.exception.JourneyAccessDeniedException;
import com.dduru.gildongmu.journey.exception.JourneyMemberNotFoundException;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.journey.repository.JourneyRepository;
import com.dduru.gildongmu.journey.repository.JourneyScheduleRepository;
import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import com.dduru.gildongmu.participation.repository.ParticipationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.s3.enums.S3ImageDirectory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class JourneyService {
    private static final int TITLE_MIN_LENGTH = 5;
    private static final int TITLE_MAX_LENGTH = 40;

    private final JourneyRepository journeyRepository;
    private final JourneyMemberRepository journeyMemberRepository;
    private final JourneyScheduleRepository journeyScheduleRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ParticipationRepository participationRepository;
    private final PostRepository postRepository;
    private final ChatMessageSendService chatMessageSendService;
    private final S3ImageUrlValidator s3ImageUrlValidator;
    private final TimeProvider timeProvider;
    private final ApplicationEventPublisher eventPublisher;

    public JourneyUpdateResponse updateBasicInfo(Long journeyId, Long userId, JourneyUpdateRequest request) {
        Journey journey = getUpdatableJourney(journeyId, userId);

        String title = normalizeTitle(request.title());
        String photoUrl = normalizePhotoUrl(request.photoUrl());
        LocalDate startDate = request.startDate();
        LocalDate endDate = request.endDate();
        validateHasAnyPatch(title, photoUrl, startDate, endDate);
        validateTravelDatePatch(startDate, endDate);

        journey.updateBasicInfo(title, photoUrl);
        if (startDate != null) {
            deleteOutOfRangeSchedules(journeyId, userId, startDate, endDate);
            journey.getPost().updateTravelDates(startDate, endDate);
        }
        eventPublisher.publishEvent(new JourneyBasicInfoUpdatedEvent(journeyId));

        log.info("나의 여정 기본 정보 수정됨 - journeyId={}, userId={}", journeyId, userId);
        return JourneyUpdateResponse.from(journey);
    }

    public JourneyMemberRoleResponse updateMemberRole(
            Long journeyId,
            Long hostUserId,
            Long memberUserId,
            JourneyMemberRoleUpdateRequest request
    ) {
        validateActiveHost(journeyId, hostUserId);
        JourneyMember member = findActiveMemberOrThrow(journeyId, memberUserId);
        member.updateRole(request.roleType(), normalizeCustomRoleLabel(request.customRoleLabel()));
        log.info("나의 여정 멤버 역할 지정됨 - journeyId={}, hostUserId={}, memberUserId={}, roleType={}",
                journeyId, hostUserId, memberUserId, request.roleType());
        return JourneyMemberRoleResponse.from(member);
    }

    public void clearMemberRole(Long journeyId, Long hostUserId, Long memberUserId) {
        validateActiveHost(journeyId, hostUserId);
        JourneyMember member = findActiveMemberOrThrow(journeyId, memberUserId);
        member.clearRole();
        log.info("나의 여정 멤버 역할 해제됨 - journeyId={}, hostUserId={}, memberUserId={}",
                journeyId, hostUserId, memberUserId);
    }

    public void removeMember(Long journeyId, Long hostUserId, Long memberUserId) {
        validateActiveHost(journeyId, hostUserId);

        Post post = getPostForMemberRemoval(journeyId);
        JourneyMember member = getActiveMemberForUpdate(journeyId, memberUserId);
        validateRemovableMember(member);

        Long postId = post.getId();
        member.remove(timeProvider.now());
        // 신청 승인 이력은 유지하고, 현재 멤버십에 맞춰 모집 인원만 줄인다.
        participationRepository.findByPostIdAndUserIdAndStatus(postId, memberUserId, ParticipationStatus.APPROVED)
                .ifPresent(post::decrementRecruitCountIfApproved);
        findGroupChatRoomAndRemoveMember(journeyId, hostUserId, memberUserId);

        log.info("나의 여정 멤버 내보내기 완료 - journeyId={}, hostUserId={}, memberUserId={}",
                journeyId, hostUserId, memberUserId);
    }

    private Journey getUpdatableJourney(Long journeyId, Long userId) {
        return journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE)
                .orElseThrow(JourneyAccessDeniedException::new);
    }

    private void validateActiveHost(Long journeyId, Long hostUserId) {
        boolean isActiveHost = journeyMemberRepository.existsActiveHost(journeyId, hostUserId);
        if (!isActiveHost) {
            throw new JourneyAccessDeniedException();
        }
    }

    private Post getPostForMemberRemoval(Long journeyId) {
        Long postId = journeyRepository.getPostIdByIdOrThrow(journeyId);
        return postRepository.getActiveByIdWithLockOrThrow(postId);
    }

    private JourneyMember getActiveMemberForUpdate(Long journeyId, Long memberUserId) {
        return journeyMemberRepository.findActiveMemberWithLock(journeyId, memberUserId)
                .orElseThrow(JourneyAccessDeniedException::new);
    }

    private static void validateRemovableMember(JourneyMember member) {
        if (member.isHost()) {
            throw new JourneyAccessDeniedException();
        }
    }

    private void findGroupChatRoomAndRemoveMember(Long journeyId, Long hostUserId, Long memberUserId) {
        // 그룹 채팅방 멤버십은 존재할 때만 함께 정리한다.
        chatRoomRepository.findByJourneyIdAndRoomType(journeyId, ChatRoomType.GROUP)
                .ifPresent(chatRoom -> removeMemberFromGroupChatRoom(chatRoom, hostUserId, memberUserId));
    }

    private void removeMemberFromGroupChatRoom(ChatRoom chatRoom, Long hostUserId, Long memberUserId) {
        chatRoomMemberRepository.findByRoomIdAndUserId(chatRoom.getId(), memberUserId)
                .ifPresent(chatRoomMember -> {
                    chatRoomMemberRepository.delete(chatRoomMember);
                    eventPublisher.publishEvent(new ChatMemberChangedEvent(
                            chatRoom.getId(),
                            memberUserId,
                            ChatMemberChangeType.MEMBER_REMOVED
                    ));
                    chatMessageSendService.publishUserKicked(chatRoom, memberUserId, hostUserId);
                });
    }

    private void validateHasAnyPatch(String title, String photoUrl, LocalDate startDate, LocalDate endDate) {
        if (title == null && photoUrl == null && startDate == null && endDate == null) {
            throw InvalidJourneyBasicInfoException.emptyPatch();
        }
    }

    private static void validateTravelDatePatch(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return;
        }
        if (startDate == null || endDate == null) {
            throw InvalidJourneyBasicInfoException.incompleteTravelDate();
        }
        if (endDate.isBefore(startDate)) {
            throw InvalidJourneyBasicInfoException.invalidTravelDate();
        }
    }

    private void deleteOutOfRangeSchedules(Long journeyId, Long userId, LocalDate startDate, LocalDate endDate) {
        int newTotalDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        journeyScheduleRepository.findActiveSchedulesWithDayOffsetGreaterThanOrEqual(journeyId, newTotalDays)
                .forEach(schedule -> schedule.delete(userId, timeProvider.now()));
    }

    private String normalizeTitle(String title) {
        if (!StringUtils.hasText(title)) {
            return null;
        }
        String normalizedTitle = title.trim();
        validateTitleLength(normalizedTitle);
        return normalizedTitle;
    }

    private void validateTitleLength(String title) {
        int length = title.codePointCount(0, title.length());
        if (length < TITLE_MIN_LENGTH || length > TITLE_MAX_LENGTH) {
            throw InvalidJourneyBasicInfoException.invalidTitleLength();
        }
    }

    private static String normalizeCustomRoleLabel(String label) {
        if (!StringUtils.hasText(label)) {
            return null;
        }
        return label.trim();
    }

    private JourneyMember findActiveMemberOrThrow(Long journeyId, Long memberUserId) {
        return journeyMemberRepository.findByJourneyIdAndUserId(journeyId, memberUserId)
                .filter(m -> m.getStatus() == JourneyMemberStatus.ACTIVE)
                .orElseThrow(JourneyMemberNotFoundException::new);
    }

    private String normalizePhotoUrl(String photoUrl) {
        if (!StringUtils.hasText(photoUrl)) {
            return null;
        }

        try {
            return s3ImageUrlValidator.validateAndNormalize(photoUrl, S3ImageDirectory.JOURNEYS);
        } catch (InvalidImageUrlException e) {
            throw InvalidJourneyBasicInfoException.invalidPhotoUrl();
        }
    }
}
