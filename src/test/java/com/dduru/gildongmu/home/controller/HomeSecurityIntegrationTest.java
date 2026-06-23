package com.dduru.gildongmu.home.controller;

import com.dduru.gildongmu.common.config.SecurityConfig;
import com.dduru.gildongmu.common.jwt.JwtAccessDeniedHandler;
import com.dduru.gildongmu.common.jwt.JwtAuthenticationEntryPoint;
import com.dduru.gildongmu.common.jwt.JwtTokenProvider;
import com.dduru.gildongmu.home.HomeEndpoints;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HomeSecurityIntegrationTest.TestHomeSecurityController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationEntryPoint.class,
        JwtAccessDeniedHandler.class,
        HomeSecurityIntegrationTest.TestHomeSecurityController.class
})
@DisplayName("Home Security 통합 테스트")
// HomeController 로직이 아니라 SecurityConfig의 홈 URL public/private 정책을 실제 필터체인으로 검증한다.
class HomeSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("비회원은 홈 초기 구성 경로에 접근할 수 있다")
    void guestCanAccessHome() throws Exception {
        mockMvc.perform(get(HomeEndpoints.HOME))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비회원은 홈 public 섹션 경로에 접근할 수 있다")
    void guestCanAccessPublicHomeSection() throws Exception {
        mockMvc.perform(get(HomeEndpoints.POPULAR_DESTINATIONS))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비회원은 홈 회원 전용 섹션 경로에 접근할 수 없다")
    void guestCannotAccessMemberOnlyHomeSection() throws Exception {
        mockMvc.perform(get(HomeEndpoints.SAME_AGE_TRIPS))
                .andExpect(status().isUnauthorized());
    }

    @RestController
    static class TestHomeSecurityController {

        @GetMapping(HomeEndpoints.HOME)
        ResponseEntity<Void> home() {
            return ResponseEntity.ok().build();
        }

        @GetMapping(HomeEndpoints.POPULAR_DESTINATIONS)
        ResponseEntity<Void> popularDestinations() {
            return ResponseEntity.ok().build();
        }

        @GetMapping(HomeEndpoints.SAME_AGE_TRIPS)
        ResponseEntity<Void> sameAgeTrips() {
            return ResponseEntity.ok().build();
        }
    }

    @SpringBootConfiguration
    static class TestApplication {
    }
}
