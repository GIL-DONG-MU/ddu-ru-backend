package com.dduru.gildongmu.survey.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
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
        this.rhythmScore = rhythmScore;
        this.energyScore = energyScore;
        this.consumptionScore = consumptionScore;
        this.decisionScore = decisionScore;
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
        this.rhythmScore = rhythmScore;
        this.energyScore = energyScore;
        this.consumptionScore = consumptionScore;
        this.decisionScore = decisionScore;
        this.avatarType = avatarType;
    }
}
