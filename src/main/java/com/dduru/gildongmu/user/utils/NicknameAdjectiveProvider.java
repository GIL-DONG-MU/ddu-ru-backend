package com.dduru.gildongmu.user.utils;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class NicknameAdjectiveProvider {

    private final List<String> adjectives;

    public NicknameAdjectiveProvider() {
        this.adjectives = initializeAdjectives();
    }

    private List<String> initializeAdjectives() {
        return Arrays.asList(
                // 여행
                "용감한", "씩씩한", "자유로운", "열정적인", "모험적인",
                "낭만적인", "활기찬", "여유로운", "설레는", "떠나는",
                "길잃은", "헤매는", "느긋한", "천천히가는", "돌아가는",
                "우연한", "쉬어가는", "그냥가는", "잠깐멈춘", "계획없는",
                // 자연
                "푸른", "맑은", "시원한", "따뜻한", "포근한",
                "상쾌한", "고요한", "평화로운", "싱그러운", "빛나는",
                // 분위기
                "낯선", "이국적인", "몽환적인", "감성적인", "힙한",
                "고풍스러운","탁트인", "그림같은", "아기자기한",
                // 감정/성격
                "귀여운", "사랑스런", "깜찍한", "지혜로운", "영리한",
                "다정한", "행복한", "즐거운", "유쾌한", "친절한",
                "즉흥적인", "거침없는", "호기심많은", "부지런한",
                "알뜰한", "엉뚱한",
                // 맛
                "달콤한", "맛있는", "신선한", "향긋한", "고소한",
                // 우주/판타지
                "반짝이는", "신비로운", "찬란한", "마법의", "전설의",
                // 동행 느낌
                "울창한", "황홀한", "다채로운", "든든한", "소중한",
                "그리운", "끈끈한", "오붓한"
        );
    }

    public String getRandomAdjective() {
        return adjectives.get(ThreadLocalRandom.current().nextInt(adjectives.size()));
    }
}
