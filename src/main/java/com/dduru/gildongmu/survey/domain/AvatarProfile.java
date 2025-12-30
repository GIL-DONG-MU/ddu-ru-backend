package com.dduru.gildongmu.survey.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "avatar_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AvatarProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "avatar_type", nullable = false, unique = true)
    private AvatarType avatarType;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String personality;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String strength;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String tip;

    @Column(nullable = false, columnDefinition = "JSON")
    private String tags;

    @Builder
    public AvatarProfile(AvatarType avatarType, String description, String personality, String strength, String tip, String tags) {
        this.avatarType = avatarType;
        this.description = description;
        this.personality = personality;
        this.strength = strength;
        this.tip = tip;
        this.tags = tags;
    }
}
