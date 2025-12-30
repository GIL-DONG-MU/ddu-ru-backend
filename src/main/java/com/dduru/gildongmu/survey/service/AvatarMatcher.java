package com.dduru.gildongmu.survey.service;

import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AvatarMatcher {

    private static final double THRESHOLD = 5.0;

    public AvatarType match(double r, double w, double s) {
        boolean isStable = r < THRESHOLD;
        boolean isCostEffective = w < THRESHOLD;
        boolean isSocial = s >= THRESHOLD;

        if (isStable && !isSocial && isCostEffective) {
            return AvatarType.TTUR_POGUNI;
        } else if (isStable && !isSocial) {
            return AvatarType.TTUR_MOOD;
        } else if (isStable && isCostEffective) {
            return AvatarType.TTUR_MALLANGI;
        } else if (isStable) {
            return AvatarType.TTUR_SWEET;
        } else if (!isSocial && isCostEffective) {
            return AvatarType.TTUR_POPO;
        } else if (!isSocial) {
            return AvatarType.TTUR_SPARKLE;
        } else if (isCostEffective) {
            return AvatarType.TTUR_GLIMMING;
        } else {
            return AvatarType.TTUR_PADO;
        }
    }
}
