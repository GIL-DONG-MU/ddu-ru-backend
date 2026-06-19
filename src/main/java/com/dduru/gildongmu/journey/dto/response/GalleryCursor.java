package com.dduru.gildongmu.journey.dto.response;

import java.time.LocalDateTime;

public record GalleryCursor(
        LocalDateTime createdAt,
        Long journeyPostId,
        int sortOrder
) {}
