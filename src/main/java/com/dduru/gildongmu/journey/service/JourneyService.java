package com.dduru.gildongmu.journey.service;

import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.dto.request.JourneyUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyUpdateResponse;
import com.dduru.gildongmu.journey.exception.InvalidJourneyBasicInfoException;
import com.dduru.gildongmu.journey.exception.JourneyAccessDeniedException;
import com.dduru.gildongmu.journey.repository.JourneyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

@Service
@RequiredArgsConstructor
@Transactional
public class JourneyService {
    private static final int TITLE_MIN_LENGTH = 5;
    private static final int TITLE_MAX_LENGTH = 40;

    private final JourneyRepository journeyRepository;

    public JourneyUpdateResponse update(Long journeyId, Long userId, JourneyUpdateRequest request) {
        Journey journey = journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE)
                .orElseThrow(JourneyAccessDeniedException::new);

        String title = normalizeTitle(request.title());
        String photoUrl = normalizePhotoUrl(request.photoUrl());
        validateHasAnyPatch(title, photoUrl);

        journey.update(title, photoUrl);
        return JourneyUpdateResponse.from(journey);
    }

    private void validateHasAnyPatch(String title, String photoUrl) {
        if (title == null && photoUrl == null) {
            throw InvalidJourneyBasicInfoException.emptyPatch();
        }
    }

    private void validateTitle(String title) {
        if (title == null) {
            return;
        }
        int length = title.codePointCount(0, title.length());
        if (length < TITLE_MIN_LENGTH || length > TITLE_MAX_LENGTH) {
            throw InvalidJourneyBasicInfoException.invalidTitleLength();
        }
    }

    private String normalizeTitle(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String title = raw.trim();
        validateTitle(title);
        return title;
    }

    private String normalizePhotoUrl(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String photoUrl = raw.trim();
        validatePhotoUrl(photoUrl);
        return photoUrl;
    }

    private void validatePhotoUrl(String photoUrl) {
        try {
            URI uri = new URI(photoUrl);
            if (!uri.isAbsolute()) {
                throw InvalidJourneyBasicInfoException.invalidPhotoUrl();
            }
            if (uri.getScheme() == null || !"https".equalsIgnoreCase(uri.getScheme())) {
                throw InvalidJourneyBasicInfoException.invalidPhotoUrl();
            }
            if (!StringUtils.hasText(uri.getHost())) {
                throw InvalidJourneyBasicInfoException.invalidPhotoUrl();
            }
        } catch (URISyntaxException e) {
            throw InvalidJourneyBasicInfoException.invalidPhotoUrl();
        }
    }
}
