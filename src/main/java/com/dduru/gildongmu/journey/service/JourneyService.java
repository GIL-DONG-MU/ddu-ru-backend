package com.dduru.gildongmu.journey.service;

import com.dduru.gildongmu.common.validation.InvalidImageUrlException;
import com.dduru.gildongmu.common.validation.S3ImageUrlValidator;
import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.dto.request.JourneyUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyUpdateResponse;
import com.dduru.gildongmu.journey.exception.InvalidJourneyBasicInfoException;
import com.dduru.gildongmu.journey.exception.JourneyAccessDeniedException;
import com.dduru.gildongmu.journey.repository.JourneyRepository;
import com.dduru.gildongmu.s3.enums.S3ImageDirectory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class JourneyService {
    private static final int TITLE_MIN_LENGTH = 5;
    private static final int TITLE_MAX_LENGTH = 40;

    private final JourneyRepository journeyRepository;
    private final S3ImageUrlValidator s3ImageUrlValidator;

    public JourneyUpdateResponse update(Long journeyId, Long userId, JourneyUpdateRequest request) {
        Journey journey = getUpdatableJourney(journeyId, userId);

        String title = normalizeTitle(request.title());
        String photoUrl = normalizePhotoUrl(request.photoUrl());
        validateHasAnyPatch(title, photoUrl);

        journey.updateBasicInfo(title, photoUrl);
        log.info("나의 여정 기본 정보 수정됨 - journeyId={}, userId={}", journeyId, userId);
        return JourneyUpdateResponse.from(journey);
    }

    private Journey getUpdatableJourney(Long journeyId, Long userId) {
        return journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE)
                .orElseThrow(JourneyAccessDeniedException::new);
    }

    private void validateHasAnyPatch(String title, String photoUrl) {
        if (title == null && photoUrl == null) {
            throw InvalidJourneyBasicInfoException.emptyPatch();
        }
    }

    private String normalizeTitle(String title) {
        if (!StringUtils.hasText(title)) {
            return null;
        }
        String normalizedTitle = title.trim();
        validateTitleLength(normalizedTitle);
        return normalizedTitle;
    }

    private void validateTitleLength(String title) {
        int length = title.codePointCount(0, title.length());
        if (length < TITLE_MIN_LENGTH || length > TITLE_MAX_LENGTH) {
            throw InvalidJourneyBasicInfoException.invalidTitleLength();
        }
    }

    private String normalizePhotoUrl(String photoUrl) {
        if (!StringUtils.hasText(photoUrl)) {
            return null;
        }

        try {
            return s3ImageUrlValidator.validateAndNormalize(photoUrl, S3ImageDirectory.JOURNEYS);
        } catch (InvalidImageUrlException e) {
            throw InvalidJourneyBasicInfoException.invalidPhotoUrl();
        }
    }
}
