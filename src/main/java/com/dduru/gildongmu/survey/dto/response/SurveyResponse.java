package com.dduru.gildongmu.survey.dto.response;

import com.dduru.gildongmu.survey.domain.TravelTendency;
import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import com.dduru.gildongmu.survey.domain.enums.RecordStyleType;
import com.dduru.gildongmu.survey.service.AvatarProfileService;

public record SurveyResponse(
        Double rhythmScore,
        Double energyScore,
        Double consumptionScore,
        Double decisionScore,
        Integer avatarCode,
        AvatarType avatarType,
        RecordStyleType recordStyleType,
        String avatarLabel,
        AvatarProfileResponse avatar
) {
    public static SurveyResponse from(
            TravelTendency travelTendency,
            RecordStyleType recordStyleType,
            AvatarProfileService avatarProfileService
    ) {
        AvatarType avatarType = travelTendency.getAvatarType();
        AvatarProfileResponse avatarProfile = avatarProfileService.getProfile(avatarType);

        return new SurveyResponse(
                travelTendency.getRhythmScore().doubleValue(),
                travelTendency.getEnergyScore().doubleValue(),
                travelTendency.getConsumptionScore().doubleValue(),
                travelTendency.getDecisionScore().doubleValue(),
                avatarType.getCode(),
                avatarType,
                recordStyleType,
                avatarLabel(avatarProfile, recordStyleType),
                avatarProfile
        );
    }

    public static SurveyResponse of(
            TendencyScoreResponse scores,
            AvatarType avatarType,
            RecordStyleType recordStyleType,
            AvatarProfileResponse avatarProfile
    ) {
        return new SurveyResponse(
                scores.rhythmScore(),
                scores.energyScore(),
                scores.consumptionScore(),
                scores.decisionScore(),
                avatarType.getCode(),
                avatarType,
                recordStyleType,
                avatarLabel(avatarProfile, recordStyleType),
                avatarProfile
        );
    }

    private static String avatarLabel(AvatarProfileResponse avatar, RecordStyleType recordStyleType) {
        return avatar.characterName() + "-" + recordStyleType.name();
    }
}
