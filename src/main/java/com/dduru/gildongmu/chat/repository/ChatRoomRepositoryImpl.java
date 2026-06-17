package com.dduru.gildongmu.chat.repository;

import com.dduru.gildongmu.chat.domain.ChatMessage;
import com.dduru.gildongmu.chat.domain.QChatMessage;
import com.dduru.gildongmu.chat.domain.QChatRoomMember;
import com.dduru.gildongmu.chat.domain.enums.ChatMessageType;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.query.ChatRoomListCursor;
import com.dduru.gildongmu.chat.dto.query.ChatRoomListQueryResult;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dduru.gildongmu.chat.domain.QChatMessage.chatMessage;
import static com.dduru.gildongmu.chat.domain.QChatRoom.chatRoom;
import static com.dduru.gildongmu.chat.domain.QChatRoomMember.chatRoomMember;
import static com.dduru.gildongmu.journey.domain.QJourney.journey;
import static com.dduru.gildongmu.post.domain.QPost.post;
import static com.dduru.gildongmu.profile.domain.QProfile.profile;
import static com.dduru.gildongmu.user.domain.QUser.user;

/**
 * 채팅방 목록 화면에 필요한 QueryDSL 기반 조회를 담당한다.
 * <p>
 * 채팅방 정렬 기준(activityAt), 사용자 참여 시각 이후의 메시지 visibility,
 * SYSTEM 메시지 제외 정책, unread count/참여자 수 집계를 이곳에서 처리한다.
 */
@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChatRoomListQueryResult> findActiveListPageByUserId(
            Long userId,
            ChatRoomType roomType,
            ChatRoomListCursor cursor,
            Pageable pageable
    ) {
        DateTimeExpression<LocalDateTime> activityAt = activityAtExpression();

        List<Tuple> tuples = queryFactory
                .select(chatRoomMember, activityAt)
                .from(chatRoomMember)
                .join(chatRoomMember.room, chatRoom).fetchJoin()
                .leftJoin(chatRoomMember.lastReadMessage).fetchJoin()
                .leftJoin(chatRoom.post, post).fetchJoin()
                .leftJoin(chatRoom.journey, journey).fetchJoin()
                .leftJoin(journey.post).fetchJoin()
                .where(
                        chatRoomMember.user.id.eq(userId),
                        chatRoom.status.eq(ChatRoomStatus.ACTIVE),
                        roomTypeCondition(roomType),
                        cursorCondition(cursor, activityAt)
                )
                .orderBy(activityAt.desc(), chatRoom.id.desc())
                .limit(pageable.getPageSize())
                .fetch();

        return tuples.stream()
                .map(tuple -> new ChatRoomListQueryResult(
                        tuple.get(chatRoomMember),
                        tuple.get(activityAt)
                ))
                .toList();
    }

    @Override
    public List<ChatMessage> findLastVisibleMessagesByRoomIds(Long userId, Collection<Long> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) {
            return List.of();
        }

        QChatMessage lastMessage = new QChatMessage("lastMessage");

        return queryFactory
                .selectFrom(lastMessage)
                .leftJoin(lastMessage.sender, user).fetchJoin()
                .leftJoin(user.profile, profile).fetchJoin()
                .where(
                        lastMessage.room.id.in(roomIds),
                        nonSystemMessage(lastMessage),
                        latestVisibleNonSystemMessage(lastMessage, userId)
                )
                .fetch();
    }

    @Override
    public Map<Long, Long> countUnreadMessagesByRoomIds(Long userId, Collection<Long> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) {
            return Map.of();
        }

        QChatRoomMember currentMember = new QChatRoomMember("currentMember");
        List<Tuple> tuples = queryFactory
                .select(chatMessage.room.id, chatMessage.count())
                .from(chatMessage)
                .join(currentMember)
                .on(
                        currentMember.room.eq(chatMessage.room),
                        currentMember.user.id.eq(userId)
                )
                .where(
                        chatMessage.room.id.in(roomIds),
                        chatMessage.messageType.ne(ChatMessageType.SYSTEM),
                        chatMessage.createdAt.goe(currentMember.createdAt),
                        currentMember.lastReadMessage.isNull()
                                .or(chatMessage.id.gt(currentMember.lastReadMessage.id))
                )
                .groupBy(chatMessage.room.id)
                .fetch();

        return tuples.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(chatMessage.room.id),
                        tuple -> tuple.get(chatMessage.count())
                ));
    }

    @Override
    public Map<Long, Long> countMembersByRoomIds(Collection<Long> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) {
            return Map.of();
        }

        QChatRoomMember participant = new QChatRoomMember("participant");
        List<Tuple> tuples = queryFactory
                .select(participant.room.id, participant.count())
                .from(participant)
                .where(participant.room.id.in(roomIds))
                .groupBy(participant.room.id)
                .fetch();

        return tuples.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(participant.room.id),
                        tuple -> tuple.get(participant.count())
                ));
    }

    private DateTimeExpression<LocalDateTime> activityAtExpression() {
        QChatMessage message = new QChatMessage("activityMessage");

        return Expressions.dateTimeTemplate(
                LocalDateTime.class,
                "coalesce({0}, {1})",
                JPAExpressions
                        .select(message.createdAt.max())
                        .from(message)
                        .where(
                                message.room.eq(chatRoom),
                                nonSystemMessage(message),
                                message.createdAt.goe(chatRoomMember.createdAt)
                        ),
                chatRoomMember.createdAt
        );
    }

    private BooleanExpression nonSystemMessage(QChatMessage message) {
        return message.messageType.ne(ChatMessageType.SYSTEM);
    }

    private BooleanExpression latestVisibleNonSystemMessage(QChatMessage lastMessage, Long userId) {
        QChatMessage sameTimeMessage = new QChatMessage("sameTimeMessage");

        return lastMessage.id.eq(
                JPAExpressions
                        .select(sameTimeMessage.id.max())
                        .from(sameTimeMessage)
                        .where(
                                sameTimeMessage.room.id.eq(lastMessage.room.id),
                                nonSystemMessage(sameTimeMessage),
                                sameTimeMessage.createdAt.eq(latestVisibleMessageCreatedAt(lastMessage, userId))
                        )
        );
    }

    private JPQLQuery<LocalDateTime> latestVisibleMessageCreatedAt(QChatMessage roomScopedMessage, Long userId) {
        QChatMessage maxTimeMessage = new QChatMessage("maxTimeMessage");

        return JPAExpressions
                .select(maxTimeMessage.createdAt.max())
                .from(maxTimeMessage)
                .where(
                        maxTimeMessage.room.id.eq(roomScopedMessage.room.id),
                        nonSystemMessage(maxTimeMessage),
                        maxTimeMessage.createdAt.goe(memberJoinedAt(roomScopedMessage, userId))
                );
    }

    private JPQLQuery<LocalDateTime> memberJoinedAt(QChatMessage roomScopedMessage, Long userId) {
        QChatRoomMember visibleMember = new QChatRoomMember("visibleMember");

        return JPAExpressions
                .select(visibleMember.createdAt)
                .from(visibleMember)
                .where(
                        visibleMember.room.id.eq(roomScopedMessage.room.id),
                        visibleMember.user.id.eq(userId)
                );
    }

    private BooleanExpression roomTypeCondition(ChatRoomType roomType) {
        if (roomType == null) {
            return null;
        }
        return chatRoom.roomType.eq(roomType);
    }

    private BooleanExpression cursorCondition(
            ChatRoomListCursor cursor,
            DateTimeExpression<LocalDateTime> activityAt
    ) {
        if (cursor == null) {
            return null;
        }
        return activityAt.lt(cursor.activityAt())
                .or(activityAt.eq(cursor.activityAt()).and(chatRoom.id.lt(cursor.chatRoomId())));
    }
}
