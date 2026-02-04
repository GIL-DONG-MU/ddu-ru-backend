package com.dduru.gildongmu.survey.service;

import com.dduru.gildongmu.survey.domain.SurveyQuestion;
import com.dduru.gildongmu.survey.domain.SurveyQuestionOption;
import com.dduru.gildongmu.survey.dto.response.SurveyQuestionListResponse;
import com.dduru.gildongmu.survey.dto.response.SurveyQuestionOptionResponse;
import com.dduru.gildongmu.survey.dto.response.SurveyQuestionResponse;
import com.dduru.gildongmu.survey.repository.SurveyQuestionOptionRepository;
import com.dduru.gildongmu.survey.repository.SurveyQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SurveyQuestionService {

    private static final int QUESTIONNAIRE_VERSION = 1;
    private static final String CACHE_NAME = "surveyQuestions";

    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyQuestionOptionRepository surveyQuestionOptionRepository;
    private final CacheManager cacheManager;

    public SurveyQuestionListResponse getSurveyQuestions() {
        log.debug("설문 문항 리스트 조회 시작");

        String cacheKey = buildCacheKey();
        SurveyQuestionListResponse cached = getCached(cacheKey);
        if (cached != null) {
            log.debug("설문 문항 리스트 캐시 히트 - cacheKey: {}", cacheKey);
            return cached;
        }

        SurveyQuestionListResponse response = fetchFromDb();
        putCache(cacheKey, response);

        log.info("설문 문항 리스트 조회 완료 - count: {}, cacheKey: {}", response.count(), cacheKey);
        return response;
    }

    private String buildCacheKey() {
        return "db:" + resolveDbVersion();
    }

    private SurveyQuestionListResponse getCached(String cacheKey) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            return null;
        }
        return cache.get(cacheKey, SurveyQuestionListResponse.class);
    }

    private void putCache(String cacheKey, SurveyQuestionListResponse response) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            return;
        }
        cache.put(cacheKey, response);
    }

    private SurveyQuestionListResponse fetchFromDb() {
        List<SurveyQuestion> questions = surveyQuestionRepository.findAllByOrderByDisplayOrderAsc();
        List<String> questionIds = questions.stream()
                .map(SurveyQuestion::getQuestionId)
                .toList();

        Map<String, List<SurveyQuestionOptionResponse>> optionsByQuestionId = resolveOptions(questionIds);
        List<SurveyQuestionResponse> questionResponses = questions.stream()
                .map(q -> toQuestionResponse(q, optionsByQuestionId))
                .toList();

        return new SurveyQuestionListResponse(
                QUESTIONNAIRE_VERSION,
                questionResponses.size(),
                questionResponses
        );
    }

    private SurveyQuestionResponse toQuestionResponse(
            SurveyQuestion question,
            Map<String, List<SurveyQuestionOptionResponse>> optionsByQuestionId
    ) {
        return new SurveyQuestionResponse(
                question.getQuestionId(),
                question.getDisplayOrder(),
                question.getQuestionText(),
                question.getImageUrl(),
                question.getType().name(),
                question.isRequired(),
                question.getMinSelect(),
                question.getMaxSelect(),
                optionsByQuestionId.getOrDefault(question.getQuestionId(), List.of())
        );
    }

    private Map<String, List<SurveyQuestionOptionResponse>> resolveOptions(List<String> questionIds) {
        if (questionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<SurveyQuestionOption> options =
                surveyQuestionOptionRepository.findOptionsByQuestionIds(questionIds);

        return options.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getQuestion().getQuestionId(),
                        Collectors.mapping(
                                o -> new SurveyQuestionOptionResponse(o.getCode(), o.getIcon(), o.getText()),
                                Collectors.toList()
                        )
                ));
    }

    private String resolveDbVersion() {
        LocalDateTime qMax = surveyQuestionRepository.findMaxModifiedAt();
        LocalDateTime oMax = surveyQuestionOptionRepository.findMaxModifiedAt();
        long qCount = surveyQuestionRepository.count();
        long oCount = surveyQuestionOptionRepository.count();

        LocalDateTime max = qMax;
        if (oMax != null && (max == null || oMax.isAfter(max))) {
            max = oMax;
        }

        if (max == null) {
            return "v0";
        }

        return max.truncatedTo(ChronoUnit.SECONDS).toString() + "_" + qCount + "_" + oCount;
    }
}
