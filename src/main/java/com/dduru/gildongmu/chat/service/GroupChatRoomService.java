package com.dduru.gildongmu.chat.service;

import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.ChatRoomMember;
import com.dduru.gildongmu.chat.domain.enums.ChatMemberRole;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.request.GroupChatInviteRequest;
import com.dduru.gildongmu.chat.dto.response.GroupChatInviteMemberResponse;
import com.dduru.gildongmu.chat.exception.ChatRoomCapacityExceededException;
import com.dduru.gildongmu.chat.exception.ChatRoomClosedException;
import com.dduru.gildongmu.chat.exception.GroupChatRoomInviteAccessDeniedException;
import com.dduru.gildongmu.chat.exception.NotSelfChatException;
import com.dduru.gildongmu.chat.repository.ChatRoomMemberRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 그룹 채팅방 초대.
 * <p>
 * {@code ChatRoom} 행을 {@code FOR UPDATE}로 잠근 뒤, 같은 트랜잭션에서 방 상태({@code CLOSED}/{@code DELETED} 제외),
 * 호스트(게시글 작성자) 권한, 정원을 검증한다. 방 행에 대한 갱신은 이 락과 직렬화되므로, 검증 시점의 스냅샷이
 * 저장 직전까지 크게 어긋나지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GroupChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;

    public GroupChatInviteMemberResponse inviteMemberOrGetRoom(Long userId, Long roomId, GroupChatInviteRequest request) {
        ChatRoom chatRoom = chatRoomRepository.getByIdAndRoomTypeWithLockOrThrow(roomId, ChatRoomType.GROUP);
        return inviteMemberOrGetRoom(userId, chatRoom, request.inviteeUserId());
    }

    public GroupChatInviteMemberResponse inviteMemberOrGetRoom(Long userId, Long postId, Long inviteeUserId) {
        ChatRoom chatRoom = chatRoomRepository.getByPostIdAndRoomTypeWithLockOrThrow(postId, ChatRoomType.GROUP);
        return inviteMemberOrGetRoom(userId, chatRoom, inviteeUserId);
    }

    /**
     * 그룹 멤버 추가는 항상 {@code ChatRoom} 락을 잡은 뒤 이 메서드로만 진입한다.
     */
    private GroupChatInviteMemberResponse inviteMemberOrGetRoom(Long userId, ChatRoom chatRoom, Long inviteeUserId) {
        User invitee = userRepository.getByIdOrThrow(inviteeUserId);

        validateHostAuthority(chatRoom.getPost().getUser().getId(), userId);
        validateNotSelfChat(userId, inviteeUserId);
        validateRoomIsActive(chatRoom);

        if (isAlreadyMember(chatRoom, inviteeUserId)) {
            return new GroupChatInviteMemberResponse(chatRoom.getId(), false);
        }

        validateRoomCapacity(chatRoom);
        boolean invited = saveInvitee(chatRoom, invitee);

        return new GroupChatInviteMemberResponse(chatRoom.getId(), invited);
    }

    private boolean isAlreadyMember(ChatRoom chatRoom, Long inviteeUserId) {
        return chatRoomMemberRepository.existsByChatRoom_IdAndUser_Id(chatRoom.getId(), inviteeUserId);
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
    public static void activateChatOnFirstMessage(ChatRoom room) {
        if (room.getRoomType() != ChatRoomType.GROUP) {
            return;
        }
        room.activateIfPending();
    }

    private static void validateRoomIsActive(ChatRoom room) {
        if (room.getStatus() == ChatRoomStatus.CLOSED || room.getStatus() == ChatRoomStatus.DELETED) {
            throw new ChatRoomClosedException();
        }
    }

    private static void validateHostAuthority(Long postUserId, Long userId) {
        if (!postUserId.equals(userId)) {
            throw new GroupChatRoomInviteAccessDeniedException();
        }
    }

    private static void validateNotSelfChat(Long requesterId, Long targetUserId) {
        if (requesterId.equals(targetUserId)) {
            throw new NotSelfChatException();
        }
    }

    private void validateRoomCapacity(ChatRoom room) {
        int currentMemberCount = chatRoomMemberRepository.countByRoom(room);
        int totalAfterInvite = currentMemberCount + 1;

        if (!room.canAccommodate(totalAfterInvite)) {
            throw new ChatRoomCapacityExceededException();
        }
    }

    /**
     * 그룹 멤버 row 추가는 room lock을 잡은 흐름에서만 수행한다.
     */
    private boolean saveInvitee(ChatRoom room, User user) {
        ChatRoomMember chatRoomMember = ChatRoomMember.create(room, user, ChatMemberRole.GUEST);
        try {
            chatRoomMemberRepository.save(chatRoomMember);
            return true;
        } catch (DataIntegrityViolationException e) {
            if (isAlreadyMember(room, user.getId())) {
                log.warn("그룹 채팅 멤버 중복 초대 경쟁 상태 감지 - roomId={}, userId={}", room.getId(), user.getId());
                return false;
            }
            throw e;
        }
    }
}
