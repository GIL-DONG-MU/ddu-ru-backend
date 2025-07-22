package com.dduru.gildongmu.auth.domain;

import com.dduru.gildongmu.auth.enums.AgeGroup;
import com.dduru.gildongmu.auth.enums.Gender;
import com.dduru.gildongmu.auth.enums.OauthType;
import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "profile_image", nullable = false, length = 500)
    private String profileImage;

    @Column(name = "oauth_id", nullable = false, length = 100)
    private String oauthId;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_type", nullable = false)
    private OauthType oauthType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", nullable = false)
    private AgeGroup ageGroup;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Builder
    public User(String email, String name, String profileImage, String oauthId,
                OauthType oauthType, Gender gender, AgeGroup ageGroup, String phoneNumber) {
        this.email = email;
        this.name = name;
        this.profileImage = profileImage;
        this.oauthId = oauthId;
        this.oauthType = oauthType;
        this.gender = gender;
        this.ageGroup = ageGroup;
        this.phoneNumber = phoneNumber;
    }
}
