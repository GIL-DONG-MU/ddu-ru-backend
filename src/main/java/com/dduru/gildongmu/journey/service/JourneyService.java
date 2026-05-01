package com.dduru.gildongmu.journey.service;

import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.journey.domain.JourneyMember;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.dto.request.JourneyUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyUpdateResponse;
import com.dduru.gildongmu.journey.exception.InvalidJourneyBasicInfoException;
import com.dduru.gildongmu.journey.exception.JourneyAccessDeniedException;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.journey.repository.JourneyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class JourneyService {
    private static final int TITLE_MIN_LENGTH = 5;
    private static final int TITLE_MAX_LENGTH = 40;

    private final JourneyRepository journeyRepository;
    private final JourneyMemberRepository journeyMemberRepository;

    public JourneyUpdateResponse update(Long journeyId, Long userId, JourneyUpdateRequest request) {
        Journey journey = journeyRepository.getByIdOrThrow(journeyId);
        JourneyMember journeyMember = journeyMemberRepository.findByJourneyIdAndUserId(journeyId, userId)
                .orElseThrow(JourneyAccessDeniedException::new);

        validateActiveMemberAccess(journeyMember);

        String title = normalizeText(request.title());
        String photoUrl = normalizeText(request.photoUrl());
        validateHasAnyPatch(title, photoUrl);
        validateTitle(title);

        journey.update(title, photoUrl);
        return JourneyUpdateResponse.from(journey);
    }

    private void validateActiveMemberAccess(JourneyMember journeyMember) {
        if (journeyMember.getStatus() != JourneyMemberStatus.ACTIVE) {
            throw new JourneyAccessDeniedException();
        }
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
        if (title.length() < TITLE_MIN_LENGTH || title.length() > TITLE_MAX_LENGTH) {
            throw InvalidJourneyBasicInfoException.invalidTitleLength();
        }
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
