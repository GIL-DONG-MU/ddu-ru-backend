package com.dduru.gildongmu.survey.service;

import com.dduru.gildongmu.profile.service.ProfileManagementService;
import com.dduru.gildongmu.survey.converter.SurveyConverter;
import com.dduru.gildongmu.survey.domain.Survey;
import com.dduru.gildongmu.survey.domain.TravelTendency;
import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import com.dduru.gildongmu.survey.dto.response.AvatarProfileResponse;
import com.dduru.gildongmu.survey.dto.request.SurveyRequest;
import com.dduru.gildongmu.survey.dto.response.SurveyResponse;
import com.dduru.gildongmu.survey.exception.SurveyResultNotFoundException;
import com.dduru.gildongmu.survey.repository.AvatarProfileRepository;
import com.dduru.gildongmu.survey.repository.SurveyRepository;
import com.dduru.gildongmu.survey.repository.TravelTendencyRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final TravelTendencyRepository travelTendencyRepository;
    private final SurveyConverter surveyConverter;
    private final TravelTendencyCalculator tendencyCalculator;
    private final AvatarMatcher avatarMatcher;
    private final AvatarProfileService avatarProfileService;
    private final AvatarProfileRepository avatarProfileRepository;
    private final ProfileManagementService profileManagementService;
    private final UserRepository userRepository;

    public SurveyResponse submitSurvey(Long userId, SurveyRequest request) {
        log.debug("설문조사 제출 시작 - userId: {}", userId);

        User user = userRepository.getByIdOrThrow(userId);
        Survey survey = saveOrUpdateSurvey(user, request);

        TravelTendencyCalculator.TendencyScores scores = tendencyCalculator.calculate(survey);
        AvatarType avatarType = avatarMatcher.match(scores.r(), scores.w(), scores.s());

        saveOrUpdateTravelTendency(user, scores, avatarType);

        saveAvatarIdToProfile(userId, avatarType);
        onboardingService.completeSurvey(userId);

        AvatarProfileResponse profile = avatarProfileService.getProfile(avatarType);

        log.info("설문조사 제출 완료 - userId: {}, avatarType: {}, 점수: R={}, W={}, S={}, P={}",
                userId, avatarType, scores.r(), scores.w(), scores.s(), scores.p());
        return SurveyResponse.of(scores.r(), scores.w(), scores.s(), scores.p(), avatarType, profile);
    }

    @Transactional(readOnly = true)
    public SurveyResponse getMySurveyResult(Long userId) {
        log.debug("설문 결과 조회 시작 - userId: {}", userId);

        User user = userRepository.getByIdOrThrow(userId);
        TravelTendency travelTendency = travelTendencyRepository.findByUser(user)
                .orElseThrow(SurveyResultNotFoundException::of);

        log.info("설문 결과 조회 완료 - userId: {}, avatarType: {}", userId, travelTendency.getAvatarType());
        return SurveyResponse.from(travelTendency, avatarProfileService);
    }

    private Survey saveOrUpdateSurvey(User user, SurveyRequest request) {
        SurveyConverter.ParsedSurveyData parsed = surveyConverter.parseRequest(request);

        return surveyRepository.findByUser(user)
                .map(existing -> {
                    existing.updateSurvey(parsed.q1(), parsed.q2(), parsed.q3(), parsed.q4(), parsed.q5(),
                            parsed.q6(), parsed.q7(), parsed.q8(), parsed.q9(), parsed.q10(), parsed.q11());
                    return existing;
                })
                .orElseGet(() -> surveyRepository.save(surveyConverter.toEntity(user, request)));
    }

    private void saveOrUpdateTravelTendency(User user, TravelTendencyCalculator.TendencyScores scores, AvatarType avatarType) {
        BigDecimal rDecimal = toBigDecimal(scores.r());
        BigDecimal wDecimal = toBigDecimal(scores.w());
        BigDecimal sDecimal = toBigDecimal(scores.s());
        BigDecimal pDecimal = toBigDecimal(scores.p());

        travelTendencyRepository.findByUser(user)
                .ifPresentOrElse(
                        existing -> existing.update(rDecimal, wDecimal, sDecimal, pDecimal, avatarType),
                        () -> travelTendencyRepository.save(
                                TravelTendency.create(user, rDecimal, wDecimal, sDecimal, pDecimal, avatarType)
                        )
                );
    }

    private void saveAvatarIdToProfile(Long userId, AvatarType avatarType) {
        avatarProfileRepository.findByAvatarType(avatarType)
                .ifPresentOrElse(
                        avatarProfile -> {
                            profileManagementService.updateAvatar(userId, avatarProfile.getId());
                            log.debug("아바타 ID 저장 완료 - userId: {}, avatarId: {}", userId, avatarProfile.getId());
                        },
                        () -> log.warn("아바타 프로필을 찾을 수 없어 Profile에 저장하지 않음 - userId: {}, avatarType: {}", userId, avatarType)
                );
    }

    private BigDecimal toBigDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP);
    }
}
