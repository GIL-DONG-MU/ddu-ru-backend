package com.dduru.gildongmu.chat.repository;

import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    int countByRoom(ChatRoom room);

    @Query("""
            SELECT COUNT(m) > 0
            FROM ChatRoomMember m
            WHERE m.room.id = :roomId
              AND m.user.id = :participantUserId
              AND m.room.status = 'ACTIVE'
            """)
    boolean existsByChatRoom_IdAndUser_Id(@Param("roomId") Long roomId, @Param("participantUserId") Long participantUserId);
}
