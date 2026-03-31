package com.dduru.gildongmu.common.util;

import com.dduru.gildongmu.common.exception.JsonConvertException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonConverter {

    private final ObjectMapper objectMapper;

    public String convertListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("리스트를 JSON으로 변환 실패 - 리스트: {}", list, e);
            throw new JsonConvertException();
        }
    }

    public String convertTagListToJson(List<String> tags) {
        if (tags == null) {
            return null;
        }
        return convertListToJson(normalizeTagList(tags));
    }

    public List<String> normalizeTagList(List<String> tags) {
        if (tags == null) {
            return null;
        }
        return tags.stream().map(String::strip).toList();
    }

    public List<String> convertJsonToList(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("JSON을 리스트로 변환 실패 - JSON: {}", json, e);
            throw new JsonConvertException();
        }
    }
}
