package com.dduru.gildongmu.chat.domain;

import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.exception.InvalidChatRoomContextException;
import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.journey.domain.Journey;
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
    @JoinColumn(name = "post_id")
    private Post post;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id")
    private Journey journey;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private ChatRoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ChatRoomStatus status;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @Builder(access = AccessLevel.PRIVATE)
    private ChatRoom(Post post, Journey journey, ChatRoomType roomType, Integer maxCapacity, ChatRoomStatus status) {
        validateRoomContext(post, journey, roomType);
        this.post = post;
        this.journey = journey;
        this.roomType = roomType;
        this.maxCapacity = maxCapacity;
        this.status = status != null ? status : ChatRoomStatus.PENDING;
    }

    public static ChatRoom forPrivateChat(Post post) {
        return ChatRoom.builder()
                .post(post)
                .roomType(ChatRoomType.PRIVATE)
                .maxCapacity(2)
                .status(ChatRoomStatus.ACTIVE)
                .build();
    }

    public static ChatRoom createPendingGroupChat(Journey journey) {
        validateRoomContext(null, journey, ChatRoomType.GROUP);
        int capacity = journey.getPost().getRecruitCapacity();
        return ChatRoom.builder()
                .journey(journey)
                .roomType(ChatRoomType.GROUP)
                .maxCapacity(capacity)
                .status(ChatRoomStatus.PENDING)
                .build();
    }

    public boolean canAccommodate(int participantCount) {
        return participantCount <= maxCapacity;
    }

    public void activateIfPending() {
        if (this.status == ChatRoomStatus.PENDING) {
            this.status = ChatRoomStatus.ACTIVE;
        }
    }

    public Post getContextPost() {
        if (roomType == ChatRoomType.PRIVATE) {
            return post;
        }
        return journey.getPost();
    }

    private static void validateRoomContext(Post post, Journey journey, ChatRoomType roomType) {
        if (roomType == null) {
            throw InvalidChatRoomContextException.missingRoomType();
        }

        InvalidChatRoomContextException exception = switch (roomType) {
            case PRIVATE -> post == null || journey != null
                    ? InvalidChatRoomContextException.invalidPrivateContext()
                    : null;
            case GROUP -> post != null || journey == null || journey.getPost() == null
                    ? InvalidChatRoomContextException.invalidGroupContext()
                    : null;
        };
        if (exception != null) {
            throw exception;
        }
    }
}
