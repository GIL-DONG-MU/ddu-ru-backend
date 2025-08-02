package com.dduru.gildongmu.auth.utils;

import com.dduru.gildongmu.auth.dto.google.PersonExtendedInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class GoogleUserInfoExtractor {

    public PersonExtendedInfo extractPersonInfo(Map<String, Object> response) {
        if (response == null) {
            log.warn("People API 응답이 null입니다.");
            return PersonExtendedInfo.empty();
        }

        log.debug("개인정보 추출 시작 - 응답 키: {}", response.keySet());

        String gender = extractGender(response);
        String ageRange = extractAgeRangeFromBirthday(response);
        String phoneNumber = extractPhoneNumber(response);

        log.info("추출 완료 - Gender: {}, AgeRange: {}, PhoneNumber: {}",
                gender, ageRange, phoneNumber != null ? "있음" : "없음");

        return new PersonExtendedInfo(gender, ageRange, phoneNumber);
    }

    @SuppressWarnings("unchecked")
    private String extractAgeRangeFromBirthday(Map<String, Object> response) {
        log.debug("생년월일 정보 추출 시작");

        return extractBirthdayData(response)
                .flatMap(this::createBirthDate)
                .map(AgeCalculator::calculateAgeRange)
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private Optional<Map<String, Object>> extractBirthdayData(Map<String, Object> response) {
        try {
            List<Map<String, Object>> birthdays = (List<Map<String, Object>>) response.get("birthdays");

            if (birthdays == null || birthdays.isEmpty()) {
                log.warn("birthdays 정보가 없습니다.");
                return Optional.empty();
            }

            Map<String, Object> birthday = birthdays.get(0);
            Map<String, Object> date = (Map<String, Object>) birthday.get("date");

            return Optional.ofNullable(date);
        } catch (Exception e) {
            log.error("생년월일 정보 추출 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<LocalDate> createBirthDate(Map<String, Object> date) {
        try {
            Integer year = (Integer) date.get("year");
            Integer month = (Integer) date.get("month");
            Integer day = (Integer) date.get("day");

            log.info("Google 생년월일: {}-{}-{}", year, month, day);

            return Optional.ofNullable(AgeCalculator.createBirthday(year, month, day));
        } catch (Exception e) {
            log.error("생년월일 변환 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private String extractGender(Map<String, Object> response) {
        return OauthResponseUtils.extractFieldFromResponse(response, "genders", "value")
                .map(this::normalizeGender)
                .orElse(null);
    }

    private String extractPhoneNumber(Map<String, Object> response) {
        return OauthResponseUtils.extractFieldFromResponse(response, "phoneNumbers", "value")
                .orElse(null);
    }

    private String normalizeGender(String googleGender) {
        if (googleGender == null) return null;
        return switch (googleGender.toLowerCase()) {
            case "male" -> "male";
            case "female" -> "female";
            default -> null;
        };
    }
}
