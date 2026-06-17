package com.dduru.gildongmu.chat.repository;

import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.query.ChatRoomIdByPostIdQueryResult;
import com.dduru.gildongmu.chat.exception.ChatRoomNotFoundException;
import com.dduru.gildongmu.user.domain.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomRepositoryCustom {
    @Query("""
            SELECT DISTINCT r
            FROM ChatRoom r
            JOIN ChatRoomMember m1 ON m1.room = r
            JOIN ChatRoomMember m2 ON m2.room = r
            WHERE r.post.id = :postId
              AND r.roomType = :roomType
              AND r.status = :status
              AND m1.user = :requester
              AND m2.user = :target
            """)
    Optional<ChatRoom> findPrivateRoomByPostAndUsers(
            @Param("postId") Long postId,
            @Param("roomType") ChatRoomType roomType,
            @Param("status") ChatRoomStatus status,
            @Param("requester") User requester,
            @Param("target") User target
    );

    @Query("""
            SELECT DISTINCT r.id
            FROM ChatRoom r
            JOIN ChatRoomMember m1 ON m1.room = r
            JOIN ChatRoomMember m2 ON m2.room = r
            WHERE r.post.id = :postId
              AND r.roomType = :roomType
              AND r.status = :status
              AND (
                (m1.user.id = :userId1 AND m2.user.id = :userId2)
                OR (m1.user.id = :userId2 AND m2.user.id = :userId1)
              )
            """)
    Optional<Long> findPrivateRoomIdByPostAndUserIds(
            @Param("postId") Long postId,
            @Param("roomType") ChatRoomType roomType,
            @Param("status") ChatRoomStatus status,
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2
    );

    Optional<ChatRoom> findByJourneyIdAndRoomType(Long journeyId, ChatRoomType roomType);

    @Query("""
            SELECT r
            FROM ChatRoom r
            LEFT JOIN FETCH r.post p
            LEFT JOIN FETCH r.journey j
            LEFT JOIN FETCH j.post
            WHERE r.id = :roomId
            """)
    Optional<ChatRoom> findByIdWithContext(@Param("roomId") Long roomId);

    @Query("""
            SELECT r
            FROM ChatRoom r
            WHERE r.journey.post.id = :postId
              AND r.roomType = :roomType
            """)
    Optional<ChatRoom> findByJourneyPostIdAndRoomType(
            @Param("postId") Long postId,
            @Param("roomType") ChatRoomType roomType
    );

    @Query("""
            SELECT new com.dduru.gildongmu.chat.dto.query.ChatRoomIdByPostIdQueryResult(r.journey.post.id, r.id)
            FROM ChatRoom r
            WHERE r.journey.post.id IN :postIds
              AND r.roomType = :roomType
            """)
    List<ChatRoomIdByPostIdQueryResult> findRoomIdsByJourneyPostIdsAndRoomType(
            @Param("postIds") Collection<Long> postIds,
            @Param("roomType") ChatRoomType roomType
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r
            FROM ChatRoom r
            WHERE r.journey.id = :journeyId
              AND r.roomType = :roomType
            """)
    Optional<ChatRoom> findByJourneyIdAndRoomTypeWithLock(
            @Param("journeyId") Long journeyId,
            @Param("roomType") ChatRoomType roomType
    );

    default ChatRoom getByIdOrThrow(Long roomId) {
        return findById(roomId).orElseThrow(ChatRoomNotFoundException::new);
    }

    default ChatRoom getByIdWithContextOrThrow(Long roomId) {
        return findByIdWithContext(roomId).orElseThrow(ChatRoomNotFoundException::new);
    }

    default ChatRoom getByJourneyIdAndRoomTypeWithLock(Long journeyId, ChatRoomType roomType) {
        return findByJourneyIdAndRoomTypeWithLock(journeyId, roomType)
                .orElseThrow(ChatRoomNotFoundException::new);
    }
}
