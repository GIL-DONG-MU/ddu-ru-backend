package com.dduru.gildongmu.journey.dto.response;

import java.time.LocalDateTime;

public record GalleryImageInfo(
        Long journeyPostId,
        String imageUrl,
        int sortOrder,
        LocalDateTime postedAt
) {}
