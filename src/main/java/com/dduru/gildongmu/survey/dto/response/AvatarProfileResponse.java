package com.dduru.gildongmu.survey.dto.response;

import java.util.List;

public record AvatarProfileResponse(
        String characterName,
        String oneLineDescription,
        List<String> tags,
        String description,
        String imageUrl
) {
}
