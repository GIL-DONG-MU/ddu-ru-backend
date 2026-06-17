package com.dduru.gildongmu.journey.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.journey.domain.enums.ScheduleCategory;
import com.dduru.gildongmu.journey.exception.InvalidJourneyScheduleException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "journey_schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JourneySchedule extends BaseTimeEntity {
    private static final int TITLE_MAX_LENGTH = 30;
    private static final int PLACE_NAME_MAX_LENGTH = 30;
    private static final int MEMO_MAX_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id", nullable = false)
    private Journey journey;

    @Column(nullable = false, length = 30)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ScheduleCategory category;

    @Column(name = "day_offset", nullable = false)
    private int dayOffset;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "place_name", nullable = false, length = 30)
    private String placeName;

    @Column(length = 100)
    private String memo;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Builder(access = AccessLevel.PRIVATE)
    private JourneySchedule(
            Journey journey,
            String title,
            ScheduleCategory category,
            int dayOffset,
            LocalTime startTime,
            LocalTime endTime,
            String placeName,
            String memo,
            String imageUrl
    ) {
        this.journey = journey;
        this.title = validateTitle(title);
        this.category = category;
        this.dayOffset = dayOffset;
        validateTimeConstraint(startTime, endTime);
        this.startTime = startTime;
        this.endTime = endTime;
        this.placeName = validatePlaceName(placeName);
        this.memo = validateMemo(memo);
        this.imageUrl = imageUrl;
        this.isDeleted = false;
    }

    public static JourneySchedule create(
            Journey journey,
            String title,
            ScheduleCategory category,
            int dayOffset,
            LocalTime startTime,
            LocalTime endTime,
            String placeName,
            String memo,
            String imageUrl
    ) {
        return JourneySchedule.builder()
                .journey(journey)
                .title(title)
                .category(category)
                .dayOffset(dayOffset)
                .startTime(startTime)
                .endTime(endTime)
                .placeName(placeName)
                .memo(memo)
                .imageUrl(imageUrl)
                .build();
    }

    public void update(
            String title,
            ScheduleCategory category,
            Integer dayOffset,
            LocalTime startTime,
            LocalTime endTime,
            String placeName,
            boolean applyMemoPatch,
            String memo,
            boolean applyImageUrlPatch,
            String imageUrl
    ) {
        LocalTime effectiveStartTime = startTime != null ? startTime : this.startTime;
        LocalTime effectiveEndTime = endTime != null ? endTime : this.endTime;
        validateTimeConstraint(effectiveStartTime, effectiveEndTime);

        if (title != null) {
            this.title = validateTitle(title);
        }
        if (category != null) {
            this.category = category;
        }
        if (dayOffset != null) {
            this.dayOffset = dayOffset;
        }
        if (startTime != null) {
            this.startTime = startTime;
        }
        if (endTime != null) {
            this.endTime = endTime;
        }
        if (placeName != null) {
            this.placeName = validatePlaceName(placeName);
        }
        if (applyMemoPatch) {
            this.memo = memo != null ? validateMemo(memo) : null;
        }
        if (applyImageUrlPatch) {
            this.imageUrl = imageUrl;
        }
    }

    public void delete(Long deletedBy, LocalDateTime deletedAt) {
        this.isDeleted = true;
        this.deletedBy = deletedBy;
        this.deletedAt = deletedAt;
    }

    private static void validateTimeConstraint(LocalTime startTime, LocalTime endTime) {
        if (endTime != null && startTime == null) {
            throw InvalidJourneyScheduleException.invalidScheduleTime();
        }
        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            throw InvalidJourneyScheduleException.invalidScheduleTime();
        }
    }

    private static String validateTitle(String title) {
        if (!StringUtils.hasText(title)) {
            throw InvalidJourneyScheduleException.invalidTitle();
        }
        int length = title.codePointCount(0, title.length());
        if (length > TITLE_MAX_LENGTH) {
            throw InvalidJourneyScheduleException.invalidTitle();
        }
        return title;
    }

    private static String validatePlaceName(String placeName) {
        if (!StringUtils.hasText(placeName)) {
            throw InvalidJourneyScheduleException.invalidPlaceName();
        }
        int length = placeName.codePointCount(0, placeName.length());
        if (length > PLACE_NAME_MAX_LENGTH) {
            throw InvalidJourneyScheduleException.invalidPlaceName();
        }
        return placeName;
    }

    private static String validateMemo(String memo) {
        if (!StringUtils.hasText(memo)) return null;
        int length = memo.codePointCount(0, memo.length());
        if (length > MEMO_MAX_LENGTH) {
            throw InvalidJourneyScheduleException.invalidMemo();
        }
        return memo;
    }
}
