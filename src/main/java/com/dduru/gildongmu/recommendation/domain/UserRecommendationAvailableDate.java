package com.dduru.gildongmu.recommendation.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.recommendation.exception.InvalidAvailableDateException;
import com.dduru.gildongmu.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "user_recommendation_available_dates",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_recommendation_available_dates_user_range",
                columnNames = {"user_id", "start_date", "end_date"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRecommendationAvailableDate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    private UserRecommendationAvailableDate(User user, LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        this.user = user;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static UserRecommendationAvailableDate of(User user, LocalDate startDate, LocalDate endDate) {
        return new UserRecommendationAvailableDate(user, startDate, endDate);
    }

    private static void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new InvalidAvailableDateException();
        }
    }
}
