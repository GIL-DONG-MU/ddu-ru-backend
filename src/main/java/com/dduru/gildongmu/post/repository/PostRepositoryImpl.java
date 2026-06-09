package com.dduru.gildongmu.post.repository;

import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.dto.request.PostListRequest;
import com.dduru.gildongmu.post.domain.enums.PostSortType;
import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static com.dduru.gildongmu.destination.domain.QDestination.destination;
import static com.dduru.gildongmu.post.domain.QPost.post;
import static com.dduru.gildongmu.profile.domain.QProfile.profile;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Post> findPostsWithFilters(PostListRequest request, Post cursorPost, Pageable pageable) {
        return queryFactory
                .selectFrom(post)
                .leftJoin(post.destination, destination).fetchJoin()
                .leftJoin(post.user).fetchJoin()
                .leftJoin(post.user.profile, profile).fetchJoin()
                .where(
                        isNotDeleted(),
                        cursorCondition(request.cursor(), cursorPost, request.sort()),
                        keywordCondition(request.keyword()),
                        dateRangeCondition(request.startDate(), request.endDate()),
                        genderCondition(request.preferredGender()),
                        ageRangeCondition(request.preferredAge()),
                        destinationCondition(request.destinationId()),
                        recruitmentStatusCondition(request.isRecruitOpen())
                )
                .orderBy(sortOrder(request.sort()))
                .limit(pageable.getPageSize())
                .fetch();
    }

    private OrderSpecifier<?>[] sortOrder(PostSortType sort) {
        return switch (sort) {
            case VIEW -> new OrderSpecifier[]{post.viewCount.desc(), post.id.desc()};
            case LIKE -> new OrderSpecifier[]{post.likeCount.desc(), post.id.desc()};
            default -> new OrderSpecifier[]{post.id.desc()};
        };
    }

    private BooleanExpression isNotDeleted() {
        return post.isDeleted.eq(false);
    }

    private BooleanExpression cursorCondition(Long cursorId, Post cursorPost, PostSortType sort) {
        if (cursorId == null) return null;
        return switch (sort) {
            case VIEW -> cursorPost == null ? null :
                    post.viewCount.lt(cursorPost.getViewCount())
                            .or(post.viewCount.eq(cursorPost.getViewCount()).and(post.id.lt(cursorId)));
            case LIKE -> cursorPost == null ? null :
                    post.likeCount.lt(cursorPost.getLikeCount())
                            .or(post.likeCount.eq(cursorPost.getLikeCount()).and(post.id.lt(cursorId)));
            default -> post.id.lt(cursorId);
        };
    }

    private BooleanExpression keywordCondition(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return null;
        }
        return post.title.containsIgnoreCase(keyword)
                .or(post.content.containsIgnoreCase(keyword));
    }

    private BooleanExpression dateRangeCondition(LocalDate startDate, LocalDate endDate) {
        BooleanExpression condition = null;

        if (startDate != null) {
            condition = post.endDate.goe(startDate);
        }

        if (endDate != null) {
            BooleanExpression endCondition = post.startDate.loe(endDate);
            condition = (condition != null) ? condition.and(endCondition) : endCondition;
        }

        return condition;
    }

    private BooleanExpression genderCondition(Gender preferredGender) {
        if (preferredGender == null || preferredGender == Gender.U) {
            return null;
        }
        return post.preferredGender.eq(preferredGender);
    }

    private BooleanExpression ageRangeCondition(Integer preferredAge) {
        if (preferredAge == null) {
            return null;
        }
        BooleanExpression ageAny = post.isAgeAny.eq(true);
        BooleanExpression inRange = post.isAgeAny.eq(false)
                .and(post.minAge.isNotNull())
                .and(post.maxAge.isNotNull())
                .and(post.minAge.loe(preferredAge))
                .and(post.maxAge.goe(preferredAge));
        return ageAny.or(inRange);
    }

    private BooleanExpression destinationCondition(Long destinationId) {
        if (destinationId == null) {
            return null;
        }
        return post.destination.id.eq(destinationId);
    }

    private BooleanExpression recruitmentStatusCondition(Boolean isRecruitOpen) {
        if (isRecruitOpen == null) {
            return null;
        }

        if (isRecruitOpen) {
            return post.status.eq(PostStatus.OPEN);
        } else {
            return post.status.ne(PostStatus.OPEN);
        }
    }
}
