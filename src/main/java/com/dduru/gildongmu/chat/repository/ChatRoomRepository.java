package com.dduru.gildongmu.chat.repository;

import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.exception.ChatRoomNotFoundException;
import com.dduru.gildongmu.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
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

    default ChatRoom getByIdOrThrow(Long roomId) {
        return findById(roomId).orElseThrow(() -> ChatRoomNotFoundException.of(roomId));
    }

    Optional<ChatRoom> findByIdAndRoomType(Long roomId, ChatRoomType chatRoomType);

    /**
     * LAZY 연관(post.user) 접근 시 추가 쿼리가 발생할 수 있어 fetch join으로 함께 로딩한다.
     */
    @Query("""
            SELECT r
            FROM ChatRoom r
            JOIN FETCH r.post p
            JOIN FETCH p.user u
            WHERE r.id = :roomId
              AND r.roomType = :roomType
            """)
    Optional<ChatRoom> findByIdAndRoomTypeWithPostUser(
            @Param("roomId") Long roomId,
            @Param("roomType") ChatRoomType roomType
    );
}
