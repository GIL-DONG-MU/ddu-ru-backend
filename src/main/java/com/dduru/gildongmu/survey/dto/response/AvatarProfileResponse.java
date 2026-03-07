package com.dduru.gildongmu.survey.dto.response;

import java.util.List;

public record AvatarProfileResponse(
        String description,
        String imageUrl,
        String personality,
        String strength,
        String tip,
        List<String> tags
) {
}
