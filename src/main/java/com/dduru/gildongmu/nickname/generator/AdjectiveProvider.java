package com.dduru.gildongmu.nickname.generator;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 닉네임 생성에 사용되는 형용사를 제공하는 클래스
 */
@Component
public class AdjectiveProvider {

    private final Random random = new Random();
    private final List<String> adjectives;

    public AdjectiveProvider() {
        this.adjectives = initializeAdjectives();
    }

    private List<String> initializeAdjectives() {
        return Arrays.asList(
                // 여행 관련
                "용감한", "씩씩한", "자유로운", "열정적인", "모험적인",
                "낭만적인", "활기찬", "여유로운", "설레는", "떠나는",
                // 자연 관련
                "푸른", "맑은", "시원한", "따뜻한", "포근한",
                "상쾌한", "고요한", "평화로운", "싱그러운", "빛나는",
                // 감정/성격 관련
                "귀여운", "사랑스런", "깜찍한", "지혜로운", "영리한",
                "다정한", "행복한", "즐거운", "유쾌한", "친절한",
                // 맛 관련
                "달콤한", "맛있는", "신선한", "향긋한", "고소한",
                // 우주/판타지 관련
                "반짝이는", "신비로운", "찬란한", "마법의", "전설의"
        );
    }

    /**
     * 랜덤 형용사를 반환합니다.
     */
    public String getRandomAdjective() {
        return adjectives.get(random.nextInt(adjectives.size()));
    }
}
