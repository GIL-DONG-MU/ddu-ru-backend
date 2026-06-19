package com.dduru.gildongmu.journey.dto.response;

import java.util.List;

public record JourneyGalleryResponse(
        Long journeyId,
        List<GalleryImageInfo> images,
        GalleryCursor nextCursor,
        boolean hasNext,
        int size
) {
    public static JourneyGalleryResponse of(Long journeyId, List<GalleryImageInfo> images, boolean hasNext) {
        GalleryCursor nextCursor = null;
        if (hasNext && !images.isEmpty()) {
            GalleryImageInfo last = images.get(images.size() - 1);
            nextCursor = new GalleryCursor(last.postedAt(), last.journeyPostId(), last.sortOrder());
        }
        return new JourneyGalleryResponse(journeyId, images, nextCursor, hasNext, images.size());
    }
}
