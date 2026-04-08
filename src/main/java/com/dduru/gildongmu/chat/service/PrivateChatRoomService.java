package com.dduru.gildongmu.chat.service;

import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.ChatRoomMember;
import com.dduru.gildongmu.chat.domain.enums.ChatMemberRole;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.response.PrivateChatRoomCreateResponse;
import com.dduru.gildongmu.chat.exception.NotSelfChatException;
import com.dduru.gildongmu.chat.repository.ChatRoomMemberRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PrivateChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PrivateChatRoomCreateResponse createOrGetRoom(Long requesterId, Long postId) {
        Post post = postRepository.getActiveByIdOrThrow(postId);
        return createOrGetRoom(requesterId, post, post.getUser().getId());
    }

    public PrivateChatRoomCreateResponse createOrGetRoom(Long requesterId, Post post, Long targetUserId) {
        validateNotSelfChat(requesterId, targetUserId);

        User requester = userRepository.getByIdOrThrow(requesterId);
        User target = userRepository.getByIdOrThrow(targetUserId);

        return findExistingRoom(post, requester, target)
                .map(room -> new PrivateChatRoomCreateResponse(room.getId(), false))
                .orElseGet(() -> new PrivateChatRoomCreateResponse(createRoom(post, requester, target), true));
    }

    private Optional<ChatRoom> findExistingRoom(Post post, User requester, User target) {
        return chatRoomRepository.findPrivateRoomByPostAndUsers(
                post.getId(),
                ChatRoomType.PRIVATE,
                ChatRoomStatus.ACTIVE,
                requester,
                target
        );
    }

    private Long createRoom(Post post, User requester, User target) {
        ChatRoom room = ChatRoom.forPrivateChat(post);
        chatRoomRepository.save(room);
        saveRoomMembers(room, requester, target);
        log.info("1:1 채팅방 생성 - roomId={}, postId={}, users=[{}, {}]", room.getId(), post.getId(),
                requester.getId(), target.getId());
        return room.getId();
    }

    private void saveRoomMembers(ChatRoom room, User requester, User target) {
        chatRoomMemberRepository.saveAll(List.of(
                ChatRoomMember.create(room, requester, ChatMemberRole.HOST),
                ChatRoomMember.create(room, target, ChatMemberRole.GUEST)
        ));
    }

    static void validateNotSelfChat(Long requesterId, Long targetUserId) {
        if (requesterId.equals(targetUserId)) {
            throw new NotSelfChatException();
        }
    }
}
