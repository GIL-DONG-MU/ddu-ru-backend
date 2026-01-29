package com.dduru.gildongmu.profile.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
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

    @Column(name = "avatar_id")
    private Long avatarId;

    @Column(name = "bg_color_id")
    private Integer bgColorId;

    @Column(name = "uploadedImageUrl", length = 500)
    private String uploadedImageUrl;

    @Column(name = "profile_image_type")
    @Enumerated(EnumType.STRING)
    private ProfileImageType profileImageType;

    @Column(name = "bio", length = 60)
    private String bio;

    @Builder
    public Profile(User user, String nickname, Gender gender, String phoneNumber, LocalDate birthday,
                   Long avatarId, Integer bgColorId, String uploadedImageUrl, ProfileImageType profileImageType, String bio
    ) {
        this.user = user;
        this.nickname = nickname;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.birthday = birthday;
        this.avatarId = avatarId;
        this.bgColorId = bgColorId;
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


    public void updateProfile(String uploadedImageUrl, ProfileImageType profileImageType, Integer bgColorId, String bio) {
        if (uploadedImageUrl != null) {
            this.uploadedImageUrl = uploadedImageUrl;
        }
        if (profileImageType != null) {
            this.profileImageType = profileImageType;
        }
        if (bgColorId != null) {
            this.bgColorId = bgColorId;
        }
        if (bio != null) {
            this.bio = bio;
        }
    }


    public void updateAvatarId(Long avatarId) {
        this.avatarId = avatarId;
    }
}
