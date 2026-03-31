package com.dduru.gildongmu.onboarding.service;

import com.dduru.gildongmu.onboarding.domain.UserOnboarding;
import com.dduru.gildongmu.onboarding.domain.enums.SurveyStatus;
import com.dduru.gildongmu.onboarding.dto.response.OnboardingStatusResponse;
import com.dduru.gildongmu.onboarding.exception.UserOnboardingNotFoundException;
import com.dduru.gildongmu.onboarding.repository.UserOnboardingRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("온보딩 서비스 테스트")
class OnboardingServiceTest {

    @Mock
    private UserOnboardingRepository userOnboardingRepository;

    @InjectMocks
    private OnboardingService onboardingService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .name("테스트")
                .oauthId("12345")
                .oauthType(OauthType.KAKAO)
                .build();
    }

    @Test
    @DisplayName("온보딩 상태 조회 - 초기 상태")
    void getStatus_초기상태_반환() {
        // given
        UserOnboarding userOnboarding = new UserOnboarding(testUser);
        when(userOnboardingRepository.getByUserIdOrThrow(1L)).thenReturn(userOnboarding);

        // when
        OnboardingStatusResponse response = onboardingService.getStatus(1L);

        // then
        assertThat(response.isOnboardingCompleted()).isFalse();
        assertThat(response.isProfileCompleted()).isFalse();
        assertThat(response.surveyStatus()).isEqualTo(SurveyStatus.NOT_STARTED);
    }

    @Test
    @DisplayName("온보딩 상태 조회 - 모든 단계 완료 상태")
    void getStatus_완료상태_반환() {
        // given
        UserOnboarding userOnboarding = new UserOnboarding(testUser);
        userOnboarding.completeOnboarding();
        userOnboarding.completeProfile();
        userOnboarding.completeSurvey();
        when(userOnboardingRepository.getByUserIdOrThrow(1L)).thenReturn(userOnboarding);

        // when
        OnboardingStatusResponse response = onboardingService.getStatus(1L);

        // then
        assertThat(response.isOnboardingCompleted()).isTrue();
        assertThat(response.isProfileCompleted()).isTrue();
        assertThat(response.surveyStatus()).isEqualTo(SurveyStatus.COMPLETED);
    }

    @Test
    @DisplayName("온보딩 상태 조회 - 설문 스킵 상태")
    void getStatus_설문스킵상태_반환() {
        // given
        UserOnboarding userOnboarding = new UserOnboarding(testUser);
        userOnboarding.skipSurvey();
        when(userOnboardingRepository.getByUserIdOrThrow(1L)).thenReturn(userOnboarding);

        // when
        OnboardingStatusResponse response = onboardingService.getStatus(1L);

        // then
        assertThat(response.surveyStatus()).isEqualTo(SurveyStatus.SKIPPED);
    }

    @Test
    @DisplayName("온보딩 상태 조회 - 온보딩 레코드 없으면 예외 발생")
    void getStatus_온보딩레코드없음_예외발생() {
        // given
        Long userId = 999L;
        when(userOnboardingRepository.getByUserIdOrThrow(userId)).thenThrow(new UserOnboardingNotFoundException());

        // when & then
        assertThatThrownBy(() -> onboardingService.getStatus(userId))
                .isInstanceOf(UserOnboardingNotFoundException.class)
                .hasMessage("유저 온보딩 정보를 찾을 수 없습니다.");

        verify(userOnboardingRepository).getByUserIdOrThrow(userId);
    }

    @Test
    @DisplayName("온보딩 상태 조회 - 조회 요청한 사용자 ID가 레포지토리에 전달됨")
    void getStatus_사용자id_정확히조회() {
        // given
        Long userId = 11L;
        UserOnboarding userOnboarding = new UserOnboarding(testUser);
        when(userOnboardingRepository.getByUserIdOrThrow(userId)).thenReturn(userOnboarding);

        // when
        onboardingService.getStatus(userId);

        // then
        verify(userOnboardingRepository).getByUserIdOrThrow(userId);
    }

    @Test
    @DisplayName("설문 스킵 - surveyStatus가 SKIPPED로 변경됨")
    void skipSurvey_성공() {
        // given
        UserOnboarding userOnboarding = new UserOnboarding(testUser);
        when(userOnboardingRepository.getByUserIdOrThrow(1L)).thenReturn(userOnboarding);

        // when
        onboardingService.skipSurvey(1L);

        // then
        assertThat(userOnboarding.getSurveyStatus()).isEqualTo(SurveyStatus.SKIPPED);
        verify(userOnboardingRepository).getByUserIdOrThrow(1L);
    }

    @Test
    @DisplayName("설문 스킵 - 이미 SKIPPED여도 안전하게 처리됨")
    void skipSurvey_이미스킵된_상태에서_재호출_안정적으로_처리() {
        // given
        UserOnboarding userOnboarding = new UserOnboarding(testUser);
        userOnboarding.skipSurvey();
        when(userOnboardingRepository.getByUserIdOrThrow(1L)).thenReturn(userOnboarding);

        // when
        onboardingService.skipSurvey(1L);

        // then
        assertThat(userOnboarding.getSurveyStatus()).isEqualTo(SurveyStatus.SKIPPED);
        verify(userOnboardingRepository).getByUserIdOrThrow(1L);
    }

    @Test
    @DisplayName("설문 스킵 - 온보딩 레코드 없으면 예외 발생")
    void skipSurvey_온보딩레코드없음_예외발생() {
        // given
        Long userId = 999L;
        when(userOnboardingRepository.getByUserIdOrThrow(userId)).thenThrow(new UserOnboardingNotFoundException());

        // when & then
        assertThatThrownBy(() -> onboardingService.skipSurvey(userId))
                .isInstanceOf(UserOnboardingNotFoundException.class)
                .hasMessage("유저 온보딩 정보를 찾을 수 없습니다.");

        verify(userOnboardingRepository).getByUserIdOrThrow(userId);
    }
}
