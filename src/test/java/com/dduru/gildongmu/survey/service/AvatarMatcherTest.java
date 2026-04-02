package com.dduru.gildongmu.survey.service;

import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("아바타 매칭 테스트")
class AvatarMatcherTest {

    private final AvatarMatcher avatarMatcher = new AvatarMatcher();

    @ParameterizedTest(name = "[{index}] r={0}, e={1}, c={2}, d={3} -> {4}")
    @CsvSource({
            "4.0, 4.0, 4.0, 4.0, TTUR_POGUNI",
            "4.0, 4.0, 6.0, 4.0, TTUR_MOOD",
            "4.0, 4.0, 4.0, 6.0, TTUR_MALLANGI",
            "4.0, 4.0, 6.0, 6.0, TTUR_SWEET",
            "6.0, 4.0, 4.0, 4.0, TTUR_POPO",
            "6.0, 4.0, 6.0, 4.0, TTUR_SPARKLE",
            "6.0, 4.0, 4.0, 6.0, TTUR_GLIMMING",
            "6.0, 4.0, 6.0, 6.0, TTUR_PADO",
            "4.0, 6.0, 4.0, 4.0, TTUR_DASHI",
            "4.0, 6.0, 6.0, 4.0, TTUR_FLARE",
            "4.0, 6.0, 4.0, 6.0, TTUR_BOUNCY",
            "4.0, 6.0, 6.0, 6.0, TTUR_PEPPI",
            "6.0, 6.0, 4.0, 4.0, TTUR_JETTI",
            "6.0, 6.0, 6.0, 4.0, TTUR_BLAZE",
            "6.0, 6.0, 4.0, 6.0, TTUR_VIVID",
            "6.0, 6.0, 6.0, 6.0, TTUR_SURGE"
    })
    @DisplayName("4축 조합별 아바타 매칭")
    void 사축_조합별_아바타_매칭(double rhythm, double energy, double consumption, double decision, AvatarType expected) {
        AvatarType avatar = avatarMatcher.match(rhythm, energy, consumption, decision);
        assertThat(avatar).isEqualTo(expected);
    }

    @ParameterizedTest(name = "[{index}] r={0}, e={1}, c={2}, d={3} -> {4}")
    @CsvSource({
            "5.0, 5.0, 5.0, 5.0, TTUR_SURGE",
            "4.9, 4.9, 4.9, 4.9, TTUR_POGUNI",
            "4.9, 5.0, 4.9, 4.9, TTUR_DASHI",
            "5.0, 4.9, 4.9, 5.0, TTUR_GLIMMING"
    })
    @DisplayName("임계값 5.0 경계 매칭")
    void 임계값_경계_매칭(double rhythm, double energy, double consumption, double decision, AvatarType expected) {
        AvatarType avatar = avatarMatcher.match(rhythm, energy, consumption, decision);
        assertThat(avatar).isEqualTo(expected);
    }
}
