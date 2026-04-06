package com.dduru.gildongmu.survey.service;

import com.dduru.gildongmu.survey.domain.SurveyQuestion;
import com.dduru.gildongmu.survey.domain.SurveyQuestionOption;
import com.dduru.gildongmu.survey.dto.response.SurveyQuestionListResponse;
import com.dduru.gildongmu.survey.dto.response.SurveyQuestionOptionResponse;
import com.dduru.gildongmu.survey.dto.response.SurveyQuestionResponse;
import com.dduru.gildongmu.survey.repository.SurveyQuestionOptionRepository;
import com.dduru.gildongmu.survey.repository.SurveyQuestionRepository;
import lombok.RequiredArgsConstructor;
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

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SurveyQuestionService {

    /**
     * 설문 질문/옵션 응답 포맷이 바뀌어
     * 클라이언트가 다른 방식으로 해석해야 할 때 증가시키는 계약 버전
     */
    private static final int QUESTIONNAIRE_VERSION = 2;
    private static final String CACHE_NAME = "surveyQuestions";
    private static final String EMPTY_FINGERPRINT = "v0";

    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyQuestionOptionRepository surveyQuestionOptionRepository;
    private final CacheManager cacheManager;

    public SurveyQuestionListResponse getSurveyQuestions() {
        String cacheKey = buildCacheKey();

        SurveyQuestionListResponse cached = readFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        SurveyQuestionListResponse response = loadSurveyQuestionsFromDb();
        writeToCache(cacheKey, response);
        return response;
    }
    
    private String buildCacheKey() {
        return "db:v" + QUESTIONNAIRE_VERSION + ":" + resolveCacheFingerprint();
    }

    private Cache getCacheOrNull() {
        return cacheManager.getCache(CACHE_NAME);
    }

    private SurveyQuestionListResponse readFromCache(String cacheKey) {
        Cache cache = getCacheOrNull();
        return cache == null ? null : cache.get(cacheKey, SurveyQuestionListResponse.class);
    }

    private void writeToCache(String cacheKey, SurveyQuestionListResponse response) {
        Cache cache = getCacheOrNull();
        if (cache != null) {
            cache.put(cacheKey, response);
        }
    }

    private SurveyQuestionListResponse loadSurveyQuestionsFromDb() {
        List<SurveyQuestion> questions = surveyQuestionRepository.findAllByOrderByDisplayOrderAsc();

        List<String> questionIds = questions.stream()
                .map(SurveyQuestion::getQuestionId)
                .toList();

        Map<String, List<SurveyQuestionOptionResponse>> optionsByQuestionId =
                loadOptionsByQuestionId(questionIds);

        List<SurveyQuestionResponse> questionResponses = questions.stream()
                .map(question -> toQuestionResponse(question, optionsByQuestionId))
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
                question.getType(),
                question.isRequired(),
                question.getMinSelect(),
                question.getMaxSelect(),
                optionsByQuestionId.getOrDefault(question.getQuestionId(), List.of())
        );
    }

    private Map<String, List<SurveyQuestionOptionResponse>> loadOptionsByQuestionId(List<String> questionIds) {
        /**
         * 질문이 없으면 옵션 조회도 불필요하므로 바로 빈 맵 반환
         */
        if (questionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<SurveyQuestionOption> options =
                surveyQuestionOptionRepository.findOptionsByQuestionIds(questionIds);

        return options.stream()
                .collect(Collectors.groupingBy(
                        option -> option.getQuestion().getQuestionId(),
                        Collectors.mapping(
                                option -> new SurveyQuestionOptionResponse(
                                        option.getCode(),
                                        option.getIcon(),
                                        option.getText()
                                ),
                                Collectors.toList()
                        )
                ));
    }

    /**
     * DB의 데이터 변경 여부를 감지하기 위한 지문(Fingerprint) 생성
     */
    private String resolveCacheFingerprint() {
        LocalDateTime questionMaxModifiedAt = surveyQuestionRepository.findMaxModifiedAt();
        LocalDateTime optionMaxModifiedAt = surveyQuestionOptionRepository.findMaxModifiedAt();
        long questionCount = surveyQuestionRepository.count();
        long optionCount = surveyQuestionOptionRepository.count();

        LocalDateTime latestModifiedAt = resolveLatestModifiedAt(questionMaxModifiedAt, optionMaxModifiedAt);

        if (latestModifiedAt == null) {
            return EMPTY_FINGERPRINT;
        }

        return latestModifiedAt.truncatedTo(ChronoUnit.SECONDS)
                + "_" + questionCount
                + "_" + optionCount;
    }

    private LocalDateTime resolveLatestModifiedAt(
            LocalDateTime questionMaxModifiedAt,
            LocalDateTime optionMaxModifiedAt
    ) {
        if (optionMaxModifiedAt == null) {
            return questionMaxModifiedAt;
        }
        if (questionMaxModifiedAt == null || optionMaxModifiedAt.isAfter(questionMaxModifiedAt)) {
            return optionMaxModifiedAt;
        }
        return questionMaxModifiedAt;
    }
}
