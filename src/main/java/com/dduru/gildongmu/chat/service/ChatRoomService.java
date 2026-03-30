package com.dduru.gildongmu.chat.service;

import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.ChatRoomMember;
import com.dduru.gildongmu.chat.domain.enums.ChatMemberRole;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.request.GroupChatInviteRequest;
import com.dduru.gildongmu.chat.dto.response.GroupChatInviteResponse;
import com.dduru.gildongmu.chat.dto.response.PrivateChatRoomCreateResponse;
import com.dduru.gildongmu.chat.repository.ChatRoomMemberRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final PrivateChatRoomService privateChatRoomService;
    private final GroupChatRoomService groupChatRoomService;

    @Transactional
    public PrivateChatRoomCreateResponse createOrGetPrivateRoom(Long requesterId, Long postId) {
        return privateChatRoomService.createOrGetPrivateRoom(requesterId, postId);
    }

    @Transactional
    public GroupChatInviteResponse inviteMembersToGroupRoom(Long requesterId, Long roomId, GroupChatInviteRequest request) {
        return groupChatRoomService.inviteMembersToGroupRoom(requesterId, roomId, request);
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
}
