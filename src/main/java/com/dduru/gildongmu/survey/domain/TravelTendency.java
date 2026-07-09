package com.dduru.gildongmu.survey.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.survey.exception.InvalidTravelTendencyScoreException;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "travel_tendencies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelTendency extends BaseTimeEntity {

    private static final BigDecimal MIN_SCORE = BigDecimal.ZERO;
    private static final BigDecimal MAX_SCORE = BigDecimal.TEN;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "rhythm_score", nullable = false, precision = 3, scale = 1)
    private BigDecimal rhythmScore;

    @Column(name = "energy_score", nullable = false, precision = 3, scale = 1)
    private BigDecimal energyScore;

    @Column(name = "consumption_score", nullable = false, precision = 3, scale = 1)
    private BigDecimal consumptionScore;

    @Column(name = "decision_score", nullable = false, precision = 3, scale = 1)
    private BigDecimal decisionScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "avatar_type", nullable = false)
    private AvatarType avatarType;

    @Builder
    public TravelTendency(User user,
                          BigDecimal rhythmScore, BigDecimal energyScore,
                          BigDecimal consumptionScore, BigDecimal decisionScore,
                          AvatarType avatarType) {
        this.user = user;
        updateScores(rhythmScore, energyScore, consumptionScore, decisionScore);
        this.avatarType = avatarType;
    }

    public static TravelTendency create(User user,
                                        BigDecimal rhythmScore, BigDecimal energyScore,
                                        BigDecimal consumptionScore, BigDecimal decisionScore,
                                        AvatarType avatarType) {
        return TravelTendency.builder()
                .user(user)
                .rhythmScore(rhythmScore)
                .energyScore(energyScore)
                .consumptionScore(consumptionScore)
                .decisionScore(decisionScore)
                .avatarType(avatarType)
                .build();
    }

    public void update(BigDecimal rhythmScore, BigDecimal energyScore,
                       BigDecimal consumptionScore, BigDecimal decisionScore,
                       AvatarType avatarType) {
        updateScores(rhythmScore, energyScore, consumptionScore, decisionScore);
        this.avatarType = avatarType;
    }

    public static double scoreRange() {
        return MAX_SCORE.subtract(MIN_SCORE).doubleValue();
    }

    private void updateScores(BigDecimal rhythmScore, BigDecimal energyScore,
                              BigDecimal consumptionScore, BigDecimal decisionScore) {
        this.rhythmScore = requireValidScore(rhythmScore);
        this.energyScore = requireValidScore(energyScore);
        this.consumptionScore = requireValidScore(consumptionScore);
        this.decisionScore = requireValidScore(decisionScore);
    }

    private static BigDecimal requireValidScore(BigDecimal score) {
        if (score == null || score.compareTo(MIN_SCORE) < 0 || score.compareTo(MAX_SCORE) > 0) {
            throw new InvalidTravelTendencyScoreException();
        }
        return score;
    }
}
