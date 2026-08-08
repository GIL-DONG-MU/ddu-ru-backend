package com.dduru.gildongmu.home.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record HomePopularDestinationResponse(
        LocalDateTime updatedAt,
        List<Item> items
) {

    public record Item(
            int rank,
            Long destinationId,
            String destinationName,
            String imageUrl,
            long availableTripCount,
            List<String> tags
    ) {
    }
}
