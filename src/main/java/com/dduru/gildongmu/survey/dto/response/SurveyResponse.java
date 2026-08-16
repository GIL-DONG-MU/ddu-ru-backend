package com.dduru.gildongmu.survey.dto.response;

import com.dduru.gildongmu.survey.domain.TravelTendency;
import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import com.dduru.gildongmu.survey.domain.enums.RecordStyleType;
import com.dduru.gildongmu.survey.service.AvatarProfileService;

import java.time.LocalDate;

public record SurveyResponse(
        TendencyScoreResponse tendencyScores,
        Integer avatarCode,
        AvatarType avatarType,
        RecordStyleType recordStyleType,
        String avatarLabel,
        AvatarProfileResponse avatarProfile,
        LocalDate lastTestedAt,
        boolean canRetake,
        LocalDate nextRetakeAvailableDate,
        long remainingRetakeDays
) {
    public static SurveyResponse from(
            TravelTendency travelTendency,
            RecordStyleType recordStyleType,
            AvatarProfileService avatarProfileService,
            LocalDate lastTestedAt,
            boolean canRetake,
            LocalDate nextRetakeAvailableDate,
            long remainingRetakeDays
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
                avatarProfile,
                lastTestedAt,
                canRetake,
                nextRetakeAvailableDate,
                remainingRetakeDays
        );
    }

    public static SurveyResponse of(
            TendencyScoreResponse scores,
            AvatarType avatarType,
            RecordStyleType recordStyleType,
            AvatarProfileResponse avatarProfile,
            LocalDate lastTestedAt,
            boolean canRetake,
            LocalDate nextRetakeAvailableDate,
            long remainingRetakeDays
    ) {
        return new SurveyResponse(
                scores,
                avatarType.getCode(),
                avatarType,
                recordStyleType,
                avatarLabel(avatarProfile, recordStyleType),
                avatarProfile,
                lastTestedAt,
                canRetake,
                nextRetakeAvailableDate,
                remainingRetakeDays
        );
    }

    private static String avatarLabel(AvatarProfileResponse avatarProfile, RecordStyleType recordStyleType) {
        return avatarProfile.characterName() + "-" + recordStyleType.name();
    }
}
