package com.dduru.gildongmu.chat.repository;

import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    int countByRoom(ChatRoom room);

    @Query("SELECT m.user.id FROM ChatRoomMember m WHERE m.room.id = :roomId AND m.user.id IN :userIds")
    List<Long> findExistingUserIdsByRoomIdAndUserIdIn(
            @Param("roomId") Long roomId,
            @Param("userIds") Collection<Long> userIds);
}
