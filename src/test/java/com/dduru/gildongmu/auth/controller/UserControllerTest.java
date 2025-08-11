package com.dduru.gildongmu.auth.controller;

import com.dduru.gildongmu.auth.dto.NicknameUpdateRequest;
import com.dduru.gildongmu.auth.dto.NicknameUpdateResponse;
import com.dduru.gildongmu.auth.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.willDoNothing;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void 닉네임_수정_API가_정상적으로_동작한다() {
        // given
        Long userId = 1L;
        String nickname = "새로운닉네임";
        NicknameUpdateRequest request = new NicknameUpdateRequest(nickname);

        willDoNothing().given(userService).updateNickname(userId, nickname);

        // when
        ResponseEntity<NicknameUpdateResponse> response = userController.updateNickname(userId, request);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("닉네임이 성공적으로 수정되었습니다.");
        assertThat(response.getBody().nickname()).isEqualTo(nickname);
    }
}