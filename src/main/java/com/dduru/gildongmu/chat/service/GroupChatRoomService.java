package com.dduru.gildongmu.chat.service;

import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.ChatRoomMember;
import com.dduru.gildongmu.chat.domain.enums.ChatMemberRole;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.response.GroupChatInviteMemberResponse;
import com.dduru.gildongmu.chat.exception.ChatRoomCapacityExceededException;
import com.dduru.gildongmu.chat.exception.ChatRoomClosedException;
import com.dduru.gildongmu.chat.exception.GroupChatRoomInviteAccessDeniedException;
import com.dduru.gildongmu.chat.exception.NotSelfChatException;
import com.dduru.gildongmu.chat.repository.ChatRoomMemberRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.journey.domain.Journey;
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
@Transactional
@RequiredArgsConstructor
public class GroupChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;
    private final ChatMessageSendService chatMessageSendService;

    public GroupChatInviteMemberResponse inviteMemberOrGetRoom(Long userId, Long journeyId, Long inviteeUserId) {
        ChatRoom chatRoom = chatRoomRepository.getByJourneyIdAndRoomTypeWithLock(journeyId, ChatRoomType.GROUP);
        return inviteMemberOrGetRoom(userId, chatRoom, inviteeUserId);
    }

    /**
     * 그룹 멤버 추가는 항상 {@code ChatRoom} 락을 잡은 뒤 이 메서드로만 진입한다.
     */
    private GroupChatInviteMemberResponse inviteMemberOrGetRoom(Long userId, ChatRoom chatRoom, Long inviteeUserId) {
        User invitee = userRepository.getByIdOrThrow(inviteeUserId);

        validateHostAuthority(chatRoom.getContextPost().getUser().getId(), userId);
        validateNotSelfChat(userId, inviteeUserId);
        validateRoomOpen(chatRoom);

        if (isAlreadyMember(chatRoom, inviteeUserId)) {
            return new GroupChatInviteMemberResponse(chatRoom.getId(), false);
        }

        validateRoomCapacity(chatRoom);
        boolean invited = saveInvitee(chatRoom, invitee);
        if (invited) {
            chatMessageSendService.publishUserInvited(chatRoom, invitee.getId(), userId);
        }

        return new GroupChatInviteMemberResponse(chatRoom.getId(), invited);
    }

    private boolean isAlreadyMember(ChatRoom chatRoom, Long inviteeUserId) {
        return chatRoomMemberRepository.existsByChatRoom_IdAndUser_Id(chatRoom.getId(), inviteeUserId);
    }

    /**
     * 나의 여정 생성 직후 호출. 여정당 그룹 단톡 1개를 만들고 작성자를 HOST로 둔다.
     */
    public void createRoomForJourney(Journey journey, User author) {
        ChatRoom room = chatRoomRepository.save(ChatRoom.createGroupChat(journey));
        chatRoomMemberRepository.save(ChatRoomMember.create(room, author, ChatMemberRole.HOST));
        log.info("그룹 채팅방 생성 - roomId={}, journeyId={}, hostId={}", room.getId(), journey.getId(), author.getId());
    }

    private static void validateRoomOpen(ChatRoom room) {
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
