package com.dduru.gildongmu.journey.service;

import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.journey.domain.JourneyMember;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.dto.response.JourneyDetailResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyMainCardResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyMainListResponse;
import com.dduru.gildongmu.journey.exception.JourneyAccessDeniedException;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.journey.repository.JourneyRepository;
import com.dduru.gildongmu.post.dto.response.PostDetailResponse;
import com.dduru.gildongmu.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JourneyQueryService {

    private final JourneyRepository journeyRepository;
    private final JourneyMemberRepository journeyMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final PostService postService;
    private final TimeProvider timeProvider;

    public JourneyMainListResponse retrieveMyJourneys(Long userId) {
        LocalDate today = today();

        List<JourneyMember> activeJourneyMembers = journeyMemberRepository
                .findActiveJourneyMembersByUserIdAndStatus(userId, JourneyMemberStatus.ACTIVE, today);
        List<JourneyMember> completedJourneyMembers = journeyMemberRepository
                .findCompletedJourneyMembersByUserIdAndStatus(userId, JourneyMemberStatus.ACTIVE, today);

        List<JourneyMainCardResponse> activeJourneys = toJourneyCards(activeJourneyMembers, today);
        List<JourneyMainCardResponse> completedJourneys = toJourneyCards(completedJourneyMembers, today);

        return JourneyMainListResponse.of(activeJourneys, completedJourneys);
    }

    public JourneyDetailResponse retrieveMyJourneyDetail(Long journeyId, Long userId) {
        Journey journey = journeyRepository.getByIdWithPostContextOrThrow(journeyId);
        validateJourneyAccess(journeyId, userId);

        // journey는 제목/대표 사진만 직접 가지고, 상단 카드에 필요한 나머지 값은 연결된 post에서 읽는다.
        PostDetailResponse post = postService.getDetail(journey.getPost(), userId);
        Long groupRoomId = chatRoomRepository.findByJourneyIdAndRoomType(journey.getId(), ChatRoomType.GROUP)
                .map(ChatRoom::getId)
                .orElse(null);

        return JourneyDetailResponse.from(journey, groupRoomId, post);
    }

    private List<JourneyMainCardResponse> toJourneyCards(List<JourneyMember> journeyMembers, LocalDate today) {
        // journey_members는 journey/user unique 제약을 가지므로 카드 조립 시 별도 중복제거가 필요 없다.
        return journeyMembers.stream()
                .map(journeyMember -> JourneyMainCardResponse.fromJourneyMember(journeyMember, today))
                .toList();
    }

    private void validateJourneyAccess(Long journeyId, Long userId) {
        boolean accessible = journeyMemberRepository.existsByJourneyIdAndUserIdAndStatus(
                journeyId,
                userId,
                JourneyMemberStatus.ACTIVE
        );
        if (!accessible) {
            throw new JourneyAccessDeniedException();
        }
    }

    private LocalDate today() {
        return timeProvider.today();
    }
}
