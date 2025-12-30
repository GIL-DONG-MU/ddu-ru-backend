package com.dduru.gildongmu.profile.service;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.common.jwt.JwtTokenProvider;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.dto.ProfileSetupRequest;
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
}
