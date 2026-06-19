package com.dduru.gildongmu.chat.service;

import com.dduru.gildongmu.chat.cursor.ChatRoomListCursorCodec;
import com.dduru.gildongmu.chat.domain.ChatMessage;
import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.ChatRoomMember;
import com.dduru.gildongmu.chat.dto.query.ChatRoomListCursor;
import com.dduru.gildongmu.chat.dto.query.ChatRoomListQueryResult;
import com.dduru.gildongmu.chat.dto.request.ChatRoomListRequest;
import com.dduru.gildongmu.chat.dto.request.ChatRoomListType;
import com.dduru.gildongmu.chat.dto.response.ChatMessageSenderResponse;
import com.dduru.gildongmu.chat.dto.response.ChatRoomLastMessageResponse;
import com.dduru.gildongmu.chat.dto.response.ChatRoomListItemResponse;
import com.dduru.gildongmu.chat.dto.response.ChatRoomListPageResponse;
import com.dduru.gildongmu.chat.dto.response.ChatRoomListResponse;
import com.dduru.gildongmu.chat.repository.ChatRoomMemberRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomListService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ProfileImageResolver profileImageResolver;
    private final ChatRoomListCursorCodec cursorCodec;

    public ChatRoomListResponse retrieveChatRooms(Long userId, ChatRoomListRequest request) {
        ChatRoomListType selectedRoomType = request.roomTypeOrDefault();
        int size = request.sizeOrDefault();

        ChatRoomPage page = findChatRoomPage(userId, selectedRoomType, request, size);
        RoomListData roomListData = loadRoomListData(userId, page.results());

        List<ChatRoomListItemResponse> chatRooms = createListItems(userId, page, roomListData);

        return new ChatRoomListResponse(
                selectedRoomType,
                chatRooms,
                new ChatRoomListPageResponse(
                        resolveNextCursor(page.results(), page.hasNext()), page.hasNext())
        );
    }

    /**
     * 실시간 채팅방 목록 이벤트에 사용할 단건 목록 item을 조회한다.
     * <p>
     * REST 목록 조회와 동일한 조립 정책을 사용해 사용자별 unread count, 상대 프로필, 표시명을 계산한다.
     */
    public Optional<ChatRoomListItemResponse> retrieveChatRoomItem(Long userId, Long roomId) {
        Optional<ChatRoomListQueryResult> result = chatRoomRepository.findActiveListItemByUserIdAndRoomId(userId, roomId);
        if (result.isEmpty()) {
            return Optional.empty();
        }

        ChatRoomListQueryResult itemResult = result.get();
        RoomListData roomListData = loadRoomListData(userId, List.of(itemResult));
        return Optional.of(toItemResponse(userId, itemResult, roomListData));
    }

    private ChatRoomPage findChatRoomPage(
            Long userId,
            ChatRoomListType selectedRoomType,
            ChatRoomListRequest request,
            int size
    ) {
        List<ChatRoomListQueryResult> fetchedPageResults = chatRoomRepository.findActiveListPageByUserId(
                userId,
                selectedRoomType.toChatRoomTypeOrNull(),
                cursorCodec.decode(request.cursor()),
                PageRequest.of(0, size + 1)
        );

        boolean hasNext = fetchedPageResults.size() > size;
        List<ChatRoomListQueryResult> pageResults = truncateToSize(fetchedPageResults, size);
        return new ChatRoomPage(pageResults, hasNext);
    }

    private RoomListData loadRoomListData(
            Long userId,
            List<ChatRoomListQueryResult> pageResults
    ) {
        List<Long> roomIds = pageResults.stream()
                .map(result -> result.currentMember().getRoom().getId())
                .toList();
        if (roomIds.isEmpty()) {
            return RoomListData.empty();
        }

        Map<Long, List<ChatRoomMember>> membersByRoomId = loadMembersByRoomId(roomIds);
        Map<Long, ChatMessage> lastMessagesByRoomId = loadLastMessagesByRoomId(userId, roomIds);
        Map<Long, Long> unreadCountsByRoomId = chatRoomRepository.countUnreadMessagesByRoomIds(userId, roomIds);
        Map<Long, Long> participantCountsByRoomId = chatRoomRepository.countMembersByRoomIds(roomIds);

        return new RoomListData(
                membersByRoomId,
                lastMessagesByRoomId,
                unreadCountsByRoomId,
                participantCountsByRoomId
        );
    }

    private List<ChatRoomListItemResponse> createListItems(
            Long userId,
            ChatRoomPage page,
            RoomListData roomListData
    ) {
        return page.results().stream()
                .map(result -> toItemResponse(userId, result, roomListData))
                .toList();
    }

    private static Long roomIdOf(ChatRoomListQueryResult result) {
        return result.currentMember().getRoom().getId();
    }

    private record ChatRoomPage(
            List<ChatRoomListQueryResult> results,
            boolean hasNext
    ) {
    }

    private record RoomListData(
            Map<Long, List<ChatRoomMember>> membersByRoomId,
            Map<Long, ChatMessage> lastMessagesByRoomId,
            Map<Long, Long> unreadCountsByRoomId,
            Map<Long, Long> participantCountsByRoomId
    ) {
        private static RoomListData empty() {
            return new RoomListData(Map.of(), Map.of(), Map.of(), Map.of());
        }

        private List<ChatRoomMember> membersOf(Long roomId) {
            return membersByRoomId.getOrDefault(roomId, List.of());
        }

        private ChatMessage lastMessageOf(Long roomId) {
            return lastMessagesByRoomId.get(roomId);
        }

        private long unreadCountOf(Long roomId) {
            return unreadCountsByRoomId.getOrDefault(roomId, 0L);
        }

        private long participantCountOf(Long roomId) {
            return participantCountsByRoomId.getOrDefault(roomId, 0L);
        }
    }

    private static List<ChatRoomListQueryResult> truncateToSize(
            List<ChatRoomListQueryResult> results,
            int size
    ) {
        if (results.size() <= size) {
            return results;
        }
        return new ArrayList<>(results.subList(0, size));
    }

    private Map<Long, List<ChatRoomMember>> loadMembersByRoomId(List<Long> roomIds) {
        if (roomIds.isEmpty()) {
            return Map.of();
        }
        return chatRoomMemberRepository.findByRoomIdsWithUserProfileImage(roomIds).stream()
                .collect(Collectors.groupingBy(member -> member.getRoom().getId()));
    }

    private Map<Long, ChatMessage> loadLastMessagesByRoomId(Long userId, List<Long> roomIds) {
        if (roomIds.isEmpty()) {
            return Map.of();
        }
        return chatRoomRepository.findLastVisibleMessagesByRoomIds(userId, roomIds).stream()
                .collect(Collectors.toMap(message -> message.getRoom().getId(), Function.identity()));
    }

    private ChatRoomListItemResponse toItemResponse(
            Long currentUserId,
            ChatRoomListQueryResult result,
            RoomListData roomListData
    ) {
        ChatRoomMember currentMember = result.currentMember();
        ChatRoom room = currentMember.getRoom();
        Long roomId = room.getId();
        List<ChatRoomMember> members = roomListData.membersOf(roomId);

        return new ChatRoomListItemResponse(
                room.getId(),
                room.getRoomType(),
                room.getStatus(),
                resolveDisplayName(room, currentUserId, members),
                resolvePostTitle(room),
                resolveThumbnailUrl(room, currentUserId, members),
                resolvePostId(room),
                resolveJourneyId(room),
                Math.toIntExact(roomListData.participantCountOf(roomId)),
                toLastMessageResponse(roomListData.lastMessageOf(roomId)),
                roomListData.unreadCountOf(roomId),
                currentMember.getLastReadMessage() == null ? null : currentMember.getLastReadMessage().getId(),
                result.activityAt(),
                room.getCreatedAt()
        );
    }

    private String resolveDisplayName(ChatRoom room, Long currentUserId, List<ChatRoomMember> members) {
        return switch (room.getRoomType()) {
            case PRIVATE -> resolvePrivateDisplayName(currentUserId, members);
            case GROUP -> room.getJourney().getTitle();
        };
    }

    private static String resolvePrivateDisplayName(Long currentUserId, List<ChatRoomMember> members) {
        return findOpponent(members, currentUserId)
                .map(ChatRoomMember::getUser)
                .map(ChatMessageSenderResponse::displayNameOf)
                .orElse(null);
    }

    private static String resolvePostTitle(ChatRoom room) {
        return switch (room.getRoomType()) {
            case PRIVATE -> resolvePrivatePostTitle(room);
            case GROUP -> null;
        };
    }

    private static String resolvePrivatePostTitle(ChatRoom room) {
        Post contextPost = room.getContextPost();
        return contextPost == null ? null : contextPost.getTitle();
    }

    private String resolveThumbnailUrl(ChatRoom room, Long currentUserId, List<ChatRoomMember> members) {
        return switch (room.getRoomType()) {
            case PRIVATE -> resolvePrivateThumbnailUrl(currentUserId, members);
            case GROUP -> resolveGroupThumbnailUrl(room);
        };
    }

    private String resolvePrivateThumbnailUrl(Long currentUserId, List<ChatRoomMember> members) {
        return findOpponent(members, currentUserId)
                .map(ChatRoomMember::getUser)
                .map(User::getProfile)
                .map(profileImageResolver::resolve)
                .orElse(null);
    }

    private static String resolveGroupThumbnailUrl(ChatRoom room) {
        if (StringUtils.hasText(room.getJourney().getPhotoUrl())) {
            return room.getJourney().getPhotoUrl();
        }
        return room.getJourney().getPost().getPhotoUrl();
    }

    private static java.util.Optional<ChatRoomMember> findOpponent(List<ChatRoomMember> members, Long currentUserId) {
        return members.stream()
                .filter(member -> !member.getUser().getId().equals(currentUserId))
                .findFirst();
    }

    private static Long resolvePostId(ChatRoom room) {
        Post contextPost = room.getContextPost();
        return contextPost == null ? null : contextPost.getId();
    }

    private static Long resolveJourneyId(ChatRoom room) {
        return room.getJourney() == null ? null : room.getJourney().getId();
    }

    private static ChatRoomLastMessageResponse toLastMessageResponse(ChatMessage message) {
        if (message == null) {
            return null;
        }

        User sender = message.getSender();
        return new ChatRoomLastMessageResponse(
                message.getId(),
                message.getMessageType(),
                message.getContent(),
                sender == null ? null : sender.getId(),
                sender == null ? null : ChatMessageSenderResponse.displayNameOf(sender),
                message.getCreatedAt()
        );
    }

    private String resolveNextCursor(List<ChatRoomListQueryResult> pageResults, boolean hasNext) {
        if (!hasNext || pageResults.isEmpty()) {
            return null;
        }
        ChatRoomListQueryResult lastResult = pageResults.get(pageResults.size() - 1);
        return cursorCodec.encode(new ChatRoomListCursor(
                lastResult.activityAt(),
                lastResult.currentMember().getRoom().getId()
        ));
    }
}
