package com.dduru.gildongmu.survey.service;

import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import org.springframework.stereotype.Component;

@Component
public class AvatarMatcher {

    private static final double THRESHOLD = 5.0;

    public AvatarType match(double rhythmScore, double energyScore, double consumptionScore, double decisionScore) {
        boolean isAdventurous = rhythmScore >= THRESHOLD;
        boolean isEnergetic = energyScore >= THRESHOLD;
        boolean isFlex = consumptionScore >= THRESHOLD;
        boolean isSocial = decisionScore >= THRESHOLD;

        if (!isEnergetic) {
            if (!isAdventurous && !isSocial && !isFlex) return AvatarType.TTUR_POGUNI;
            if (!isAdventurous && !isSocial) return AvatarType.TTUR_MOOD;
            if (!isAdventurous && !isFlex) return AvatarType.TTUR_MALLANGI;
            if (!isAdventurous) return AvatarType.TTUR_SWEET;
            if (!isSocial && !isFlex) return AvatarType.TTUR_POPO;
            if (!isSocial) return AvatarType.TTUR_SPARKLE;
            if (!isFlex) return AvatarType.TTUR_GLIMMING;
            return AvatarType.TTUR_PADO;
        }

        if (!isAdventurous && !isSocial && !isFlex) return AvatarType.TTUR_DASHI;
        if (!isAdventurous && !isSocial) return AvatarType.TTUR_FLARE;
        if (!isAdventurous && !isFlex) return AvatarType.TTUR_BOUNCY;
        if (!isAdventurous) return AvatarType.TTUR_PEPPI;
        if (!isSocial && !isFlex) return AvatarType.TTUR_JETTI;
        if (!isSocial) return AvatarType.TTUR_BLAZE;
        if (!isFlex) return AvatarType.TTUR_VIVID;
        return AvatarType.TTUR_SURGE;
    }
}
