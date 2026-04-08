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

import static com.dduru.gildongmu.chat.service.PrivateChatRoomService.validateNotSelfChat;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GroupChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;

    public GroupChatInviteMemberResponse inviteMemberOrGetRoom(Long userId, Long roomId, GroupChatInviteRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndRoomType(roomId, ChatRoomType.GROUP).orElseThrow(ChatRoomNotFoundException::new);
        Long inviteeUserId = request.inviteeUserId();
        return inviteMemberOrGetRoom(userId, chatRoom.getPost().getId(), inviteeUserId);
    }

    public GroupChatInviteMemberResponse inviteMemberOrGetRoom(Long userId, Long postId, Long inviteeUserId) {
        User invitee = userRepository.getByIdOrThrow(inviteeUserId);
        ChatRoom chatRoom = getActiveRoomOrThrow(postId);

        validateHostAuthority(chatRoom.getPost().getUser().getId(), userId);
        validateNotSelfChat(userId, inviteeUserId);

        if (isAlreadyMember(chatRoom, inviteeUserId)){
            return new GroupChatInviteMemberResponse(chatRoom.getId(), false);
        }

        validateRoomCapacity(chatRoom);
        saveInvitee(chatRoom, invitee);

        return new GroupChatInviteMemberResponse(chatRoom.getId(), true);
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
    public void activateChatOnFirstMessage(Long roomId) {
        ChatRoom room = chatRoomRepository.getByIdOrThrow(roomId);
        if (room.getRoomType() != ChatRoomType.GROUP) {
            return;
        }
        room.activateIfPending();
    }

    private ChatRoom getActiveRoomOrThrow(Long postId) {
        ChatRoom chatRoom = chatRoomRepository.findByPostIdAndRoomType(postId, ChatRoomType.GROUP)
                .orElseThrow(ChatRoomNotFoundException::new);
        validateRoomIsActive(chatRoom);
        return chatRoom;
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

    private void validateRoomCapacity(ChatRoom room) {
        int currentMemberCount = chatRoomMemberRepository.countByRoom(room);
        int totalAfterInvite = currentMemberCount + 1;

        if (!room.canAccommodate(totalAfterInvite)) {
            throw new ChatRoomCapacityExceededException();
        }
    }

    private void saveInvitee(ChatRoom room, User user) {
        ChatRoomMember chatRoomMember = ChatRoomMember.create(room, user, ChatMemberRole.GUEST);
        chatRoomMemberRepository.save(chatRoomMember);
    }
}
