package com.dduru.gildongmu.user.service;

import com.dduru.gildongmu.user.dto.NicknameRandomResponse;
import com.dduru.gildongmu.user.repository.UserRepository;
import com.dduru.gildongmu.user.utils.NicknameGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private NicknameGenerator nicknameGenerator;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @DisplayName("중복이 없는 경우 첫 시도에 랜덤 닉네임을 반환한다")
    @Test
    void randomNickname_firstTrySuccess() {
        // given
        String baseNickname = "용감한 여행자";
        int randomNumber = 1234;
        String expectedNickname = baseNickname + randomNumber;

        when(nicknameGenerator.generateBaseNickname()).thenReturn(baseNickname);
        when(nicknameGenerator.generateRandomNumber()).thenReturn(randomNumber);
        when(userRepository.existsByNickname(expectedNickname)).thenReturn(false);

        // when
        NicknameRandomResponse response = userService.generateRandomNickname();

        // then
        assertThat(response.nickname()).isEqualTo(expectedNickname);
        verify(nicknameGenerator, times(1)).generateBaseNickname();
        verify(nicknameGenerator, times(1)).generateRandomNumber();
        verify(userRepository, times(1)).existsByNickname(eq(expectedNickname));
    }

    @DisplayName("중복이 발생하면 재시도하여 다음 닉네임을 반환한다")
    @Test
    void randomNickname_retriesOnDuplicate() {
        // given
        String baseNickname = "자유로운 모험가";
        int firstNumber = 1111;
        int secondNumber = 2222;
        String firstNickname = baseNickname + firstNumber;
        String secondNickname = baseNickname + secondNumber;

        when(nicknameGenerator.generateBaseNickname()).thenReturn(baseNickname);
        when(nicknameGenerator.generateRandomNumber()).thenReturn(firstNumber, secondNumber);
        when(userRepository.existsByNickname(firstNickname)).thenReturn(true);
        when(userRepository.existsByNickname(secondNickname)).thenReturn(false);

        // when
        NicknameRandomResponse response = userService.generateRandomNickname();

        // then
        assertThat(response.nickname()).isEqualTo(secondNickname);
        verify(userRepository, times(1)).existsByNickname(eq(firstNickname));
        verify(userRepository, times(1)).existsByNickname(eq(secondNickname));
        verify(nicknameGenerator, times(2)).generateRandomNumber();
    }

    @DisplayName("최대 재시도 후에도 중복이면 대체 닉네임(뚜비####)을 반환한다")
    @Test
    void randomNickname_returnsFallbackAfterMaxAttempts() {
        // given
        String baseNickname = "천천히가는 여행자";
        int duplicatedNumber = 7777;
        String duplicatedNickname = baseNickname + duplicatedNumber;

        when(nicknameGenerator.generateBaseNickname()).thenReturn(baseNickname);
        when(nicknameGenerator.generateRandomNumber()).thenReturn(duplicatedNumber);
        when(userRepository.existsByNickname(duplicatedNickname)).thenReturn(true);

        // when
        NicknameRandomResponse response = userService.generateRandomNickname();

        // then
        assertThat(response.nickname()).startsWith("뚜비");
        verify(nicknameGenerator, times(10)).generateBaseNickname();
        verify(nicknameGenerator, times(10)).generateRandomNumber();
        verify(userRepository, times(10)).existsByNickname(eq(duplicatedNickname));
    }
}

