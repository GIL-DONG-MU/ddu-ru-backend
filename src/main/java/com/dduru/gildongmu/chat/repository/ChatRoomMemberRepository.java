package com.dduru.gildongmu.chat.repository;

import com.dduru.gildongmu.chat.domain.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    int countByRoom_Id(Long roomId);

    Optional<ChatRoomMember> findByRoom_IdAndUser_Id(Long roomId, Long userId);
}
