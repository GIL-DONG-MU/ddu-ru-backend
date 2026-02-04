package com.dduru.gildongmu.post.dto;

import com.dduru.gildongmu.profile.domain.enums.AgeRange;
import com.dduru.gildongmu.profile.domain.enums.Gender;

public record ParsedPostData(
        Gender preferredGender,
        AgeRange preferredAgeMin,
        AgeRange preferredAgeMax,
        String photoUrlsJson,
        String tagsJson
) {
}
