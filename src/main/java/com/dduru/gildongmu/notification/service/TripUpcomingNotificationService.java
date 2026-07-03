package com.dduru.gildongmu.notification.service;

import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.fcm.service.FcmPushService;
import com.dduru.gildongmu.journey.dto.query.UpcomingTripMemberQueryResult;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.notification.domain.Notification;
import com.dduru.gildongmu.notification.domain.enums.NotificationType;
import com.dduru.gildongmu.notification.domain.enums.ResourceType;
import com.dduru.gildongmu.notification.repository.NotificationRepository;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripUpcomingNotificationService {

    private static final int UPCOMING_DAYS_BEFORE = 3;
    private static final String PUSH_TITLE = "일정 임박";
    private static final String PUSH_BODY = "곧 여행이 시작됩니다. 준비물은 다 챙기셨나요?";

    private final JourneyMemberRepository journeyMemberRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationPersistService notificationPersistService;
    private final UserRepository userRepository;
    private final FcmPushService fcmPushService;
    private final TimeProvider timeProvider;

    @Transactional(readOnly = true)
    public void notifyUpcomingTrips() {
        LocalDate today = timeProvider.today();
        LocalDate targetDate = today.plusDays(UPCOMING_DAYS_BEFORE);
        LocalDateTime startOfToday = today.atStartOfDay();

        List<UpcomingTripMemberQueryResult> upcomingTripMembers =
                journeyMemberRepository.findUpcomingTripMembers(targetDate);

        if (upcomingTripMembers.isEmpty()) return;

        upcomingTripMembers.stream()
                .collect(Collectors.groupingBy(UpcomingTripMemberQueryResult::journeyId))
                .forEach((journeyId, members) -> notifyMembers(journeyId, members, startOfToday));
    }

    private void notifyMembers(Long journeyId, List<UpcomingTripMemberQueryResult> members, LocalDateTime startOfToday) {
        List<Long> allUserIds = members.stream()
                .map(UpcomingTripMemberQueryResult::userId)
                .toList();

        List<Long> pendingUserIds = findPendingUserIds(allUserIds, journeyId, startOfToday);

        if (pendingUserIds.isEmpty()) {
            log.info("TRIP_UPCOMING 전원 발송 완료 상태 - journeyId={}", journeyId);
            return;
        }

        List<Notification> notifications = pendingUserIds.stream()
                .map(userId -> Notification.create(
                        userRepository.getReferenceById(userId), // 실제 User를 로딩하지 않고 FK용 프록시만 생성
                        NotificationType.TRIP_UPCOMING,
                        PUSH_BODY,
                        ResourceType.JOURNEY,
                        journeyId
                ))
                .toList();

        notificationPersistService.saveAll(notifications);

        List<Long> fcmTargetIds = userRepository.findEnabledUserIds(pendingUserIds);
        if (fcmTargetIds.isEmpty()) return;
        fcmPushService.sendToUsers(fcmTargetIds, PUSH_TITLE, PUSH_BODY, null,
                FcmPushService.dataPayload(ResourceType.JOURNEY, journeyId));
    }

    private List<Long> findPendingUserIds(List<Long> allUserIds, Long journeyId, LocalDateTime startOfToday) {
        // contains() 조회를 O(1)로 처리하기 위해 Set으로 변환
        Set<Long> alreadyNotified = new HashSet<>(
                notificationRepository.findNotifiedRecipientIds(
                        NotificationType.TRIP_UPCOMING, ResourceType.JOURNEY, journeyId, startOfToday));
        return allUserIds.stream()
                .filter(id -> !alreadyNotified.contains(id))
                .toList();
    }
}
