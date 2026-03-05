package com.dduru.gildongmu.profile.service;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.common.jwt.JwtTokenProvider;
import com.dduru.gildongmu.onboarding.service.OnboardingService;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.dto.request.ProfileSetupRequest;
import com.dduru.gildongmu.profile.repository.ProfileRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("프로필 온보딩 서비스 테스트")
class ProfileOnboardingServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private OnboardingService onboardingService;

    @InjectMocks
    private ProfileOnboardingService profileOnboardingService;

    private User testUser;
    private Profile profile;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .name("테스트")
                .oauthId("12345")
                .oauthType(OauthType.KAKAO)
                .build();
        profile = new Profile(testUser);
    }

    @Test
    @DisplayName("프로필 초기 설정 성공 시 온보딩 완료 처리 및 닉네임 미저장 확인")
    void setupInitialProfile_성공_처리() {
        // given
        ProfileSetupRequest request = new ProfileSetupRequest(
                Gender.M,
                "01012345678",
                "2000-01-01",
                "valid-token"
        );

        when(profileRepository.getByUserIdOrThrow(1L)).thenReturn(profile);
        when(jwtTokenProvider.validateVerificationToken(request.verificationToken(), request.phoneNumber())).thenReturn(true);
        when(profileRepository.existsByPhoneNumber(request.phoneNumber())).thenReturn(false);
        when(profileRepository.save(profile)).thenReturn(profile);

        // when
        profileOnboardingService.setupInitialProfile(1L, request);

        // then
        assertThat(profile.getNickname()).isNull();
        assertThat(profile.getGender()).isEqualTo(Gender.M);
        assertThat(profile.getPhoneNumber()).isEqualTo("01012345678");
        assertThat(profile.getBirthday()).isEqualTo(LocalDate.of(2000, 1, 1));

        verify(jwtTokenProvider).validateVerificationToken("valid-token", "01012345678");
        verify(profileRepository).existsByPhoneNumber("01012345678");
        verify(profileRepository).save(profile);
        verify(onboardingService).completeOnboarding(1L);
        verify(profileRepository, never()).existsByNicknameWithLock(any());
    }

    @Test
    @DisplayName("전화번호 중복 시 예외 발생")
    void setupInitialProfile_중복전화번호_예외발생() {
        // given
        ProfileSetupRequest request = new ProfileSetupRequest(
                Gender.F,
                "01087654321",
                "1999-05-05",
                "valid-token"
        );

        when(profileRepository.getByUserIdOrThrow(1L)).thenReturn(profile);
        when(jwtTokenProvider.validateVerificationToken(request.verificationToken(), request.phoneNumber())).thenReturn(true);
        when(profileRepository.existsByPhoneNumber(request.phoneNumber())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> profileOnboardingService.setupInitialProfile(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_PHONE_NUMBER);

        verify(profileRepository, never()).save(profile);
        verify(onboardingService, never()).completeOnboarding(anyLong());
        verify(profileRepository, never()).existsByNicknameWithLock(any());
    }

    @Test
    @DisplayName("인증 토큰이 유효하지 않으면 예외 발생")
    void setupInitialProfile_유효하지않은토큰_예외발생() {
        // given
        ProfileSetupRequest request = new ProfileSetupRequest(
                Gender.M,
                "01099999999",
                "1988-12-31",
                "invalid-token"
        );

        when(profileRepository.getByUserIdOrThrow(1L)).thenReturn(profile);
        when(jwtTokenProvider.validateVerificationToken(request.verificationToken(), request.phoneNumber())).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> profileOnboardingService.setupInitialProfile(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_TOKEN);

        verify(profileRepository, never()).existsByPhoneNumber(any());
        verify(profileRepository, never()).save(profile);
        verify(onboardingService, never()).completeOnboarding(anyLong());
    }

    @Test
    @DisplayName("생년월일 형식이 잘못되면 INVALID_INPUT_VALUE 예외 발생")
    void setupInitialProfile_잘못된생일_예외발생() {
        // given
        ProfileSetupRequest request = new ProfileSetupRequest(
                Gender.M,
                "01012341234",
                "20000101",
                "valid-token"
        );

        when(profileRepository.getByUserIdOrThrow(1L)).thenReturn(profile);
        when(jwtTokenProvider.validateVerificationToken(request.verificationToken(), request.phoneNumber())).thenReturn(true);
        when(profileRepository.existsByPhoneNumber(request.phoneNumber())).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> profileOnboardingService.setupInitialProfile(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

        verify(profileRepository, never()).save(profile);
        verify(onboardingService, never()).completeOnboarding(anyLong());
    }
}
