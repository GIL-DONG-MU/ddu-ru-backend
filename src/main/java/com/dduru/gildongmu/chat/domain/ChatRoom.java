package com.dduru.gildongmu.chat.domain;

import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.post.domain.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private ChatRoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ChatRoomStatus status;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @Builder
    public ChatRoom(Post post, ChatRoomType roomType, Integer maxCapacity) {
        this.post = post;
        this.roomType = roomType;
        this.maxCapacity = maxCapacity;
        this.status = status != null ? status : ChatRoomStatus.ACTIVE;
    }

    public static ChatRoom forPrivateChat(Post post) {
        return newRoom(post, ChatRoomType.PRIVATE, 2);
    }

    public static ChatRoom createPendingGroupChat(Post post) {
        int capacity = post.getRecruitCapacity() + 1;
        return newRoom(post, ChatRoomType.GROUP, capacity, ChatRoomStatus.PENDING);
    }

    public boolean canAccommodate(int participantCount) {
        return participantCount <= maxCapacity;
    }

    public void activateIfPending() {
        if (this.status == ChatRoomStatus.PENDING) {
            this.status = ChatRoomStatus.ACTIVE;
        }
    }

    private static ChatRoom newRoom(Post post, ChatRoomType roomType, int maxCapacity, ChatRoomStatus status) {
        return ChatRoom.builder()
                .post(post)
                .roomType(roomType)
                .maxCapacity(maxCapacity)
                .status(status)
                .build();
    }
}
