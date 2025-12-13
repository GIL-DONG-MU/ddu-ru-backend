package com.dduru.gildongmu.nickname.controller;

import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.nickname.dto.NicknameGenerateRequest;
import com.dduru.gildongmu.nickname.dto.NicknameGenerateResponse;
import com.dduru.gildongmu.nickname.enums.NicknameTheme;
import com.dduru.gildongmu.nickname.service.NicknameGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("NicknameController 단위 테스트")
class NicknameControllerTest {

    @InjectMocks
    private NicknameController nicknameController;

    @Mock
    private NicknameGeneratorService nicknameGeneratorService;

    @Nested
    @DisplayName("generateNicknames 메서드 테스트")
    class GenerateNicknamesTest {

        @Test
        @DisplayName("유효한 요청으로 닉네임을 생성한다")
        void generateNicknames_withValidRequest_returnsNicknames() {
            // given
            NicknameGenerateRequest request = new NicknameGenerateRequest(5, NicknameTheme.TRAVEL, false);
            List<String> nicknames = List.of("용감한 여행자", "씩씩한 모험가", "자유로운 탐험가");
            NicknameGenerateResponse response = NicknameGenerateResponse.of(nicknames);

            given(nicknameGeneratorService.generateNicknames(any(NicknameGenerateRequest.class)))
                    .willReturn(response);

            // when
            ResponseEntity<ApiResult<NicknameGenerateResponse>> result = nicknameController.generateNicknames(request);

            // then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().data().nicknames()).hasSize(3);
            assertThat(result.getBody().data().count()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("generateNicknamesByTheme 메서드 테스트")
    class GenerateNicknamesByThemeTest {

        @Test
        @DisplayName("테마별로 닉네임을 생성한다")
        void generateNicknamesByTheme_withValidTheme_returnsNicknames() {
            // given
            List<String> nicknames = List.of("푸른 하늘", "맑은 바다");
            NicknameGenerateResponse response = NicknameGenerateResponse.of(nicknames);

            given(nicknameGeneratorService.generateNicknames(any(NicknameGenerateRequest.class)))
                    .willReturn(response);

            // when
            ResponseEntity<ApiResult<NicknameGenerateResponse>> result = 
                    nicknameController.generateNicknamesByTheme(NicknameTheme.NATURE, 5);

            // then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().data().nicknames()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("generateSingleNickname 메서드 테스트")
    class GenerateSingleNicknameTest {

        @Test
        @DisplayName("단일 닉네임을 생성한다")
        void generateSingleNickname_returnsOneNickname() {
            // given
            given(nicknameGeneratorService.generateUniqueNickname(NicknameTheme.RANDOM))
                    .willReturn("용감한 여행자");

            // when
            ResponseEntity<ApiResult<NicknameGenerateResponse>> result = nicknameController.generateSingleNickname();

            // then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().data().nicknames()).containsExactly("용감한 여행자");
            assertThat(result.getBody().data().count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getAvailableThemes 메서드 테스트")
    class GetAvailableThemesTest {

        @Test
        @DisplayName("사용 가능한 테마 목록을 반환한다")
        void getAvailableThemes_returnsAllThemes() {
            // when
            ResponseEntity<ApiResult<NicknameTheme[]>> result = nicknameController.getAvailableThemes();

            // then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().data()).hasSize(NicknameTheme.values().length);
        }
    }
}
