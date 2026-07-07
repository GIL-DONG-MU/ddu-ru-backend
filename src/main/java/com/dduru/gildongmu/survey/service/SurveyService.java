package com.dduru.gildongmu.survey.service;

import com.dduru.gildongmu.onboarding.service.OnboardingService;
import com.dduru.gildongmu.profile.service.ProfileManagementService;
import com.dduru.gildongmu.survey.converter.SurveyConverter;
import com.dduru.gildongmu.survey.converter.ParsedSurveyData;
import com.dduru.gildongmu.survey.domain.Survey;
import com.dduru.gildongmu.survey.domain.TravelTendency;
import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import com.dduru.gildongmu.survey.dto.request.SurveyRequest;
import com.dduru.gildongmu.survey.dto.response.AvatarProfileResponse;
import com.dduru.gildongmu.survey.dto.response.SurveyResponse;
import com.dduru.gildongmu.survey.dto.response.TendencyScoreResponse;
import com.dduru.gildongmu.survey.exception.SurveyResultNotFoundException;
import com.dduru.gildongmu.survey.repository.AvatarProfileRepository;
import com.dduru.gildongmu.survey.repository.SurveyRepository;
import com.dduru.gildongmu.survey.repository.TravelTendencyRepository;
import com.dduru.gildongmu.superhost.service.SuperHostService;
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
    private final OnboardingService onboardingService;
    private final SuperHostService superHostService;

    public SurveyResponse create(Long userId, SurveyRequest request) {
        User user = userRepository.getByIdOrThrow(userId);

        ParsedSurveyData parsed = surveyConverter.parseRequest(request);
        Survey survey = createSurvey(user, parsed);
        TendencyScoreResponse scores = tendencyCalculator.calculate(survey);
        AvatarType avatarType = matchAvatarType(scores);

        saveOrUpdateTravelTendency(user, scores, avatarType);
        updateProfileAvatar(userId, avatarType);
        onboardingService.completeSurvey(userId);
        superHostService.grantOnboardingRewardTicket(userId);

        AvatarProfileResponse avatarProfile = avatarProfileService.getProfile(avatarType);
        log.info("설문조사 제출 완료 - userId: {}, avatarType: {}", userId, avatarType);
        return SurveyResponse.of(scores, avatarType, survey.getRecordStyle().getStyleType(), avatarProfile);
    }

    public void skipSurvey(Long userId) {
        onboardingService.skipSurvey(userId);
        log.info("설문조사 스킵 - userId: {}", userId);
    }

    @Transactional(readOnly = true)
    public SurveyResponse getMySurveyResult(Long userId) {
        TravelTendency travelTendency = travelTendencyRepository.findByUserId(userId)
                .orElseThrow(SurveyResultNotFoundException::new);
        Survey survey = surveyRepository.getByUserIdOrThrow(userId);
        return SurveyResponse.from(travelTendency, survey.getRecordStyle().getStyleType(), avatarProfileService);
    }

    public SurveyResponse update(Long userId, SurveyRequest request) {
        User user = userRepository.getByIdOrThrow(userId);
        Survey survey = surveyRepository.getByUserIdOrThrow(userId);

        ParsedSurveyData parsed = surveyConverter.parseRequest(request);
        updateSurvey(survey, parsed);

        TendencyScoreResponse scores = tendencyCalculator.calculate(survey);
        AvatarType avatarType = matchAvatarType(scores);

        saveOrUpdateTravelTendency(user, scores, avatarType);
        updateProfileAvatar(userId, avatarType);

        AvatarProfileResponse avatarProfile = avatarProfileService.getProfile(avatarType);
        return SurveyResponse.of(scores, avatarType, survey.getRecordStyle().getStyleType(), avatarProfile);
    }

    private Survey createSurvey(User user, ParsedSurveyData parsed) {
        Survey survey = surveyConverter.toEntity(user, parsed);
        return surveyRepository.save(survey);
    }

    private void updateSurvey(Survey survey, ParsedSurveyData parsed) {
        survey.updateSurvey(
                parsed.rhythmQ1(),
                parsed.rhythmQ2(),
                parsed.rhythmQ3(),
                parsed.consumptionQ1(),
                parsed.consumptionQ2(),
                parsed.consumptionQ3(),
                parsed.energyQ1(),
                parsed.energyQ2(),
                parsed.energyQ3(),
                parsed.decisionQ1(),
                parsed.decisionQ2(),
                parsed.decisionQ3(),
                parsed.recordStyle(),
                parsed.activityTags()
        );
    }

    private void saveOrUpdateTravelTendency(User user, TendencyScoreResponse scores, AvatarType avatarType) {
        BigDecimal rhythm = toBigDecimal(scores.rhythmScore());
        BigDecimal energy = toBigDecimal(scores.energyScore());
        BigDecimal consumption = toBigDecimal(scores.consumptionScore());
        BigDecimal decision = toBigDecimal(scores.decisionScore());

        travelTendencyRepository.findByUserId(user.getId())
                .ifPresentOrElse(
                        existing -> existing.update(rhythm, energy, consumption, decision, avatarType),
                        () -> travelTendencyRepository.save(
                                TravelTendency.create(user, rhythm, energy, consumption, decision, avatarType)
                        )
                );
    }

    private void updateProfileAvatar(Long userId, AvatarType avatarType) {
        avatarProfileRepository.findByAvatarType(avatarType)
                .ifPresentOrElse(
                        avatarProfile -> profileManagementService.updateAvatar(userId, avatarProfile.getId()),
                        () -> log.warn("아바타 프로필을 찾을 수 없어 Profile에 저장하지 않음 - userId: {}, avatarType: {}", userId, avatarType)
                );
    }

    private AvatarType matchAvatarType(TendencyScoreResponse scores) {
        return avatarMatcher.match(
                scores.rhythmScore(),
                scores.energyScore(),
                scores.consumptionScore(),
                scores.decisionScore()
        );
    }

    private BigDecimal toBigDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP);
    }
}
