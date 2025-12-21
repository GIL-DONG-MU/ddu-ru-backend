package com.dduru.gildongmu.survey.dto;

import com.dduru.gildongmu.survey.domain.TravelTendency;
import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import com.dduru.gildongmu.survey.service.AvatarProfileProvider;

public record SurveyResponse(
        Double r,
        Double w,
        Double s,
        Double p,
        Integer avatarCode,
        String avatarType,
        String avatarName,
        String avatarDescription,
        String personality,
        String strength,
        String tip
) {
    public static SurveyResponse from(TravelTendency travelTendency, AvatarProfileProvider avatarProfileProvider) {
        AvatarType avatarType = travelTendency.getAvatarType();
        AvatarProfileProvider.AvatarProfile profile = avatarProfileProvider.getProfile(avatarType);

        return new SurveyResponse(
                travelTendency.getR().doubleValue(),
                travelTendency.getW().doubleValue(),
                travelTendency.getS().doubleValue(),
                travelTendency.getP().doubleValue(),
                avatarType.getCode(),
                avatarType.name(),
                avatarType.getText(),
                avatarType.getDescription(),
                profile.personality(),
                profile.strength(),
                profile.tip()
        );
    }

    public static SurveyResponse of(
            double r, double w, double s, double p,
            AvatarType avatarType,
            AvatarProfileProvider.AvatarProfile profile
    ) {
        return new SurveyResponse(
                r, w, s, p,
                avatarType.getCode(),
                avatarType.name(),
                avatarType.getText(),
                avatarType.getDescription(),
                profile.personality(),
                profile.strength(),
                profile.tip()
        );
    }
}
