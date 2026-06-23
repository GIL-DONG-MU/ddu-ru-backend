package com.dduru.gildongmu.home.dto.response;

import java.util.List;

public record HomePopularDestinationItemResponse(
        int rank,
        Long destinationId,
        String destinationName,
        String imageUrl,
        long availableTripCount,
        List<String> tags
) {
}
