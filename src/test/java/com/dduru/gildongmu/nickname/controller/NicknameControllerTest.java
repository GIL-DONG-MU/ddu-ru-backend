package com.dduru.gildongmu.nickname.controller;

import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.nickname.dto.NicknameGenerateResponse;
import com.dduru.gildongmu.nickname.service.NicknameGeneratorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("NicknameController 테스트")
class NicknameControllerTest {

    @InjectMocks
    private NicknameController nicknameController;

    @Mock
    private NicknameGeneratorService nicknameGeneratorService;

    @Test
    @DisplayName("랜덤 닉네임을 생성한다")
    void generateNickname_returnsNickname() {
        // given
        given(nicknameGeneratorService.generateUniqueNickname())
                .willReturn("용감한 여행자");

        // when
        ResponseEntity<ApiResult<NicknameGenerateResponse>> result = nicknameController.generateNickname();

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().data().nickname()).isEqualTo("용감한 여행자");
    }
}
