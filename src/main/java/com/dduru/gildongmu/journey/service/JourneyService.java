package com.dduru.gildongmu.journey.service;

import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.repository.ChatRoomMemberRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.chat.service.ChatMessageSendService;
import com.dduru.gildongmu.common.validation.InvalidImageUrlException;
import com.dduru.gildongmu.common.validation.S3ImageUrlValidator;
import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.journey.domain.JourneyMember;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.dto.request.JourneyUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyUpdateResponse;
import com.dduru.gildongmu.journey.exception.InvalidJourneyBasicInfoException;
import com.dduru.gildongmu.journey.exception.JourneyAccessDeniedException;
import com.dduru.gildongmu.journey.repository.JourneyRepository;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import com.dduru.gildongmu.participation.repository.ParticipationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.s3.enums.S3ImageDirectory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class JourneyService {
    private static final int TITLE_MIN_LENGTH = 5;
    private static final int TITLE_MAX_LENGTH = 40;

    private final JourneyRepository journeyRepository;
    private final JourneyMemberRepository journeyMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ParticipationRepository participationRepository;
    private final PostRepository postRepository;
    private final ChatMessageSendService chatMessageSendService;
    private final S3ImageUrlValidator s3ImageUrlValidator;

    public JourneyUpdateResponse updateBasicInfo(Long journeyId, Long userId, JourneyUpdateRequest request) {
        Journey journey = getUpdatableJourney(journeyId, userId);

        String title = normalizeTitle(request.title());
        String photoUrl = normalizePhotoUrl(request.photoUrl());
        validateHasAnyPatch(title, photoUrl);

        journey.updateBasicInfo(title, photoUrl);
        log.info("나의 여정 기본 정보 수정됨 - journeyId={}, userId={}", journeyId, userId);
        return JourneyUpdateResponse.from(journey);
    }

    public void removeMember(Long journeyId, Long hostUserId, Long memberUserId) {
        validateHostAuthority(journeyId, hostUserId);

        JourneyMember member = getActiveMemberForUpdate(journeyId, memberUserId);
        validateRemovableMember(member);

        Long postId = member.getJourney().getPost().getId();
        Post post = postRepository.getActiveByIdWithLockOrThrow(postId);
        member.remove();
        // 신청 승인 이력은 유지하고, 현재 멤버십에 맞춰 모집 인원만 줄인다.
        participationRepository.findByPostIdAndUserIdAndStatus(postId, memberUserId, ParticipationStatus.APPROVED)
                .ifPresent(post::excludeApprovedParticipation);
        findGroupChatRoomAndRemoveMember(journeyId, hostUserId, memberUserId);

        log.info("나의 여정 멤버 내보내기 완료 - journeyId={}, hostUserId={}, memberUserId={}",
                journeyId, hostUserId, memberUserId);
    }

    private Journey getUpdatableJourney(Long journeyId, Long userId) {
        return journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE)
                .orElseThrow(JourneyAccessDeniedException::new);
    }

    private void validateHostAuthority(Long journeyId, Long hostUserId) {
        boolean isActiveHost = journeyMemberRepository.existsActiveHost(journeyId, hostUserId);
        if (!isActiveHost) {
            throw new JourneyAccessDeniedException();
        }
    }

    private JourneyMember getActiveMemberForUpdate(Long journeyId, Long memberUserId) {
        return journeyMemberRepository.findActiveMemberForUpdate(journeyId, memberUserId)
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
                    chatMessageSendService.publishUserKicked(chatRoom, memberUserId, hostUserId);
                });
    }

    private void validateHasAnyPatch(String title, String photoUrl) {
        if (title == null && photoUrl == null) {
            throw InvalidJourneyBasicInfoException.emptyPatch();
        }
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
