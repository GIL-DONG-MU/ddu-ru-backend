package com.dduru.gildongmu.home.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record HomePopularDestinationResponse(
        LocalDateTime updatedAt,
        List<HomePopularDestinationItemResponse> items
) {
}
