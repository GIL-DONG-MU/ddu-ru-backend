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

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal r;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal w;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal s;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal p;

    @Enumerated(EnumType.STRING)
    @Column(name = "avatar_type", nullable = false)
    private AvatarType avatarType;

    @Builder
    public TravelTendency(User user, BigDecimal r, BigDecimal w, BigDecimal s, BigDecimal p, AvatarType avatarType) {
        this.user = user;
        this.r = r;
        this.w = w;
        this.s = s;
        this.p = p;
        this.avatarType = avatarType;
    }

    public static TravelTendency create(User user, BigDecimal r, BigDecimal w, BigDecimal s, BigDecimal p, AvatarType avatarType) {
        return TravelTendency.builder()
                .user(user)
                .r(r)
                .w(w)
                .s(s)
                .p(p)
                .avatarType(avatarType)
                .build();
    }

    public void update(BigDecimal r, BigDecimal w, BigDecimal s, BigDecimal p, AvatarType avatarType) {
        this.r = r;
        this.w = w;
        this.s = s;
        this.p = p;
        this.avatarType = avatarType;
    }
}
