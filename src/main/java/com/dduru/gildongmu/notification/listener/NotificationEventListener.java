package com.dduru.gildongmu.notification.listener;

import com.dduru.gildongmu.journey.event.JourneyNoticeCreatedEvent;
import com.dduru.gildongmu.journey.event.ScheduleCanceledEvent;
import com.dduru.gildongmu.journey.event.ScheduleCreatedEvent;
import com.dduru.gildongmu.journey.event.ScheduleUpdatedEvent;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.notification.domain.Notification;
import com.dduru.gildongmu.notification.domain.enums.NotificationType;
import com.dduru.gildongmu.notification.domain.enums.ResourceType;
import com.dduru.gildongmu.notification.repository.NotificationRepository;
import com.dduru.gildongmu.participation.event.MatchAppliedEvent;
import com.dduru.gildongmu.participation.event.MatchApprovedEvent;
import com.dduru.gildongmu.participation.event.MatchRejectedEvent;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;
    private final JourneyMemberRepository journeyMemberRepository;
    private final UserRepository userRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleMatchApplied(MatchAppliedEvent event) {
        try {
            User recipient = userRepository.getReferenceById(event.recipientUserId());
            Notification notification = Notification.create(
                    recipient,
                    NotificationType.MATCH_APPLIED,
                    "[" + event.postTitle() + "]에 새로운 참여 신청이 도착했습니다.",
                    ResourceType.MATCH,
                    event.participationId()
            );
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("MATCH_APPLIED 알림 저장 실패 - participationId={}", event.participationId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleMatchApproved(MatchApprovedEvent event) {
        try {
            User recipient = userRepository.getReferenceById(event.applicantUserId());
            Notification notification = Notification.create(
                    recipient,
                    NotificationType.MATCH_APPROVED,
                    "[" + event.journeyTitle() + "] 여행 참여가 승인되었습니다.",
                    ResourceType.JOURNEY,
                    event.journeyId()
            );
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("MATCH_APPROVED 알림 저장 실패 - participationId={}", event.participationId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleMatchRejected(MatchRejectedEvent event) {
        try {
            User recipient = userRepository.getReferenceById(event.applicantUserId());
            Notification notification = Notification.create(
                    recipient,
                    NotificationType.MATCH_REJECTED,
                    "참여 신청이 검토되었으나 함께하기 어렵게 되었습니다.",
                    ResourceType.MATCH,
                    event.participationId()
            );
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("MATCH_REJECTED 알림 저장 실패 - participationId={}", event.participationId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleJourneyNoticeCreated(JourneyNoticeCreatedEvent event) {
        try {
            // 동일 게시글에 이미 공지 알림이 발송된 경우 재발송하지 않음
            if (notificationRepository.existsByTypeAndResourceTypeAndResourceId(
                    NotificationType.JOURNEY_NOTICE, ResourceType.JOURNEY_POST, event.journeyPostId())) {
                return;
            }

            List<Long> recipientIds = journeyMemberRepository
                    .findActiveUserIdsByJourneyIdExcludingUser(event.journeyId(), event.actorUserId());
            if (recipientIds.isEmpty()) {
                return;
            }

            String body = "[" + event.journeyTitle() + "] 새 공지가 등록되었습니다.";
            List<Notification> notifications = recipientIds.stream()
                    .map(id -> Notification.create(
                            userRepository.getReferenceById(id),
                            NotificationType.JOURNEY_NOTICE,
                            body,
                            ResourceType.JOURNEY_POST,
                            event.journeyPostId()
                    ))
                    .toList();
            notificationRepository.saveAll(notifications);
        } catch (Exception e) {
            log.error("JOURNEY_NOTICE 알림 저장 실패 - journeyPostId={}", event.journeyPostId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleScheduleCreated(ScheduleCreatedEvent event) {
        try {
            saveScheduleNotifications(
                    event.journeyId(), event.actorUserId(),
                    NotificationType.SCHEDULE_CREATED,
                    "[" + event.scheduleTitle() + "] 일정이 추가되었습니다.",
                    event.scheduleId()
            );
        } catch (Exception e) {
            log.error("SCHEDULE_CREATED 알림 저장 실패 - scheduleId={}", event.scheduleId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleScheduleUpdated(ScheduleUpdatedEvent event) {
        try {
            saveScheduleNotifications(
                    event.journeyId(), event.actorUserId(),
                    NotificationType.SCHEDULE_UPDATED,
                    "[" + event.scheduleTitle() + "] 일정이 변경되었습니다.",
                    event.scheduleId()
            );
        } catch (Exception e) {
            log.error("SCHEDULE_UPDATED 알림 저장 실패 - scheduleId={}", event.scheduleId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleScheduleCanceled(ScheduleCanceledEvent event) {
        try {
            saveScheduleNotifications(
                    event.journeyId(), event.actorUserId(),
                    NotificationType.SCHEDULE_CANCELED,
                    "[" + event.scheduleTitle() + "] 일정이 취소되었습니다.",
                    event.scheduleId()
            );
        } catch (Exception e) {
            log.error("SCHEDULE_CANCELED 알림 저장 실패 - scheduleId={}", event.scheduleId(), e);
        }
    }

    private void saveScheduleNotifications(
            Long journeyId,
            Long actorUserId,
            NotificationType type,
            String body,
            Long scheduleId
    ) {
        List<Long> recipientIds = journeyMemberRepository
                .findActiveUserIdsByJourneyIdExcludingUser(journeyId, actorUserId);
        if (recipientIds.isEmpty()) {
            return;
        }

        List<Notification> notifications = recipientIds.stream()
                .map(id -> Notification.create(
                        userRepository.getReferenceById(id),
                        type,
                        body,
                        ResourceType.SCHEDULE,
                        scheduleId
                ))
                .toList();
        notificationRepository.saveAll(notifications);
    }
}
