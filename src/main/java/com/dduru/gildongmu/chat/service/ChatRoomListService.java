package com.dduru.gildongmu.chat.service;

import com.dduru.gildongmu.chat.cursor.ChatRoomListCursorCodec;
import com.dduru.gildongmu.chat.domain.ChatMessage;
import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.ChatRoomMember;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
                .map(result -> {
                    Long roomId = roomIdOf(result);
                    return toItemResponse(
                            userId,
                            result,
                            roomListData.membersByRoomId().getOrDefault(roomId, List.of()),
                            roomListData.lastMessagesByRoomId().get(roomId),
                            roomListData.unreadCountsByRoomId().getOrDefault(roomId, 0L),
                            roomListData.participantCountsByRoomId().getOrDefault(roomId, 0L)
                    );
                })
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
            List<ChatRoomMember> members,
            ChatMessage lastMessage,
            long unreadCount,
            long participantCount
    ) {
        ChatRoomMember currentMember = result.currentMember();
        ChatRoom room = currentMember.getRoom();

        return new ChatRoomListItemResponse(
                room.getId(),
                room.getRoomType(),
                room.getStatus(),
                resolveDisplayName(room, currentUserId, members),
                resolvePostTitle(room),
                resolveThumbnailUrl(room, currentUserId, members),
                resolvePostId(room),
                resolveJourneyId(room),
                Math.toIntExact(participantCount),
                toLastMessageResponse(lastMessage),
                unreadCount,
                currentMember.getLastReadMessage() == null ? null : currentMember.getLastReadMessage().getId(),
                room.getCreatedAt()
        );
    }

    private String resolveDisplayName(ChatRoom room, Long currentUserId, List<ChatRoomMember> members) {
        if (room.getRoomType() == ChatRoomType.GROUP) {
            return room.getJourney().getTitle();
        }
        return findOpponent(members, currentUserId)
                .map(ChatRoomMember::getUser)
                .map(ChatMessageSenderResponse::displayNameOf)
                .orElse(null);
    }

    private static String resolvePostTitle(ChatRoom room) {
        if (room.getRoomType() != ChatRoomType.PRIVATE) {
            return null;
        }
        Post contextPost = room.getContextPost();
        return contextPost == null ? null : contextPost.getTitle();
    }

    private String resolveThumbnailUrl(ChatRoom room, Long currentUserId, List<ChatRoomMember> members) {
        if (room.getRoomType() == ChatRoomType.GROUP) {
            if (hasText(room.getJourney().getPhotoUrl())) {
                return room.getJourney().getPhotoUrl();
            }
            return room.getJourney().getPost().getPhotoUrl();
        }
        return findOpponent(members, currentUserId)
                .map(ChatRoomMember::getUser)
                .map(User::getProfile)
                .map(profileImageResolver::resolve)
                .orElse(null);
    }

    private static java.util.Optional<ChatRoomMember> findOpponent(List<ChatRoomMember> members, Long currentUserId) {
        return members.stream()
                .filter(member -> !member.getUser().getId().equals(currentUserId))
                .findFirst();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
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
