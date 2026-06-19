package com.dduru.gildongmu.journey.service;

import com.dduru.gildongmu.journey.domain.JourneyPostImage;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.dto.request.JourneyGalleryRequest;
import com.dduru.gildongmu.journey.dto.response.GalleryImageInfo;
import com.dduru.gildongmu.journey.dto.response.JourneyGalleryResponse;
import com.dduru.gildongmu.journey.exception.JourneyAccessDeniedException;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.journey.repository.JourneyPostImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JourneyGalleryService {

    private final JourneyMemberRepository journeyMemberRepository;
    private final JourneyPostImageRepository journeyPostImageRepository;

    public JourneyGalleryResponse retrieveGallery(Long journeyId, Long userId, JourneyGalleryRequest request) {
        validateJourneyAccess(journeyId, userId);

        int size = request.size();
        List<JourneyPostImage> fetched = journeyPostImageRepository.findGalleryImages(
                journeyId,
                request.cursorCreatedAt(),
                request.cursorPostId(),
                request.cursorSortOrder(),
                PageRequest.of(0, size + 1)
        );

        boolean hasNext = fetched.size() > size;
        List<JourneyPostImage> images = hasNext ? fetched.subList(0, size) : fetched;

        List<GalleryImageInfo> imageInfos = images.stream()
                .map(img -> new GalleryImageInfo(
                        img.getJourneyPost().getId(),
                        img.getImageUrl(),
                        img.getSortOrder(),
                        img.getJourneyPost().getCreatedAt()
                ))
                .toList();

        return JourneyGalleryResponse.of(journeyId, imageInfos, hasNext);
    }

    private void validateJourneyAccess(Long journeyId, Long userId) {
        boolean isActiveMember = journeyMemberRepository.existsByJourneyIdAndUserIdAndStatus(
                journeyId, userId, JourneyMemberStatus.ACTIVE
        );
        if (!isActiveMember) {
            throw new JourneyAccessDeniedException();
        }
    }
}
