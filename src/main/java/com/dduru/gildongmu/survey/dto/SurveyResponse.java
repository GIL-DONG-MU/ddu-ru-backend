package com.dduru.gildongmu.survey.dto;

import com.dduru.gildongmu.survey.domain.TravelTendency;
import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import com.dduru.gildongmu.survey.service.AvatarProfileService;

import java.util.List;

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
        String tip,
        List<String> tags
) {
    public static SurveyResponse from(TravelTendency travelTendency, AvatarProfileService avatarProfileService) {
        AvatarType avatarType = travelTendency.getAvatarType();
        AvatarProfileResponse profile = avatarProfileService.getProfile(avatarType);

        return new SurveyResponse(
                travelTendency.getR().doubleValue(),
                travelTendency.getW().doubleValue(),
                travelTendency.getS().doubleValue(),
                travelTendency.getP().doubleValue(),
                avatarType.getCode(),
                avatarType.name(),
                avatarType.getText(),
                profile.description(),
                profile.personality(),
                profile.strength(),
                profile.tip(),
                profile.tags()
        );
    }

    public static SurveyResponse of(
            double r, double w, double s, double p,
            AvatarType avatarType,
            AvatarProfileResponse profile
    ) {
        return new SurveyResponse(
                r, w, s, p,
                avatarType.getCode(),
                avatarType.name(),
                avatarType.getText(),
                profile.description(),
                profile.personality(),
                profile.strength(),
                profile.tip(),
                profile.tags()
        );
    }
}
