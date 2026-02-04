package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.dto.UserCreationResult;
import com.dduru.gildongmu.auth.exception.DuplicateEmailException;
import com.dduru.gildongmu.auth.exception.UserNotFoundException;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import com.dduru.gildongmu.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDomainService 테스트")
class UserDomainServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCreationService userCreationService;

    @InjectMocks
    private UserDomainService userDomainService;

    @Test
    @DisplayName("userId로 사용자 조회 성공")
    void getUserOrThrow_성공() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .email("test@example.com")
                .name("테스트")
                .oauthId("oauth-123")
                .oauthType(OauthType.KAKAO)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        User result = userDomainService.getUserOrThrow(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("userId로 사용자 조회 시 없으면 UserNotFoundException 발생")
    void getUserOrThrow_사용자없음_예외발생() {
        // given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userDomainService.getUserOrThrow(userId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("기존 OAuth 사용자 조회 시 UserCreationResult 반환 (isNewUser false)")
    void findOrCreateUser_기존사용자_성공() {
        // given
        OauthUserInfo oauthUserInfo = OauthUserInfo.builder()
                .oauthId("oauth-123")
                .email("existing@example.com")
                .name("기존사용자")
                .loginType(OauthType.KAKAO)
                .build();

        User existingUser = User.builder()
                .email("existing@example.com")
                .name("기존사용자")
                .oauthId("oauth-123")
                .oauthType(OauthType.KAKAO)
                .build();
        ReflectionTestUtils.setField(existingUser, "id", 1L);

        when(userRepository.findByOauthIdAndOauthType("oauth-123", OauthType.KAKAO))
                .thenReturn(Optional.of(existingUser));

        // when
        UserCreationResult result = userDomainService.findOrCreateUser(oauthUserInfo);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isNewUser()).isFalse();
        assertThat(result.user().getId()).isEqualTo(1L);
        assertThat(result.user().getEmail()).isEqualTo("existing@example.com");
        verify(userRepository).findByOauthIdAndOauthType("oauth-123", OauthType.KAKAO);
        verify(userCreationService, never()).createNewUser(any(OauthUserInfo.class));
    }

    @Test
    @DisplayName("신규 OAuth 사용자 생성 성공 (isNewUser true)")
    void findOrCreateUser_신규사용자_생성성공() {
        // given
        OauthUserInfo oauthUserInfo = OauthUserInfo.builder()
                .oauthId("new-oauth-456")
                .email("new@example.com")
                .name("신규사용자")
                .loginType(OauthType.GOOGLE)
                .build();

        User savedUser = User.builder()
                .email("new@example.com")
                .name("신규사용자")
                .oauthId("new-oauth-456")
                .oauthType(OauthType.GOOGLE)
                .build();
        ReflectionTestUtils.setField(savedUser, "id", 2L);

        UserCreationResult creationResult = new UserCreationResult(savedUser, true);

        when(userRepository.findByOauthIdAndOauthType("new-oauth-456", OauthType.GOOGLE))
                .thenReturn(Optional.empty());
        when(userCreationService.createNewUser(oauthUserInfo)).thenReturn(creationResult);

        // when
        UserCreationResult result = userDomainService.findOrCreateUser(oauthUserInfo);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isNewUser()).isTrue();
        assertThat(result.user().getId()).isEqualTo(2L);
        assertThat(result.user().getEmail()).isEqualTo("new@example.com");
        verify(userRepository).findByOauthIdAndOauthType("new-oauth-456", OauthType.GOOGLE);
        verify(userCreationService).createNewUser(oauthUserInfo);
    }

    @Test
    @DisplayName("이미 가입된 이메일로 다른 OAuth 가입 시 DuplicateEmailException 발생")
    void findOrCreateUser_이메일중복_예외발생() {
        // given
        OauthUserInfo oauthUserInfo = OauthUserInfo.builder()
                .oauthId("other-oauth-789")
                .email("duplicate@example.com")
                .name("다른제공자")
                .loginType(OauthType.KAKAO)
                .build();

        when(userRepository.findByOauthIdAndOauthType("other-oauth-789", OauthType.KAKAO))
                .thenReturn(Optional.empty());
        when(userCreationService.createNewUser(oauthUserInfo))
                .thenThrow(DuplicateEmailException.of("duplicate@example.com"));

        // when & then
        assertThatThrownBy(() -> userDomainService.findOrCreateUser(oauthUserInfo))
                .isInstanceOf(DuplicateEmailException.class);

        verify(userRepository).findByOauthIdAndOauthType("other-oauth-789", OauthType.KAKAO);
        verify(userCreationService).createNewUser(oauthUserInfo);
        verify(userCreationService, never()).findExistingUserOrThrow(any(OauthUserInfo.class));
    }
}
