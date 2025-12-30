package com.dduru.gildongmu.survey.dto;

import java.util.List;

public record AvatarProfileResponse(
        String description,
        String personality,
        String strength,
        String tip,
        List<String> tags
) {
}
