package com.dduru.gildongmu.chat.service;

import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.ChatRoomMember;
import com.dduru.gildongmu.chat.domain.enums.ChatMemberRole;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.request.GroupChatRoomCreateRequest;
import com.dduru.gildongmu.chat.dto.request.PrivateChatRoomCreateRequest;
import com.dduru.gildongmu.chat.dto.response.ChatRoomCreateResponse;
import com.dduru.gildongmu.chat.exception.ChatRoomCapacityExceededException;
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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public ChatRoomCreateResponse createOrGetPrivateRoom(Long requesterId, PrivateChatRoomCreateRequest request) {
        Post post = postRepository.getActiveByIdOrThrow(request.postId());
        User target = post.getUser();
        User requester = userRepository.getByIdOrThrow(requesterId);
        validateNotSelfPrivateChat(requesterId, target.getId());

        return findExistingPrivateRoom(post, requester, target)
                .map(room -> new ChatRoomCreateResponse(room.getId(), false))
                .orElseGet(() -> createPrivateRoom(post, requester, target));
    }

    public ChatRoomCreateResponse createGroupRoom(Long hostId, GroupChatRoomCreateRequest request) {
        Post post = postRepository.getActiveByIdOrThrow(request.postId());
        User host = userRepository.getByIdOrThrow(hostId);
        List<Long> guestUserIds = distinctNonHostUserIds(request.memberUserIds(), hostId);

        ChatRoom room = ChatRoom.forGroupChat(post);
        validateGroupRoomCapacity(room, guestUserIds.size() + 1);

        chatRoomRepository.save(room);
        chatRoomMemberRepository.save(ChatRoomMember.create(room, host, ChatMemberRole.HOST));
        saveAllGuestMembers(room, guestUserIds);

        return new ChatRoomCreateResponse(room.getId(), true);
    }

    private Optional<ChatRoom> findExistingPrivateRoom(Post post, User requester, User target) {
        return chatRoomRepository.findPrivateRoomByPostAndUsers(
                post.getId(),
                ChatRoomType.PRIVATE,
                ChatRoomStatus.ACTIVE,
                requester,
                target
        );
    }

    private ChatRoomCreateResponse createPrivateRoom(Post post, User requester, User target) {
        ChatRoom room = ChatRoom.forPrivateChat(post);
        chatRoomRepository.save(room);
        savePrivateRoomMembers(room, requester, target);
        log.info("1:1 채팅방 생성 - roomId={}, postId={}, users=[{}, {}]", room.getId(), post.getId(), requester.getId(), target.getId());
        return new ChatRoomCreateResponse(room.getId(), true);
    }

    private void savePrivateRoomMembers(ChatRoom room, User requester, User target) {
        chatRoomMemberRepository.saveAll(List.of(
                ChatRoomMember.create(room, requester, ChatMemberRole.HOST),
                ChatRoomMember.create(room, target, ChatMemberRole.GUEST)
        ));
    }

    private static void validateNotSelfPrivateChat(Long requesterId, Long targetUserId) {
        if (requesterId.equals(targetUserId)) {
            throw new NotSelfChatException();
        }
    }

    private static List<Long> distinctNonHostUserIds(List<Long> userIds, Long hostUserId) {
        Set<Long> orderedUnique = new LinkedHashSet<>();
        for (Long id : userIds) {
            if (id != null && !id.equals(hostUserId)) {
                orderedUnique.add(id);
            }
        }
        return new ArrayList<>(orderedUnique);
    }

    private static void validateGroupRoomCapacity(ChatRoom room, int totalParticipants) {
        if (!room.canAccommodate(totalParticipants)) {
            throw new ChatRoomCapacityExceededException();
        }
    }

    private void saveAllGuestMembers(ChatRoom room, List<Long> guestUserIds) {
        List<ChatRoomMember> members = guestUserIds.stream()
                .map(userId -> {
                    User user = userRepository.getByIdOrThrow(userId);
                    return ChatRoomMember.create(room, user, ChatMemberRole.GUEST);
                })
                .toList();
        chatRoomMemberRepository.saveAll(members);
    }
}
