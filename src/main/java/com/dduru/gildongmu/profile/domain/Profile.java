package com.dduru.gildongmu.profile.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.exception.InvalidProfileImageUrlException;
import com.dduru.gildongmu.profile.validator.NicknameBadWordValidator;
import com.dduru.gildongmu.survey.domain.AvatarProfile;
import com.dduru.gildongmu.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Entity
@Table(name = "profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile extends BaseTimeEntity {

    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9가-힣]+( [a-zA-Z0-9가-힣]+)*$");

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

    @Column(name = "phone_number", length = 20, unique = true)
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

    public Profile(User user) {
        this.user = user;
    }

    public void setupInitialProfile(Gender gender, String phoneNumber, LocalDate birthday) {
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

    public void updateProfile(String nickname, String uploadedImageUrl, ProfileImageType profileImageType, BgColor bgColor, String bio) {
        validateUploadedImageUrlForType(profileImageType, uploadedImageUrl);

        updateNickname(nickname);

        if (profileImageType == ProfileImageType.DEFAULT) {
            this.uploadedImageUrl = null;
        } else if (uploadedImageUrl != null) {
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

    public void updateNickname(String nickname) {
        validateNickname(nickname);
        this.nickname = nickname;
    }

    public static void validateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new BusinessException(ErrorCode.NICKNAME_NOT_BLANK);
        }

        if (nickname.length() < 2 || nickname.length() > 14) {
            throw new BusinessException(ErrorCode.NICKNAME_INVALID_LENGTH);
        }

        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            throw new BusinessException(ErrorCode.NICKNAME_INVALID_CHARACTERS);
        }

        if (!NicknameBadWordValidator.validate(nickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_CONTAINS_BAD_WORD);
        }
    }

    private void validateUploadedImageUrlForType(ProfileImageType profileImageType, String uploadedImageUrl) {
        if (profileImageType == ProfileImageType.UPLOADED && (uploadedImageUrl == null || uploadedImageUrl.isBlank())) {
            throw InvalidProfileImageUrlException.uploadedTypeRequiresUrl();
        }
    }
}
