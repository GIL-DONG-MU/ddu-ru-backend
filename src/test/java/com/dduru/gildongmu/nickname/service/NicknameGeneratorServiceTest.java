package com.dduru.gildongmu.nickname.service;

import com.dduru.gildongmu.nickname.dto.NicknameGenerateRequest;
import com.dduru.gildongmu.nickname.dto.NicknameGenerateResponse;
import com.dduru.gildongmu.nickname.enums.NicknameTheme;
import com.dduru.gildongmu.nickname.generator.AdjectiveProvider;
import com.dduru.gildongmu.nickname.generator.NounProvider;
import com.dduru.gildongmu.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
        // lenient를 사용하여 불필요한 stubbing 경고 방지
        lenient().when(userRepository.existsByNickname(anyString())).thenReturn(false);
    }

    @Nested
    @DisplayName("generateNicknames 메서드 테스트")
    class GenerateNicknamesTest {

        @Test
        @DisplayName("기본 요청으로 5개의 닉네임을 생성한다")
        void generateNicknames_withDefaultRequest_returnsFiveNicknames() {
            // given
            NicknameGenerateRequest request = NicknameGenerateRequest.defaultRequest();

            // when
            NicknameGenerateResponse response = nicknameGeneratorService.generateNicknames(request);

            // then
            assertThat(response.nicknames()).hasSize(5);
            assertThat(response.count()).isEqualTo(5);
        }

        @Test
        @DisplayName("요청한 개수만큼 닉네임을 생성한다")
        void generateNicknames_withSpecificCount_returnsRequestedCount() {
            // given
            NicknameGenerateRequest request = new NicknameGenerateRequest(3, NicknameTheme.RANDOM, false);

            // when
            NicknameGenerateResponse response = nicknameGeneratorService.generateNicknames(request);

            // then
            assertThat(response.nicknames()).hasSize(3);
            assertThat(response.count()).isEqualTo(3);
        }

        @ParameterizedTest
        @EnumSource(value = NicknameTheme.class, names = {"TRAVEL", "NATURE", "ANIMAL", "FOOD", "SPACE", "FANTASY"})
        @DisplayName("각 테마별로 닉네임을 생성한다")
        void generateNicknames_withEachTheme_returnsNicknames(NicknameTheme theme) {
            // given
            NicknameGenerateRequest request = new NicknameGenerateRequest(3, theme, false);

            // when
            NicknameGenerateResponse response = nicknameGeneratorService.generateNicknames(request);

            // then
            assertThat(response.nicknames()).isNotEmpty();
            assertThat(response.nicknames()).allSatisfy(nickname -> {
                assertThat(nickname).isNotBlank();
                assertThat(nickname.length()).isLessThanOrEqualTo(12);
            });
        }

        @Test
        @DisplayName("숫자 접미사를 포함한 닉네임을 생성한다")
        void generateNicknames_withNumberSuffix_includesNumber() {
            // given
            NicknameGenerateRequest request = new NicknameGenerateRequest(5, NicknameTheme.RANDOM, true);

            // when
            NicknameGenerateResponse response = nicknameGeneratorService.generateNicknames(request);

            // then
            assertThat(response.nicknames()).isNotEmpty();
            // 숫자가 포함된 닉네임이 있어야 함
            assertThat(response.nicknames()).anyMatch(nickname -> 
                nickname.matches(".*\\d+$")
            );
        }

        @Test
        @DisplayName("생성된 닉네임은 중복되지 않는다")
        void generateNicknames_returnsUniqueNicknames() {
            // given
            NicknameGenerateRequest request = new NicknameGenerateRequest(10, NicknameTheme.RANDOM, false);

            // when
            NicknameGenerateResponse response = nicknameGeneratorService.generateNicknames(request);

            // then
            assertThat(response.nicknames()).doesNotHaveDuplicates();
        }

        @Test
        @DisplayName("닉네임 길이는 12자를 초과하지 않는다")
        void generateNicknames_allNicknamesWithinMaxLength() {
            // given
            NicknameGenerateRequest request = new NicknameGenerateRequest(10, NicknameTheme.RANDOM, false);

            // when
            NicknameGenerateResponse response = nicknameGeneratorService.generateNicknames(request);

            // then
            assertThat(response.nicknames()).allSatisfy(nickname ->
                assertThat(nickname.length()).isLessThanOrEqualTo(12)
            );
        }
    }

    @Nested
    @DisplayName("generateSingleNickname 메서드 테스트")
    class GenerateSingleNicknameTest {

        @Test
        @DisplayName("단일 닉네임을 생성한다")
        void generateSingleNickname_returnsOneNickname() {
            // when
            String nickname = nicknameGeneratorService.generateSingleNickname();

            // then
            assertThat(nickname).isNotBlank();
            assertThat(nickname).contains(" "); // 형용사 + 공백 + 명사 형식
        }

        @Test
        @DisplayName("특정 테마로 닉네임을 생성한다")
        void generateSingleNickname_withTheme_returnsThemedNickname() {
            // when
            String nickname = nicknameGeneratorService.generateSingleNickname(NicknameTheme.TRAVEL, false);

            // then
            assertThat(nickname).isNotBlank();
        }
    }

    @Nested
    @DisplayName("generateUniqueNickname 메서드 테스트")
    class GenerateUniqueNicknameTest {

        @Test
        @DisplayName("유니크한 닉네임을 생성한다")
        void generateUniqueNickname_returnsUniqueNickname() {
            // when
            String nickname = nicknameGeneratorService.generateUniqueNickname(NicknameTheme.RANDOM);

            // then
            assertThat(nickname).isNotBlank();
            assertThat(nickname.length()).isLessThanOrEqualTo(12);
        }

        @Test
        @DisplayName("이미 사용 중인 닉네임이 있으면 다른 닉네임을 생성한다")
        void generateUniqueNickname_whenNicknameExists_generatesAlternative() {
            // given - 처음 몇 번은 중복, 그 후 사용 가능
            given(userRepository.existsByNickname(anyString()))
                    .willReturn(true)
                    .willReturn(true)
                    .willReturn(false);

            // when
            String nickname = nicknameGeneratorService.generateUniqueNickname(NicknameTheme.TRAVEL);

            // then
            assertThat(nickname).isNotBlank();
        }
    }
}
