package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.domain.User;
import com.dduru.gildongmu.auth.enums.AgeRange;
import com.dduru.gildongmu.auth.enums.Gender;
import com.dduru.gildongmu.auth.enums.OauthType;
import com.dduru.gildongmu.auth.exception.UserNotFoundException;
import com.dduru.gildongmu.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void 존재하는_사용자의_닉네임을_업데이트할_수_있다() {
        // given
        Long userId = 1L;
        String newNickname = "새로운닉네임";
        
        User user = User.builder()
                .email("test@example.com")
                .name("테스트사용자")
                .nickname("기존닉네임")
                .profileImage("http://example.com/profile.jpg")
                .oauthId("12345")
                .oauthType(OauthType.KAKAO)
                .gender(Gender.M)
                .ageRange(AgeRange.AGE_20s)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        userService.updateNickname(userId, newNickname);

        // then
        assertThat(user.getNickname()).isEqualTo(newNickname);
    }

    @Test
    void 존재하지_않는_사용자의_닉네임_업데이트_시_예외가_발생한다() {
        // given
        Long userId = 999L;
        String newNickname = "새로운닉네임";
        
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateNickname(userId, newNickname))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

    @Test
    void 사용자_조회가_가능하다() {
        // given
        Long userId = 1L;
        
        User user = User.builder()
                .email("test@example.com")
                .name("테스트사용자")
                .profileImage("http://example.com/profile.jpg")
                .oauthId("12345")
                .oauthType(OauthType.KAKAO)
                .gender(Gender.M)
                .ageRange(AgeRange.AGE_20s)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        User foundUser = userService.findById(userId);

        // then
        assertThat(foundUser).isEqualTo(user);
    }

    @Test
    void 존재하지_않는_사용자_조회_시_예외가_발생한다() {
        // given
        Long userId = 999L;
        
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }
}