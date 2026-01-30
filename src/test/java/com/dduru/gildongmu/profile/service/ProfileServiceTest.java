package com.dduru.gildongmu.profile.service;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.common.jwt.JwtTokenProvider;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.dto.ProfileSetupRequest;
import com.dduru.gildongmu.profile.dto.request.ProfileUpdateRequest;
import com.dduru.gildongmu.profile.repository.ProfileRepository;
import com.dduru.gildongmu.profile.utils.NicknameGenerator;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.enums.OauthType;
import com.dduru.gildongmu.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileService 프로필 생성 테스트")
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private NicknameGenerator nicknameGenerator;

    @InjectMocks
    private ProfileService profileService;

    @Test
    @DisplayName("정상적인 프로필 생성 성공")
    void 성공() {
        // given
        Long userId = 1L;
        String nickname = "테스트닉네임";
        String phoneNumber = "01012345678";
        String verificationToken = "valid-token";
        ProfileSetupRequest request = new ProfileSetupRequest(
                nickname,
                "M",
                phoneNumber,
                "2000-01-01",
                verificationToken
        );

        User user = User.builder()
                .email("test@example.com")
                .name("테스트사용자")
                .oauthId("12345")
                .oauthType(OauthType.KAKAO)
                .build();

        Profile profile = Profile.builder()
                .user(user)
                .build();

        when(userRepository.getByIdOrThrow(userId)).thenReturn(user);
        when(profileRepository.findByUser(user)).thenReturn(Optional.of(profile));
        when(profileRepository.existsByNicknameWithLock(nickname)).thenReturn(false);
        when(jwtTokenProvider.validateVerificationToken(verificationToken, phoneNumber)).thenReturn(true);
        when(profileRepository.existsByPhoneNumber(phoneNumber)).thenReturn(false);
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        // when
        profileService.setupInitialProfile(userId, request);

        // then
        verify(profileRepository).existsByNicknameWithLock(nickname);
        verify(jwtTokenProvider).validateVerificationToken(verificationToken, phoneNumber);
        verify(profileRepository).existsByPhoneNumber(phoneNumber);
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    @DisplayName("닉네임 중복 시 NICKNAME_ALREADY_TAKEN 예외 발생 (비관적 잠금 사용)")
    void 닉네임중복_예외발생() {
        // given
        Long userId = 1L;
        String duplicateNickname = "중복닉네임";
        ProfileSetupRequest request = new ProfileSetupRequest(
                duplicateNickname,
                "M",
                "01012345678",
                "2000-01-01",
                "valid-token"
        );

        User user = User.builder()
                .email("test@example.com")
                .name("테스트사용자")
                .oauthId("12345")
                .oauthType(OauthType.KAKAO)
                .build();

        Profile profile = Profile.builder()
                .user(user)
                .build();

        when(userRepository.getByIdOrThrow(userId)).thenReturn(user);
        when(profileRepository.findByUser(user)).thenReturn(Optional.of(profile));
        when(profileRepository.existsByNicknameWithLock(duplicateNickname)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> profileService.setupInitialProfile(userId, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.NICKNAME_ALREADY_TAKEN);
                });

        verify(profileRepository).existsByNicknameWithLock(duplicateNickname);
        verify(profileRepository, never()).save(any(Profile.class));
        verify(jwtTokenProvider, never()).validateVerificationToken(anyString(), anyString());
    }

    @Test
    @DisplayName("전화번호 중복 시 DUPLICATE_PHONE_NUMBER 예외 발생")
    void 전화번호중복_예외발생() {
        // given
        Long userId = 1L;
        String duplicatePhoneNumber = "01012345678";
        ProfileSetupRequest request = new ProfileSetupRequest(
                "테스트닉네임",
                "M",
                duplicatePhoneNumber,
                "2000-01-01",
                "valid-token"
        );

        User user = User.builder()
                .email("test@example.com")
                .name("테스트사용자")
                .oauthId("12345")
                .oauthType(OauthType.KAKAO)
                .build();

        Profile profile = Profile.builder()
                .user(user)
                .build();

        when(userRepository.getByIdOrThrow(userId)).thenReturn(user);
        when(profileRepository.findByUser(user)).thenReturn(Optional.of(profile));
        when(profileRepository.existsByNicknameWithLock("테스트닉네임")).thenReturn(false);
        when(jwtTokenProvider.validateVerificationToken("valid-token", duplicatePhoneNumber)).thenReturn(true);
        when(profileRepository.existsByPhoneNumber(duplicatePhoneNumber)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> profileService.setupInitialProfile(userId, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_PHONE_NUMBER);
                });

        verify(profileRepository).existsByNicknameWithLock("테스트닉네임");
        verify(profileRepository).existsByPhoneNumber(duplicatePhoneNumber);
        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    @DisplayName("인증 토큰 검증 실패 시 INVALID_TOKEN 예외 발생")
    void 인증토큰검증실패_예외발생() {
        // given
        Long userId = 1L;
        String invalidToken = "invalid-token";
        String phoneNumber = "01012345678";
        ProfileSetupRequest request = new ProfileSetupRequest(
                "테스트닉네임",
                "M",
                phoneNumber,
                "2000-01-01",
                invalidToken
        );

        User user = User.builder()
                .email("test@example.com")
                .name("테스트사용자")
                .oauthId("12345")
                .oauthType(OauthType.KAKAO)
                .build();

        Profile profile = Profile.builder()
                .user(user)
                .build();

        when(userRepository.getByIdOrThrow(userId)).thenReturn(user);
        when(profileRepository.findByUser(user)).thenReturn(Optional.of(profile));
        when(profileRepository.existsByNicknameWithLock("테스트닉네임")).thenReturn(false);
        when(jwtTokenProvider.validateVerificationToken(invalidToken, phoneNumber)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> profileService.setupInitialProfile(userId, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
                });

        verify(profileRepository).existsByNicknameWithLock("테스트닉네임");
        verify(jwtTokenProvider).validateVerificationToken(invalidToken, phoneNumber);
        verify(profileRepository, never()).existsByPhoneNumber(anyString());
        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    @DisplayName("프로필이 없는 경우 PROFILE_NOT_FOUND 예외 발생")
    void 프로필없음_예외발생() {
        // given
        Long userId = 1L;
        ProfileSetupRequest request = new ProfileSetupRequest(
                "테스트닉네임",
                "M",
                "01012345678",
                "2000-01-01",
                "valid-token"
        );

        User user = User.builder()
                .email("test@example.com")
                .name("테스트사용자")
                .oauthId("12345")
                .oauthType(OauthType.KAKAO)
                .build();

        when(userRepository.getByIdOrThrow(userId)).thenReturn(user);
        when(profileRepository.findByUser(user)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> profileService.setupInitialProfile(userId, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.PROFILE_NOT_FOUND);
                });

        verify(profileRepository).findByUser(user);
        verify(profileRepository, never()).existsByNicknameWithLock(anyString());
        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    @DisplayName("비관적 잠금을 사용하여 닉네임 중복 체크 메서드 호출 확인")
    void 비관적잠금사용확인() {
        // given
        Long userId = 1L;
        String nickname = "테스트닉네임";
        ProfileSetupRequest request = new ProfileSetupRequest(
                nickname,
                "F",
                "01087654321",
                "1995-05-15",
                "valid-token"
        );

        User user = User.builder()
                .email("test2@example.com")
                .name("테스트사용자2")
                .oauthId("67890")
                .oauthType(OauthType.GOOGLE)
                .build();

        Profile profile = Profile.builder()
                .user(user)
                .build();

        when(userRepository.getByIdOrThrow(userId)).thenReturn(user);
        when(profileRepository.findByUser(user)).thenReturn(Optional.of(profile));
        when(profileRepository.existsByNicknameWithLock(nickname)).thenReturn(false);
        when(jwtTokenProvider.validateVerificationToken("valid-token", "01087654321")).thenReturn(true);
        when(profileRepository.existsByPhoneNumber("01087654321")).thenReturn(false);
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        // when
        profileService.setupInitialProfile(userId, request);

        // then
        // existsByNickname이 아닌 existsByNicknameWithLock이 호출되었는지 확인
        verify(profileRepository).existsByNicknameWithLock(nickname);
        verify(profileRepository, never()).existsByNickname(nickname);
    }

    // ==================== updateProfile 단위 테스트 ====================

    @Test
    @DisplayName("updateProfile: UPLOADED 타입일 때 이미지 URL·배경색·bio가 반영되고 저장된다")
    void updateProfile_UPLOADED_성공() {
        // given
        Long userId = 1L;
        String imageUrl = "https://s3.example.com/profile/abc.png";
        Integer bgColorId = 3;
        String bio = "안녕하세요 여행 좋아해요";
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                imageUrl,
                "UPLOADED",
                bgColorId,
                bio
        );

        User user = User.builder()
                .email("test@example.com")
                .name("테스트")
                .oauthId("oauth-1")
                .oauthType(OauthType.KAKAO)
                .build();

        Profile profile = Profile.builder()
                .user(user)
                .nickname("닉네임")
                .build();

        when(userRepository.getByIdOrThrow(userId)).thenReturn(user);
        when(profileRepository.findByUser(user)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        // when
        profileService.updateProfile(userId, request);

        // then
        verify(profileRepository).findByUser(user);
        verify(profileRepository).save(profile);
        assertThat(profile.getUploadedImageUrl()).isEqualTo(imageUrl);
        assertThat(profile.getProfileImageType()).isEqualTo(ProfileImageType.UPLOADED);
        assertThat(profile.getBgColorId()).isEqualTo(bgColorId);
        assertThat(profile.getBio()).isEqualTo(bio);
    }

    @Test
    @DisplayName("updateProfile: AVATAR 타입일 때 uploadedImageUrl은 null, 배경색·bio는 반영된다")
    void updateProfile_AVATAR_성공() {
        // given
        Long userId = 1L;
        Integer bgColorId = 5;
        String bio = "아바타로 할게요";
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                null,
                "AVATAR",
                bgColorId,
                bio
        );

        User user = User.builder()
                .email("test@example.com")
                .name("테스트")
                .oauthId("oauth-1")
                .oauthType(OauthType.KAKAO)
                .build();

        Profile profile = Profile.builder()
                .user(user)
                .nickname("닉네임")
                .build();

        when(userRepository.getByIdOrThrow(userId)).thenReturn(user);
        when(profileRepository.findByUser(user)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        // when
        profileService.updateProfile(userId, request);

        // then
        verify(profileRepository).save(profile);
        assertThat(profile.getUploadedImageUrl()).isNull();
        assertThat(profile.getProfileImageType()).isEqualTo(ProfileImageType.AVATAR);
        assertThat(profile.getBgColorId()).isEqualTo(bgColorId);
        assertThat(profile.getBio()).isEqualTo(bio);
    }

    @Test
    @DisplayName("updateProfile: DEFAULT 타입은 AVATAR와 동일하게 처리된다")
    void updateProfile_DEFAULT_AVATAR와동일처리() {
        // given
        Long userId = 1L;
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                null,
                "DEFAULT",
                1,
                "기본"
        );

        User user = User.builder()
                .email("test@example.com")
                .name("테스트")
                .oauthId("oauth-1")
                .oauthType(OauthType.KAKAO)
                .build();

        Profile profile = Profile.builder()
                .user(user)
                .nickname("닉네임")
                .build();

        when(userRepository.getByIdOrThrow(userId)).thenReturn(user);
        when(profileRepository.findByUser(user)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        // when
        profileService.updateProfile(userId, request);

        // then
        verify(profileRepository).save(profile);
        assertThat(profile.getUploadedImageUrl()).isNull();
        assertThat(profile.getProfileImageType()).isEqualTo(ProfileImageType.AVATAR);
        assertThat(profile.getBgColorId()).isEqualTo(1);
        assertThat(profile.getBio()).isEqualTo("기본");
    }

    @Test
    @DisplayName("updateProfile: 프로필이 없으면 PROFILE_NOT_FOUND 예외 발생")
    void updateProfile_프로필없음_예외발생() {
        // given
        Long userId = 1L;
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                null,
                "AVATAR",
                1,
                "bio"
        );

        User user = User.builder()
                .email("test@example.com")
                .name("테스트")
                .oauthId("oauth-1")
                .oauthType(OauthType.KAKAO)
                .build();

        when(userRepository.getByIdOrThrow(userId)).thenReturn(user);
        when(profileRepository.findByUser(user)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> profileService.updateProfile(userId, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException e = (BusinessException) ex;
                    assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PROFILE_NOT_FOUND);
                });

        verify(profileRepository).findByUser(user);
        verify(profileRepository, never()).save(any(Profile.class));
    }

    // ==================== updateAvatarId 단위 테스트 ====================

    @Test
    @DisplayName("updateAvatarId: 아바타 ID가 정상 반영되고 저장된다")
    void updateAvatarId_성공() {
        // given
        Long userId = 1L;
        Long avatarId = 10L;

        User user = User.builder()
                .email("test@example.com")
                .name("테스트")
                .oauthId("oauth-1")
                .oauthType(OauthType.KAKAO)
                .build();

        Profile profile = Profile.builder()
                .user(user)
                .nickname("닉네임")
                .build();

        when(userRepository.getByIdOrThrow(userId)).thenReturn(user);
        when(profileRepository.findByUser(user)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        // when
        profileService.updateAvatarId(userId, avatarId);

        // then
        verify(profileRepository).save(profile);
        assertThat(profile.getAvatarId()).isEqualTo(avatarId);
    }

    @Test
    @DisplayName("updateAvatarId: 프로필이 없으면 PROFILE_NOT_FOUND 예외 발생")
    void updateAvatarId_프로필없음_예외발생() {
        // given
        Long userId = 1L;
        Long avatarId = 10L;

        User user = User.builder()
                .email("test@example.com")
                .name("테스트")
                .oauthId("oauth-1")
                .oauthType(OauthType.KAKAO)
                .build();

        when(userRepository.getByIdOrThrow(userId)).thenReturn(user);
        when(profileRepository.findByUser(user)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> profileService.updateAvatarId(userId, avatarId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException e = (BusinessException) ex;
                    assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PROFILE_NOT_FOUND);
                });

        verify(profileRepository).findByUser(user);
        verify(profileRepository, never()).save(any(Profile.class));
    }
}
