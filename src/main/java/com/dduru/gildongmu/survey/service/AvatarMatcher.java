package com.dduru.gildongmu.survey.service;

import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AvatarMatcher {

    private static final double THRESHOLD = 5.0;

    public AvatarType match(double r, double w, double s) {
        // R: 5.0 미만 = 안정, 5.0 이상 = 모험
        boolean isStable = r < THRESHOLD;

        // W: 5.0 미만 = 가성비, 5.0 이상 = 플랙스
        boolean isCostEffective = w < THRESHOLD;

        // S: 5.0 미만 = 독립, 5.0 이상 = 사교
        boolean isSocial = s >= THRESHOLD;

        // 8가지 조합 매칭
        if (isStable && !isSocial && isCostEffective) {
            return AvatarType.TTUR_POGUNI;  // 안정 x 독립 x 가성비
        } else if (isStable && !isSocial) {
            return AvatarType.TTUR_MOOD;  // 안정 x 독립 x 플랙스
        } else if (isStable && isCostEffective) {
            return AvatarType.TTUR_MALLANGI;  // 안정 x 사교 x 가성비
        } else if (isStable) {
            return AvatarType.TTUR_SWEET;  // 안정 x 사교 x 플랙스
        } else if (!isSocial && isCostEffective) {
            return AvatarType.TTUR_POPO;  // 모험 x 독립 x 가성비
        } else if (!isSocial) {
            return AvatarType.TTUR_SPARKLE;  // 모험 x 독립 x 플랙스
        } else if (isCostEffective) {
            return AvatarType.TTUR_GLIMMING;  // 모험 x 사교 x 가성비
        } else {
            return AvatarType.TTUR_PADO;  // 모험 x 사교 x 플랙스
        }
    }
}
