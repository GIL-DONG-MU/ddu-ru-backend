package com.dduru.gildongmu.chat.repository;

import com.dduru.gildongmu.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    long countByRoom_Id(Long roomId);
}
