package com.dduru.gildongmu.survey.dto.response;

import com.dduru.gildongmu.survey.domain.TravelTendency;
import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import com.dduru.gildongmu.survey.domain.enums.RecordStyleType;
import com.dduru.gildongmu.survey.service.AvatarProfileService;

public record SurveyResponse(
        TendencyScoreResponse tendencyScores,
        Integer avatarCode,
        AvatarType avatarType,
        RecordStyleType recordStyleType,
        String avatarLabel,
        AvatarProfileResponse avatarProfile
) {
    public static SurveyResponse from(
            TravelTendency travelTendency,
            RecordStyleType recordStyleType,
            AvatarProfileService avatarProfileService
    ) {
        AvatarType avatarType = travelTendency.getAvatarType();
        AvatarProfileResponse avatarProfile = avatarProfileService.getProfile(avatarType);

        TendencyScoreResponse tendencyScores = new TendencyScoreResponse(
                travelTendency.getRhythmScore().doubleValue(),
                travelTendency.getEnergyScore().doubleValue(),
                travelTendency.getConsumptionScore().doubleValue(),
                travelTendency.getDecisionScore().doubleValue()
        );

        return new SurveyResponse(
                tendencyScores,
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
                scores,
                avatarType.getCode(),
                avatarType,
                recordStyleType,
                avatarLabel(avatarProfile, recordStyleType),
                avatarProfile
        );
    }

    private static String avatarLabel(AvatarProfileResponse avatarProfile, RecordStyleType recordStyleType) {
        return avatarProfile.characterName() + "-" + recordStyleType.name();
    }
}
