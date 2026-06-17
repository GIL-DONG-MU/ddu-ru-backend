package com.dduru.gildongmu.profile.service;

import com.dduru.gildongmu.chat.event.ProfileUpdatedEvent;
import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.onboarding.service.OnboardingService;
import com.dduru.gildongmu.profile.domain.BgColor;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.dto.request.ProfileUpdateRequest;
import com.dduru.gildongmu.profile.exception.InvalidProfileImageUrlException;
import com.dduru.gildongmu.profile.repository.BgColorRepository;
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
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("프로필 관리 서비스 테스트")
class ProfileManagementServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private BgColorRepository bgColorRepository;

    @Mock
    private OnboardingService onboardingService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProfileManagementService profileManagementService;

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
    @DisplayName("프로필 수정 시 닉네임, 이미지, 배경색, 소개글이 함께 반영된다")
    void updateProfile_성공() {
        // given
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "뉴닉네임",
                "https://example.com/profile.png",
                ProfileImageType.UPLOADED,
                null,
                "안녕하세요"
        );

        when(profileRepository.getByUserIdOrThrow(1L)).thenReturn(profile);
        when(profileRepository.existsByNickname(request.nickname())).thenReturn(false);

        // when
        profileManagementService.updateProfile(1L, request);

        // then
        assertThat(profile.getNickname()).isEqualTo("뉴닉네임");
        assertThat(profile.getUploadedImageUrl()).isEqualTo("https://example.com/profile.png");
        assertThat(profile.getProfileImageType()).isEqualTo(ProfileImageType.UPLOADED);
        assertThat(profile.getBgColor()).isNull();
        assertThat(profile.getBio()).isEqualTo("안녕하세요");
        verify(profileRepository).existsByNickname("뉴닉네임");
        verify(bgColorRepository, never()).getByIdOrThrow(anyLong());
        verify(onboardingService).completeProfile(1L);
        verify(eventPublisher).publishEvent(new ProfileUpdatedEvent(1L));
    }

    @Test
    @DisplayName("닉네임이 변경되지 않은 경우 중복 검사를 건너뛰고 프로필 업데이트 처리")
    void updateProfile_닉네임변경없음_중복검사건너뜀() {
        // given
        profile.updateNickname("동일닉네임");
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "동일닉네임",
                "https://example.com/profile.png",
                ProfileImageType.UPLOADED,
                null,
                "안녕하세요"
        );

        when(profileRepository.getByUserIdOrThrow(1L)).thenReturn(profile);

        // when
        profileManagementService.updateProfile(1L, request);

        // then
        assertThat(profile.getNickname()).isEqualTo("동일닉네임");
        verify(profileRepository, never()).existsByNickname(any());
        verify(bgColorRepository, never()).getByIdOrThrow(anyLong());
        verify(onboardingService).completeProfile(1L);
    }

    @Test
    @DisplayName("중복 닉네임이면 예외 발생")
    void updateProfile_닉네임중복시_예외발생() {
        // given
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "중복닉네임",
                "https://example.com/profile.png",
                ProfileImageType.UPLOADED,
                null,
                "안녕하세요"
        );

        when(profileRepository.getByUserIdOrThrow(1L)).thenReturn(profile);
        when(profileRepository.existsByNickname(request.nickname())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> profileManagementService.updateProfile(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.NICKNAME_ALREADY_TAKEN);

        verify(profileRepository, times(1)).existsByNickname("중복닉네임");
        verify(bgColorRepository, never()).getByIdOrThrow(anyLong());
        verify(onboardingService, never()).completeProfile(anyLong());
    }

    @Test
    @DisplayName("AVATAR 타입이면 업로드 이미지가 빈 값으로 저장된다")
    void updateProfile_AVATAR_이미지초기화() {
        // given
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "아바타닉네임",
                null,
                ProfileImageType.AVATAR,
                1L,
                "안녕하세요"
        );

        BgColor bgColor = BgColor.builder().hexCode("#ffffff").displayOrder(2).build();
        when(profileRepository.getByUserIdOrThrow(1L)).thenReturn(profile);
        when(bgColorRepository.getByIdOrThrow(1L)).thenReturn(bgColor);
        when(profileRepository.existsByNickname(request.nickname())).thenReturn(false);

        // when
        profileManagementService.updateProfile(1L, request);

        // then
        assertThat(profile.getUploadedImageUrl()).isEmpty();
        assertThat(profile.getProfileImageType()).isEqualTo(ProfileImageType.AVATAR);
        assertThat(profile.getBgColor()).isSameAs(bgColor);
    }

    @Test
    @DisplayName("UPLOADED 타입에서 이미지 URL이 없으면 예외 발생")
    void updateProfile_이미지없는_업로드타입_예외발생() {
        // given
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "유효하지않은",
                null,
                ProfileImageType.UPLOADED,
                null,
                "안녕하세요"
        );

        when(profileRepository.getByUserIdOrThrow(1L)).thenReturn(profile);

        // when & then
        assertThatThrownBy(() -> profileManagementService.updateProfile(1L, request))
                .isInstanceOf(InvalidProfileImageUrlException.class);

        verify(bgColorRepository, never()).getByIdOrThrow(anyLong());
        verify(onboardingService, never()).completeProfile(anyLong());
    }

    @Test
    @DisplayName("DEFAULT 타입으로 변경하면 업로드 이미지 URL은 저장되지 않는다")
    void updateProfile_DEFAULT_업로드이미지미저장() {
        // given
        profile.updateProfile(
                "기존닉네임",
                "https://example.com/uploaded.png",
                ProfileImageType.UPLOADED,
                null,
                "이전 소개글"
        );

        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "기본닉네임",
                null,
                ProfileImageType.DEFAULT,
                null,
                "안녕하세요"
        );

        when(profileRepository.getByUserIdOrThrow(1L)).thenReturn(profile);
        when(profileRepository.existsByNickname(request.nickname())).thenReturn(false);

        // when
        profileManagementService.updateProfile(1L, request);

        // then
        assertThat(profile.getNickname()).isEqualTo("기본닉네임");
        assertThat(profile.getUploadedImageUrl()).isNull();
        assertThat(profile.getProfileImageType()).isEqualTo(ProfileImageType.DEFAULT);
        assertThat(profile.getBgColor()).isNull();
        verify(bgColorRepository, never()).getByIdOrThrow(anyLong());
        verify(onboardingService).completeProfile(1L);
    }
}
