package com.dduru.gildongmu.profile.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.survey.domain.AvatarProfile;
import com.dduru.gildongmu.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 14, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "birthday")
    private LocalDate birthday;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar_id")
    private AvatarProfile avatar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bg_color_id")
    private BgColor bgColor;

    @Column(name = "uploaded_image_url", length = 500)
    private String uploadedImageUrl;

    @Column(name = "profile_image_type")
    @Enumerated(EnumType.STRING)
    private ProfileImageType profileImageType;

    @Column(name = "bio", length = 60)
    private String bio;

    @Builder
    public Profile(User user, String nickname, Gender gender, String phoneNumber, LocalDate birthday,
                   AvatarProfile avatar, BgColor bgColor, String uploadedImageUrl, ProfileImageType profileImageType, String bio
    ) {
        this.user = user;
        this.nickname = nickname;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.birthday = birthday;
        this.avatar = avatar;
        this.bgColor = bgColor;
        this.uploadedImageUrl = uploadedImageUrl;
        this.profileImageType = profileImageType;
        this.bio = bio;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setupInitialProfile(String nickname, Gender gender, String phoneNumber, LocalDate birthday) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (gender != null) {
            this.gender = gender;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
        if (birthday != null) {
            this.birthday = birthday;
        }
    }


    public void updateProfile(String uploadedImageUrl, ProfileImageType profileImageType, BgColor bgColor, String bio) {
        if (uploadedImageUrl != null) {
            this.uploadedImageUrl = uploadedImageUrl;
        }
        if (profileImageType != null) {
            this.profileImageType = profileImageType;
        }
        if (bgColor != null) {
            this.bgColor = bgColor;
        }
        if (bio != null) {
            this.bio = bio;
        }
    }

    public void updateAvatar(AvatarProfile avatar) {
        this.avatar = avatar;
    }
}
