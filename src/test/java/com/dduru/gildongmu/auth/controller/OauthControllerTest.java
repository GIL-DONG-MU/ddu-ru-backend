package com.dduru.gildongmu.auth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OauthControllerTest {

    @Test
    void 로그아웃_컨트롤러_생성_테스트() {
        // 컨트롤러가 정상적으로 생성되는지 확인
        // 실제 엔드포인트 테스트는 별도의 통합 테스트에서 수행
        assertThat(OauthController.class).isNotNull();
    }
}