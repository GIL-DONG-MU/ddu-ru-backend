package com.dduru.gildongmu.recommendation.support;

import com.dduru.gildongmu.common.exception.JsonConvertException;
import com.dduru.gildongmu.recommendation.dto.result.RecommendationReason;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationReasonJsonConverter {

    private final ObjectMapper objectMapper;

    public String toJson(List<RecommendationReason> reasons) {
        try {
            return objectMapper.writeValueAsString(reasons == null ? List.of() : reasons);
        } catch (JsonProcessingException e) {
            log.error("추천 이유 JSON 직렬화 실패", e);
            throw new JsonConvertException();
        }
    }

    public List<RecommendationReason> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.error("추천 이유 JSON 역직렬화 실패 - json={}", json, e);
            throw new JsonConvertException();
        }
    }
}
