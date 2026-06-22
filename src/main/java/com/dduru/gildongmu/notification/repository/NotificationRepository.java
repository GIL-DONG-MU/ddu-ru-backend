package com.dduru.gildongmu.notification.repository;

import com.dduru.gildongmu.notification.domain.Notification;
import com.dduru.gildongmu.notification.exception.NotificationNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
            SELECT n FROM Notification n
            WHERE n.recipient.id = :userId
              AND (:cursor IS NULL OR n.id < :cursor)
            ORDER BY n.id DESC
            """)
    List<Notification> findByRecipientIdWithCursor(
            @Param("userId") Long userId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    long countByRecipient_IdAndReadFalse(Long recipientId);

    Optional<Notification> findByIdAndRecipient_Id(Long id, Long recipientId);

    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.read = true, n.readAt = :now
            WHERE n.recipient.id = :userId AND n.read = false
            """)
    int markAllAsReadByRecipientId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    default Notification getByIdAndRecipientIdOrThrow(Long id, Long recipientId) {
        return findByIdAndRecipient_Id(id, recipientId)
                .orElseThrow(NotificationNotFoundException::new);
    }
}
