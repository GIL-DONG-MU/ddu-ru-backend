package com.dduru.gildongmu.auth.controller;

import com.dduru.gildongmu.auth.dto.NicknameUpdateRequest;
import com.dduru.gildongmu.auth.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void 닉네임_수정_API가_정상적으로_동작한다() throws Exception {
        // given
        Long userId = 1L;
        String nickname = "새로운닉네임";
        NicknameUpdateRequest request = new NicknameUpdateRequest(nickname);

        willDoNothing().given(userService).updateNickname(userId, nickname);

        // when & then
        mockMvc.perform(patch("/api/v1/users/nickname")
                        .header("X-User-ID", userId) // @CurrentUser 어노테이션을 위한 헤더
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("닉네임이 성공적으로 수정되었습니다."))
                .andExpect(jsonPath("$.nickname").value(nickname));
    }

    @Test
    void 닉네임이_50자를_초과하면_검증_오류가_발생한다() throws Exception {
        // given
        Long userId = 1L;
        String longNickname = "a".repeat(51); // 51자
        NicknameUpdateRequest request = new NicknameUpdateRequest(longNickname);

        // when & then
        mockMvc.perform(patch("/api/v1/users/nickname")
                        .header("X-User-ID", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}