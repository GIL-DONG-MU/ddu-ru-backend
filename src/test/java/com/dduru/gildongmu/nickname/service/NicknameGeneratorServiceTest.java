package com.dduru.gildongmu.nickname.service;

import com.dduru.gildongmu.nickname.generator.AdjectiveProvider;
import com.dduru.gildongmu.nickname.generator.NounProvider;
import com.dduru.gildongmu.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("NicknameGeneratorService 테스트")
class NicknameGeneratorServiceTest {

    @InjectMocks
    private NicknameGeneratorService nicknameGeneratorService;

    @Mock
    private UserRepository userRepository;

    @Spy
    private AdjectiveProvider adjectiveProvider = new AdjectiveProvider();

    @Spy
    private NounProvider nounProvider = new NounProvider();

    @BeforeEach
    void setUp() {
        lenient().when(userRepository.existsByNickname(anyString())).thenReturn(false);
    }

    @Test
    @DisplayName("유니크한 닉네임을 생성한다")
    void generateUniqueNickname_returnsUniqueNickname() {
        // when
        String nickname = nicknameGeneratorService.generateUniqueNickname();

        // then
        assertThat(nickname).isNotBlank();
        assertThat(nickname).contains(" "); // 형용사 + 공백 + 명사 형식
        assertThat(nickname.length()).isLessThanOrEqualTo(12);
    }

    @Test
    @DisplayName("닉네임이 이미 사용 중이면 다른 닉네임을 생성한다")
    void generateUniqueNickname_whenNicknameExists_generatesAlternative() {
        // given - 처음 몇 번은 중복, 그 후 사용 가능
        given(userRepository.existsByNickname(anyString()))
                .willReturn(true)
                .willReturn(true)
                .willReturn(false);

        // when
        String nickname = nicknameGeneratorService.generateUniqueNickname();

        // then
        assertThat(nickname).isNotBlank();
    }

    @Test
    @DisplayName("생성된 닉네임은 12자를 초과하지 않는다")
    void generateUniqueNickname_withinMaxLength() {
        // when - 여러 번 호출해서 확인
        for (int i = 0; i < 50; i++) {
            String nickname = nicknameGeneratorService.generateUniqueNickname();

            // then
            assertThat(nickname.length()).isLessThanOrEqualTo(12);
        }
    }
}
