package com.dduru.gildongmu.post.repository;

import com.dduru.gildongmu.user.enums.AgeRange;
import com.dduru.gildongmu.user.enums.Gender;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.dto.PostListRequest;
import com.dduru.gildongmu.post.enums.PostStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static com.dduru.gildongmu.destination.domain.QDestination.destination;
import static com.dduru.gildongmu.post.domain.QPost.post;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Post> findPostsWithFilters(PostListRequest request, Pageable pageable) {
        return queryFactory
                .selectFrom(post)
                .leftJoin(post.destination, destination).fetchJoin()
                .leftJoin(post.user).fetchJoin()
                .where(
                        isNotDeleted(),
                        cursorCondition(request.cursor()),
                        keywordCondition(request.keyword()),
                        dateRangeCondition(request.startDate(), request.endDate()),
                        genderCondition(request.preferredGender()),
                        ageRangeCondition(request.preferredAge()),
                        destinationCondition(request.destinationId()),
                        recruitmentStatusCondition(request.isRecruitOpen())
                )
                .orderBy(post.id.desc())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanExpression isNotDeleted() {
        return post.isDeleted.eq(false);
    }

    private BooleanExpression cursorCondition(Long cursor) {
        if (cursor == null) {
            return null;
        }
        return post.id.lt(cursor);
    }

    private BooleanExpression keywordCondition(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
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

    private BooleanExpression genderCondition(String preferredGenderStr) {
        if (preferredGenderStr == null || preferredGenderStr.trim().isEmpty()) {
            return null;
        }

        try {
            Gender preferredGender = Gender.valueOf(preferredGenderStr.toUpperCase());
            return post.preferredGender.eq(preferredGender);
        } catch (IllegalArgumentException e) {
            Gender preferredGender = Gender.from(preferredGenderStr);
            if (preferredGender == Gender.U && !preferredGenderStr.equalsIgnoreCase("U")) {
                return null;
            }
            return post.preferredGender.eq(preferredGender);
        }
    }

    private BooleanExpression ageRangeCondition(String preferredAgeStr) {
        if (preferredAgeStr == null || preferredAgeStr.trim().isEmpty()) {
            return null;
        }

        try {
            AgeRange preferredAge = AgeRange.valueOf(preferredAgeStr.toUpperCase());
            return buildAgeRangeExpression(preferredAge);
        } catch (IllegalArgumentException e) {
            AgeRange preferredAge = AgeRange.from(preferredAgeStr);
            if (preferredAge == AgeRange.UNKNOWN) {
                return null;
            }
            return buildAgeRangeExpression(preferredAge);
        }
    }

    private BooleanExpression buildAgeRangeExpression(AgeRange preferredAge) {
        return post.preferredAgeMin.eq(preferredAge)
                .or(post.preferredAgeMax.eq(preferredAge))
                .or(post.preferredAgeMin.loe(preferredAge)
                        .and(post.preferredAgeMax.goe(preferredAge)));
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

        LocalDate currentDate = LocalDate.now();

        if (isRecruitOpen) {
            return post.status.eq(PostStatus.OPEN)
                    .and(post.recruitDeadline.goe(currentDate))
                    .and(post.recruitCount.lt(post.recruitCapacity));
        } else {
            return post.status.eq(PostStatus.FULL)
                    .or(post.status.eq(PostStatus.CLOSED))
                    .or(post.recruitDeadline.lt(currentDate))
                    .or(post.recruitCount.goe(post.recruitCapacity));
        }
    }
}
