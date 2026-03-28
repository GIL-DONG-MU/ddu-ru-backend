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
import com.dduru.gildongmu.chat.exception.UnauthorizedChatRoomCreationException;
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
        validateGroupChatRoomHost(post.getUser().getId(), hostId);

        List<Long> guestUserIds = distinctNonHostUserIds(request.memberUserIds(), hostId);

        boolean isNew = false;

        ChatRoom chatRoom = chatRoomRepository.findByPostIdAndRoomType(post.getId(), ChatRoomType.GROUP)
                .orElse(null);

        if (chatRoom == null) {
            chatRoom = ChatRoom.forGroupChat(post);
            chatRoomRepository.save(chatRoom);
            isNew = true;
        }

        validateGroupRoomCapacity(chatRoom, guestUserIds.size() + 1);

        chatRoomMemberRepository.save(ChatRoomMember.create(chatRoom, host, ChatMemberRole.HOST));
        saveAllGuestMembers(chatRoom, guestUserIds);

        return new ChatRoomCreateResponse(chatRoom.getId(), isNew);
    }

    private void validateGroupChatRoomHost(Long postUserId, Long hostId) {
        if (!postUserId.equals(hostId)) {
            throw new UnauthorizedChatRoomCreationException();
        }
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

    /**
     * 게시글 저장 직후 호출. 글당 그룹 단톡 1개를 PENDING으로 만들고 작성자를 HOST로 둔다.
     */
    public void createPendingGroupRoomForPost(Post post, User author) {
        ChatRoom room = chatRoomRepository.save(ChatRoom.createPendingGroupChat(post));
        chatRoomMemberRepository.save(ChatRoomMember.create(room, author, ChatMemberRole.HOST));
        log.info("그룹 채팅방 생성 - roomId={}, postId={}, hostId={}", room.getId(), post.getId(), author.getId());
    }

    /**
     * 해당 방에 첫 메시지가 저장된 직후 호출하면 PENDING → ACTIVE 로 전환한다.
     */
    public void activateGroupChatOnFirstMessage(Long roomId) {
        ChatRoom room = chatRoomRepository.getByIdOrThrow(roomId);
        if (room.getRoomType() != ChatRoomType.GROUP) {
            return;
        }
        room.activateIfPending();
    }
}
