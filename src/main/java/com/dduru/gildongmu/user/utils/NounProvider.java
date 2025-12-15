package com.dduru.gildongmu.user.utils;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class NounProvider {

    private final Random random = new Random();
    private final List<String> nouns;

    public NounProvider() {
        this.nouns = initializeNouns();
    }

    private List<String> initializeNouns() {
        return Arrays.asList(
                // 여행 관련
                "여행자", "모험가", "탐험가", "방랑자", "나그네",
                "길동무", "배낭객", "트래블러", "노마드",
                // 자연 관련
                "하늘", "바람", "구름", "바다", "숲",
                "별", "달", "강", "꽃", "나무",
                // 동물 관련
                "고양이", "강아지", "토끼", "곰돌이", "다람쥐",
                "펭귄", "여우", "사슴", "판다", "수달",
                // 음식 관련
                "마카롱", "푸딩", "쿠키", "케이크", "도넛",
                // 판타지 관련
                "마법사", "기사", "요정", "드래곤", "유니콘"
        );
    }

    public String getRandomNoun() {
        return nouns.get(random.nextInt(nouns.size()));
    }
}
