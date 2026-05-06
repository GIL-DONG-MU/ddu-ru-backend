package com.dduru.gildongmu.journey.service;

import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.journey.domain.JourneyMember;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.dto.response.JourneyMainCardResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyMainListResponse;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JourneyQueryService {

    private final JourneyMemberRepository journeyMemberRepository;
    private final TimeProvider timeProvider;

    public JourneyMainListResponse retrieveMyJourneys(Long userId) {
        LocalDate today = timeProvider.today();

        List<JourneyMember> activeJourneyMembers = journeyMemberRepository
                .findActiveJourneyMembersByUserIdAndStatus(userId, JourneyMemberStatus.ACTIVE, today);
        List<JourneyMember> completedJourneyMembers = journeyMemberRepository
                .findCompletedJourneyMembersByUserIdAndStatus(userId, JourneyMemberStatus.ACTIVE, today);

        Comparator<JourneyMainCardResponse> activeSortOrder = Comparator
                .comparing(JourneyMainCardResponse::startDate)
                .thenComparing(JourneyMainCardResponse::postId, Comparator.reverseOrder());
        Comparator<JourneyMainCardResponse> completedSortOrder = Comparator
                .comparing(JourneyMainCardResponse::endDate, Comparator.reverseOrder())
                .thenComparing(JourneyMainCardResponse::postId, Comparator.reverseOrder());

        List<JourneyMainCardResponse> activeJourneys = toJourneyCards(activeJourneyMembers, activeSortOrder, today);
        List<JourneyMainCardResponse> completedJourneys = toJourneyCards(completedJourneyMembers, completedSortOrder, today);

        return JourneyMainListResponse.of(activeJourneys, completedJourneys);
    }

    private List<JourneyMainCardResponse> toJourneyCards(
            List<JourneyMember> journeyMembers,
            Comparator<JourneyMainCardResponse> sortOrder,
            LocalDate today
    ) {
        // journey_members는 post/user unique 제약을 가지므로 카드 조립 시 별도 중복제거가 필요 없다.
        return journeyMembers.stream()
                .map(journeyMember -> JourneyMainCardResponse.fromJourneyMember(journeyMember, today))
                .sorted(sortOrder)
                .toList();
    }
}
