package com.dduru.gildongmu.user.domain;

import com.dduru.gildongmu.user.enums.Gender;
import com.dduru.gildongmu.user.enums.OauthType;
import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    @Column(nullable = true, length = 12)
    private String nickname;

    @Column(name = "profile_image", nullable = false, length = 500)
    private String profileImage;

    @Column(name = "oauth_id", nullable = false, length = 100)
    private String oauthId;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_type", nullable = false)
    private OauthType oauthType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Gender gender;

    // AgeRange는 사용하지 않으므로 주석 처리
    // @Enumerated(EnumType.STRING)
    // @Column(name = "age_range", nullable = true)
    // private AgeRange ageRange;

    @Column(name = "phone_number", nullable = true, length = 20)
    private String phoneNumber;

    @Column(name = "birthday", nullable = true)
    private LocalDate birthday;

    @Builder
    public User(String email, String name, String nickname, String profileImage, String oauthId,
                OauthType oauthType, Gender gender, /* AgeRange ageRange, */ String phoneNumber, LocalDate birthday) {
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.oauthId = oauthId;
        this.oauthType = oauthType;
        this.gender = gender;
        // this.ageRange = ageRange;
        this.phoneNumber = phoneNumber;
        this.birthday = birthday;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

//    public void updateAdditionalInfo(Gender gender, LocalDate birthday, String phoneNumber) {
//        if (gender != null) {
//            this.gender = gender;
//        }
//        if (birthday != null) {
//            this.birthday = birthday;
//        }
//        if (phoneNumber != null) {
//            this.phoneNumber = phoneNumber;
//        }
//    }
}
