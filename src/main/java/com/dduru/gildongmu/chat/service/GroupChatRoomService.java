package com.dduru.gildongmu.chat.service;

import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.ChatRoomMember;
import com.dduru.gildongmu.chat.domain.enums.ChatMemberRole;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.request.GroupChatInviteRequest;
import com.dduru.gildongmu.chat.dto.response.GroupChatInviteResponse;
import com.dduru.gildongmu.chat.dto.response.InviteTargetsResult;
import com.dduru.gildongmu.chat.exception.ChatRoomCapacityExceededException;
import com.dduru.gildongmu.chat.exception.ChatRoomClosedException;
import com.dduru.gildongmu.chat.exception.ChatRoomNotFoundException;
import com.dduru.gildongmu.chat.exception.GroupChatRoomInviteAccessDeniedException;
import com.dduru.gildongmu.chat.repository.ChatRoomMemberRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public GroupChatInviteResponse inviteMembers(Long requesterId, Long roomId, GroupChatInviteRequest request) {
        ChatRoom chatRoom = getActiveRoomOrThrow(roomId);
        validateHostAuthority(chatRoom.getPost().getUser().getId(), requesterId);

        List<Long> inviteeUserIds = distinctNonHostUserIds(request.inviteeUserIds(), requesterId);
        if (inviteeUserIds.isEmpty()) {
            return GroupChatInviteResponse.empty(chatRoom.getId());
        }

        InviteTargetsResult resolution = resolveInviteTargets(chatRoom.getId(), inviteeUserIds);

        List<User> newGuests = resolution.newGuests();
        if (newGuests.isEmpty()) {
            return GroupChatInviteResponse.empty(chatRoom.getId(), resolution);
        }

        validateRoomCapacity(chatRoom, newGuests.size());
        saveNewGuestMembers(chatRoom, newGuests);

        return GroupChatInviteResponse.success(chatRoom.getId(), newGuests.size(), resolution);
    }

    /**
     * 게시글 저장 직후 호출. 글당 그룹 단톡 1개를 PENDING으로 만들고 작성자를 HOST로 둔다.
     */
    public void createPendingRoomForPost(Post post, User author) {
        ChatRoom room = chatRoomRepository.save(ChatRoom.createPendingGroupChat(post));
        chatRoomMemberRepository.save(ChatRoomMember.create(room, author, ChatMemberRole.HOST));
        log.info("그룹 채팅방 생성 - roomId={}, postId={}, hostId={}", room.getId(), post.getId(), author.getId());
    }

    /**
     * 해당 방에 첫 메시지가 저장된 직후 호출하면 PENDING → ACTIVE 로 전환한다.
     */
    public void activateChatOnFirstMessage(Long roomId) {
        ChatRoom room = chatRoomRepository.getByIdOrThrow(roomId);
        if (room.getRoomType() != ChatRoomType.GROUP) {
            return;
        }
        room.activateIfPending();
    }

    private ChatRoom getActiveRoomOrThrow(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndRoomTypeWithPostUser(roomId, ChatRoomType.GROUP)
                .orElseThrow(ChatRoomNotFoundException::new);
        validateRoomIsActive(chatRoom);
        return chatRoom;
    }

    private static void validateRoomIsActive(ChatRoom room) {
        if (room.getStatus() == ChatRoomStatus.CLOSED || room.getStatus() == ChatRoomStatus.DELETED) {
            throw new ChatRoomClosedException();
        }
    }

    private static void validateHostAuthority(Long postUserId, Long requesterId) {
        if (!postUserId.equals(requesterId)) {
            throw new GroupChatRoomInviteAccessDeniedException();
        }
    }

    private static List<Long> distinctNonHostUserIds(List<Long> inviteeUserIds, Long hostUserId) {
        return inviteeUserIds.stream()
                .filter(id -> id != null && !id.equals(hostUserId)) // DTO 제약이 있지만 방어적으로 null도 제외
                .distinct()
                .toList();
    }

    private InviteTargetsResult resolveInviteTargets(Long roomId, List<Long> inviteeUserIds) {
        List<User> users = userRepository.findAllById(inviteeUserIds);
        Set<Long> userIds = extractUserIds(users);

        List<Long> missingUserIds = findMissingIds(inviteeUserIds, userIds);
        logMissingIdsIfAny(roomId, missingUserIds);

        if (userIds.isEmpty()) {
            return new InviteTargetsResult(List.of(), missingUserIds, List.of()); // all are missing
        }

        Set<Long> existingMemberUserIds = fetchExistingMemberIds(roomId, userIds);
        List<Long> alreadyMemberUserIds = findAlreadyMemberIds(inviteeUserIds, existingMemberUserIds);
        List<User> newGuests = findNewGuests(users, existingMemberUserIds);

        return new InviteTargetsResult(newGuests, missingUserIds, alreadyMemberUserIds);
    }


    private static Set<Long> extractUserIds(List<User> users) {
        return users.stream()
                .map(User::getId)
                .collect(Collectors.toSet());
    }

    private static List<Long> findMissingIds(List<Long> inviteeUserIds, Set<Long> userIds) {
        return inviteeUserIds.stream()
                .filter(id -> !userIds.contains(id))
                .toList();
    }

    private static void logMissingIdsIfAny(Long roomId, List<Long> missingIds) {
        if (missingIds.isEmpty()) {
            return;
        }
        log.warn("그룹 초대에서 존재하지 않는 사용자 id가 포함됨. roomId={}, missingIds={}",
                roomId, missingIds);
    }

    private Set<Long> fetchExistingMemberIds(Long roomId, Set<Long> userIds) {
        return new HashSet<>(
                chatRoomMemberRepository.findExistingUserIdsByRoomIdAndUserIdIn(roomId, userIds)
        );
    }

    private static List<Long> findAlreadyMemberIds(List<Long> inviteeUserIds, Set<Long> existingMemberIds) {
        return inviteeUserIds.stream()
                .filter(existingMemberIds::contains)
                .toList();
    }

    private static List<User> findNewGuests(List<User> users, Set<Long> existingMemberIds) {
        return users.stream()
                .filter(user -> !existingMemberIds.contains(user.getId()))
                .toList();
    }

    private void validateRoomCapacity(ChatRoom room, int newGuestCount) {
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
