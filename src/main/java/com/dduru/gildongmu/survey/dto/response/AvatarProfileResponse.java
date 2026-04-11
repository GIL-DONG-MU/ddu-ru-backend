package com.dduru.gildongmu.survey.dto.response;

import java.util.List;

public record AvatarProfileResponse(
        String characterName,
        String speechBubbleText,
        List<String> tags,
        String descriptionLine1,
        String descriptionLine2,
        String descriptionLine3,
        String imageUrl
) {
}
