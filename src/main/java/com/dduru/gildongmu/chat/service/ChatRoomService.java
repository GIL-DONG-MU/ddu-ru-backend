package com.dduru.gildongmu.chat.service;

import com.dduru.gildongmu.auth.exception.UserNotFoundException;
import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.ChatRoomMember;
import com.dduru.gildongmu.chat.domain.enums.ChatMemberRole;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.request.GroupChatInviteRequest;
import com.dduru.gildongmu.chat.dto.response.GroupChatInviteResponse;
import com.dduru.gildongmu.chat.dto.response.PrivateChatRoomCreateResponse;
import com.dduru.gildongmu.chat.exception.ChatRoomCapacityExceededException;
import com.dduru.gildongmu.chat.exception.GroupChatRoomInviteAccessDeniedException;
import com.dduru.gildongmu.chat.exception.NotSelfChatException;
import com.dduru.gildongmu.chat.repository.ChatRoomMemberRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public PrivateChatRoomCreateResponse createOrGetPrivateRoom(Long requesterId, Long postId) {
        Post post = postRepository.getActiveByIdOrThrow(postId);
        User target = post.getUser();
        User requester = userRepository.getByIdOrThrow(requesterId);
        validateNotSelfPrivateChat(requesterId, target.getId());

        Optional<ChatRoom> existingRoom = findExistingPrivateRoom(post, requester, target);
        if (existingRoom.isPresent()) {
            return new PrivateChatRoomCreateResponse(existingRoom.get().getId(), false);
        }
        Long newRoomId = createPrivateRoom(post, requester, target);
        return new PrivateChatRoomCreateResponse(newRoomId, true);
    }

    @Transactional
    public GroupChatInviteResponse inviteMembersToGroupRoom(Long requesterId, Long roomId, GroupChatInviteRequest request) {
        ChatRoom chatRoom = getActiveGroupRoomOrThrow(roomId);
        validateHostAuthority(chatRoom.getPost().getUser().getId(), requesterId);

        List<Long> sanitizedIds = distinctNonHostUserIds(request.inviteeUserIds(), requesterId);
        if (sanitizedIds.isEmpty()) {
            return new GroupChatInviteResponse(chatRoom.getId(), 0);
        }

        List<User> newGuests = resolveInviteTargets(chatRoom.getId(), sanitizedIds);
        if (newGuests.isEmpty()) {
            return new GroupChatInviteResponse(chatRoom.getId(), 0);
        }

        validateGroupRoomCapacity(chatRoom, newGuests.size());
        saveNewGuestMembers(chatRoom, newGuests);

        return new GroupChatInviteResponse(chatRoom.getId(), newGuests.size());
    }

    /**
     * 게시글 저장 직후 호출. 글당 그룹 단톡 1개를 PENDING으로 만들고 작성자를 HOST로 둔다.
     */
    @Transactional
    public void createPendingGroupRoomForPost(Post post, User author) {
        ChatRoom room = chatRoomRepository.save(ChatRoom.createPendingGroupChat(post));
        chatRoomMemberRepository.save(ChatRoomMember.create(room, author, ChatMemberRole.HOST));
        log.info("그룹 채팅방 생성 - roomId={}, postId={}, hostId={}", room.getId(), post.getId(), author.getId());
    }

    /**
     * 해당 방에 첫 메시지가 저장된 직후 호출하면 PENDING → ACTIVE 로 전환한다.
     */
    @Transactional
    public void activateGroupChatOnFirstMessage(Long roomId) {
        ChatRoom room = chatRoomRepository.getByIdOrThrow(roomId);
        if (room.getRoomType() != ChatRoomType.GROUP) {
            return;
        }
        room.activateIfPending();
    }

    private ChatRoom getActiveGroupRoomOrThrow(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndRoomTypeWithPostUser(roomId, ChatRoomType.GROUP)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        validateGroupRoomIsActive(chatRoom);
        return chatRoom;
    }

    private static void validateGroupRoomIsActive(ChatRoom room) {
        if (room.getStatus() == ChatRoomStatus.CLOSED || room.getStatus() == ChatRoomStatus.DELETED) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_CLOSED);
        }
    }

    private static void validateHostAuthority(Long postUserId, Long requesterId) {
        if (!postUserId.equals(requesterId)) {
            throw new GroupChatRoomInviteAccessDeniedException();
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

    private Long createPrivateRoom(Post post, User requester, User target) {
        ChatRoom room = ChatRoom.forPrivateChat(post);
        chatRoomRepository.save(room);
        savePrivateRoomMembers(room, requester, target);
        log.info("1:1 채팅방 생성 - roomId={}, postId={}, users=[{}, {}]", room.getId(), post.getId(), requester.getId(), target.getId());
        return room.getId();
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

    private static List<Long> distinctNonHostUserIds(List<Long> targetIds, Long requesterId) {
        return targetIds.stream()
                .filter(id -> id != null && !id.equals(requesterId))
                .distinct()
                .toList();
    }

    private List<User> resolveInviteTargets(Long roomId, List<Long> requestedUserIds) {
        List<User> users = userRepository.findAllById(requestedUserIds);

        if (users.size() != requestedUserIds.size()) {
            Set<Long> foundIds = users.stream()
                    .map(User::getId)
                    .collect(Collectors.toSet());
            validateAllUsersExist(requestedUserIds, foundIds);
        }

        Set<Long> existingMemberIds = new HashSet<>(
                chatRoomMemberRepository.findExistingUserIdsByRoomIdAndUserIdIn(roomId, requestedUserIds)
        );

        return users.stream()
                .filter(user -> !existingMemberIds.contains(user.getId()))
                .toList();
    }

    private void validateAllUsersExist(List<Long> requestedIds, Set<Long> foundIds) {
        Long missingId = requestedIds.stream()
                .filter(id -> !foundIds.contains(id))
                .findFirst()
                .orElse(null);

        if (missingId != null) {
            throw UserNotFoundException.of(missingId);
        }
    }

    private void validateGroupRoomCapacity(ChatRoom room, int newGuestCount) {
        int currentMemberCount = chatRoomMemberRepository.countByRoom(room);
        int totalAfterInvite = currentMemberCount + newGuestCount;

        if (!room.canAccommodate(totalAfterInvite)) {
            throw new ChatRoomCapacityExceededException();
        }
    }

    private void saveNewGuestMembers(ChatRoom room, List<User> newGuestUsers) {
        List<ChatRoomMember> created = newGuestUsers.stream()
                .map(user -> ChatRoomMember.create(room, user, ChatMemberRole.GUEST))
                .toList();
        chatRoomMemberRepository.saveAll(created);
    }
}
