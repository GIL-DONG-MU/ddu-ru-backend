package com.dduru.gildongmu.survey.service;

import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("아바타 매칭 테스트")
class AvatarMatcherTest {

    private AvatarMatcher avatarMatcher;

    @BeforeEach
    void setUp() {
        avatarMatcher = new AvatarMatcher();
    }

    @Test
    @DisplayName("안정_x_독립_x_가성비_매칭")
    void 안정_x_독립_x_가성비_매칭() {
        // given
        double r = 4.0;
        double w = 4.0;
        double s = 4.0;

        // when
        AvatarType avatar = avatarMatcher.match(r, w, s);

        // then
        assertThat(avatar).isEqualTo(AvatarType.TTUR_POGUNI);
    }

    @Test
    @DisplayName("안정_x_독립_x_플랙스_매칭")
    void 안정_x_독립_x_플랙스_매칭() {
        // given
        double r = 4.0;
        double w = 6.0;
        double s = 4.0;

        // when
        AvatarType avatar = avatarMatcher.match(r, w, s);

        // then
        assertThat(avatar).isEqualTo(AvatarType.TTUR_MOOD);
    }

    @Test
    @DisplayName("안정_x_사교_x_가성비_매칭")
    void 안정_x_사교_x_가성비_매칭() {
        // given
        double r = 4.0;
        double w = 4.0;
        double s = 6.0;

        // when
        AvatarType avatar = avatarMatcher.match(r, w, s);

        // then
        assertThat(avatar).isEqualTo(AvatarType.TTUR_MALLANGI);
    }

    @Test
    @DisplayName("안정_x_사교_x_플랙스_매칭")
    void 안정_x_사교_x_플랙스_매칭() {
        // given
        double r = 4.0;
        double w = 6.0;
        double s = 6.0;

        // when
        AvatarType avatar = avatarMatcher.match(r, w, s);

        // then
        assertThat(avatar).isEqualTo(AvatarType.TTUR_SWEET);
    }

    @Test
    @DisplayName("모험_x_독립_x_가성비_매칭")
    void 모험_x_독립_x_가성비_매칭() {
        // given
        double r = 6.0;
        double w = 4.0;
        double s = 4.0;

        // when
        AvatarType avatar = avatarMatcher.match(r, w, s);

        // then
        assertThat(avatar).isEqualTo(AvatarType.TTUR_POPO);
    }

    @Test
    @DisplayName("모험_x_독립_x_플랙스_매칭")
    void 모험_x_독립_x_플랙스_매칭() {
        // given
        double r = 6.0;
        double w = 6.0;
        double s = 4.0;

        // when
        AvatarType avatar = avatarMatcher.match(r, w, s);

        // then
        assertThat(avatar).isEqualTo(AvatarType.TTUR_SPARKLE);
    }

    @Test
    @DisplayName("모험_x_사교_x_가성비_매칭")
    void 모험_x_사교_x_가성비_매칭() {
        // given
        double r = 6.0;
        double w = 4.0;
        double s = 6.0;

        // when
        AvatarType avatar = avatarMatcher.match(r, w, s);

        // then
        assertThat(avatar).isEqualTo(AvatarType.TTUR_GLIMMING);
    }

    @Test
    @DisplayName("모험_x_사교_x_플랙스_매칭")
    void 모험_x_사교_x_플랙스_매칭() {
        // given
        double r = 6.0;
        double w = 6.0;
        double s = 6.0;

        // when
        AvatarType avatar = avatarMatcher.match(r, w, s);

        // then
        assertThat(avatar).isEqualTo(AvatarType.TTUR_PADO);
    }

    @Test
    @DisplayName("임계값_5점_경계_테스트")
    void 임계값_5점_경계_테스트() {
        // given
        double r = 5.0;
        double w = 5.0;
        double s = 5.0;

        // when
        AvatarType avatar = avatarMatcher.match(r, w, s);

        // then
        assertThat(avatar).isEqualTo(AvatarType.TTUR_PADO);
    }
}
