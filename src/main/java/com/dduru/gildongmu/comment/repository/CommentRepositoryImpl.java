package com.dduru.gildongmu.comment.repository;

import com.dduru.gildongmu.comment.domain.Comment;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dduru.gildongmu.comment.domain.QComment.comment;
import static com.dduru.gildongmu.post.domain.QPost.post;
import static com.dduru.gildongmu.user.domain.QUser.user;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Comment> findCommentsByPostId(Long postId) {
        return queryFactory
                .selectFrom(comment)
                .leftJoin(comment.user, user).fetchJoin()
                .leftJoin(comment.post, post).fetchJoin()
                .where(comment.post.id.eq(postId))
                .orderBy(comment.createdAt.asc())
                .fetch();
    }
}
