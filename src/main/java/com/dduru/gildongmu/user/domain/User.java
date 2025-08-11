package com.dduru.gildongmu.user.domain;

import com.dduru.gildongmu.user.enums.AgeRange;
import com.dduru.gildongmu.user.enums.Gender;
import com.dduru.gildongmu.user.enums.OauthType;
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

    @Column(nullable = false, length = 12)
    private String nickname;

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
    @Column(name = "age_range", nullable = false)
    private AgeRange ageRange;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Builder
    public User(String email, String name, String nickname, String profileImage, String oauthId,
                OauthType oauthType, Gender gender, AgeRange ageRange, String phoneNumber) {
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.oauthId = oauthId;
        this.oauthType = oauthType;
        this.gender = gender;
        this.ageRange = ageRange;
        this.phoneNumber = phoneNumber;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
