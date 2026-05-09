package com.dduru.gildongmu.chat.repository;

import com.dduru.gildongmu.chat.domain.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("""
            SELECT m
            FROM ChatMessage m
            LEFT JOIN FETCH m.sender s
            LEFT JOIN FETCH s.profile
            WHERE m.room.id = :roomId
              AND m.createdAt >= :visibleFrom
              AND (:beforeMessageId IS NULL OR m.id < :beforeMessageId)
            ORDER BY m.id DESC
            """)
    List<ChatMessage> findVisibleMessages(
            @Param("roomId") Long roomId,
            @Param("beforeMessageId") Long beforeMessageId,
            @Param("visibleFrom") LocalDateTime visibleFrom,
            Pageable pageable
    );

    boolean existsByIdAndRoom_Id(Long messageId, Long roomId);
}
