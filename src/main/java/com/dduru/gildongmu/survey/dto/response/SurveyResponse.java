package com.dduru.gildongmu.survey.dto.response;

import com.dduru.gildongmu.survey.domain.TravelTendency;
import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import com.dduru.gildongmu.survey.service.AvatarProfileService;

import java.util.List;

public record SurveyResponse(
        Double rhythmScore,
        Double energyScore,
        Double consumptionScore,
        Double decisionScore,
        Integer avatarCode,
        AvatarType avatarType,
        String avatarName,
        String avatarDescription,
        String imageUrl,
        String personality,
        String strength,
        String tip,
        List<String> tags
) {
    public static SurveyResponse from(TravelTendency travelTendency, AvatarProfileService avatarProfileService) {
        AvatarType avatarType = travelTendency.getAvatarType();
        AvatarProfileResponse avatarProfile = avatarProfileService.getProfile(avatarType);

        return new SurveyResponse(
                travelTendency.getRhythmScore().doubleValue(),
                travelTendency.getEnergyScore().doubleValue(),
                travelTendency.getConsumptionScore().doubleValue(),
                travelTendency.getDecisionScore().doubleValue(),
                avatarType.getCode(),
                avatarType,
                avatarType.getText(),
                avatarProfile.description(),
                avatarProfile.imageUrl(),
                avatarProfile.personality(),
                avatarProfile.strength(),
                avatarProfile.tip(),
                avatarProfile.tags()
        );
    }

    public static SurveyResponse of(
            TendencyScoreResponse scores,
            AvatarType avatarType,
            AvatarProfileResponse avatarProfile
    ) {
        return new SurveyResponse(
                scores.rhythmScore(),
                scores.energyScore(),
                scores.consumptionScore(),
                scores.decisionScore(),
                avatarType.getCode(),
                avatarType,
                avatarType.getText(),
                avatarProfile.description(),
                avatarProfile.imageUrl(),
                avatarProfile.personality(),
                avatarProfile.strength(),
                avatarProfile.tip(),
                avatarProfile.tags()
        );
    }

}
