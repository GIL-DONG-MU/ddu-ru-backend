package com.dduru.gildongmu.auth.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.Period;

@Slf4j
public final class AgeCalculator {

    private AgeCalculator() {}

    public static String calculateAgeRange(LocalDate birthday) {
        if (birthday == null) {
            log.debug("생년월일이 null입니다.");
            return null;
        }

        try {
            int age = Period.between(birthday, LocalDate.now()).getYears();
            log.debug("생년월일: {}, 계산된 나이: {}세", birthday, age);

            String ageRange = switch (age / 10) {
                case 0, 1 -> "10~19";
                case 2 -> "20~29";
                case 3 -> "30~39";
                case 4 -> "40~49";
                case 5 -> "50~59";
                default -> "60~69";
            };

            log.info("연령대 계산 완료: {}세 → {}", age, ageRange);
            return ageRange;

        } catch (Exception e) {
            log.error("연령대 계산 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }

    public static LocalDate createBirthday(Integer year, Integer month, Integer day) {
        if (year == null || month == null || day == null) {
            log.debug("생년월일 정보 불완전: year={}, month={}, day={}", year, month, day);
            return null;
        }

        try {
            LocalDate birthday = LocalDate.of(year, month, day);
            log.debug("생년월일 생성 성공: {}", birthday);
            return birthday;
        } catch (Exception e) {
            log.error("생년월일 생성 실패: year={}, month={}, day={}, error={}",
                    year, month, day, e.getMessage());
            return null;
        }
    }
}
