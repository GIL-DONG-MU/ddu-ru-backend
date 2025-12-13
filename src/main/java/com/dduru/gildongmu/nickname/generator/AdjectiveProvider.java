package com.dduru.gildongmu.nickname.generator;

import com.dduru.gildongmu.nickname.enums.NicknameTheme;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 닉네임 생성에 사용되는 형용사를 제공하는 클래스
 */
@Component
public class AdjectiveProvider {

    private final Random random = new Random();
    private final Map<NicknameTheme, List<String>> adjectivesByTheme;

    public AdjectiveProvider() {
        this.adjectivesByTheme = initializeAdjectives();
    }

    private Map<NicknameTheme, List<String>> initializeAdjectives() {
        Map<NicknameTheme, List<String>> map = new EnumMap<>(NicknameTheme.class);

        // 여행 테마 형용사
        map.put(NicknameTheme.TRAVEL, Arrays.asList(
                "용감한", "씩씩한", "자유로운", "열정적인", "호기심많은",
                "모험적인", "낭만적인", "활기찬", "여유로운", "설레는",
                "떠나는", "방랑하는", "탐험하는", "발걸음가벼운", "길위의"
        ));

        // 자연 테마 형용사
        map.put(NicknameTheme.NATURE, Arrays.asList(
                "푸른", "맑은", "시원한", "따뜻한", "포근한",
                "상쾌한", "고요한", "평화로운", "싱그러운", "청량한",
                "향기로운", "찬란한", "빛나는", "영롱한", "무지개빛"
        ));

        // 동물 테마 형용사
        map.put(NicknameTheme.ANIMAL, Arrays.asList(
                "귀여운", "사랑스런", "깜찍한", "용맹한", "날렵한",
                "지혜로운", "충직한", "영리한", "재빠른", "느긋한",
                "장난꾸러기", "다정한", "포동포동", "복슬복슬", "말랑말랑"
        ));

        // 음식 테마 형용사
        map.put(NicknameTheme.FOOD, Arrays.asList(
                "달콤한", "맛있는", "신선한", "바삭한", "촉촉한",
                "고소한", "새콤한", "향긋한", "담백한", "쫄깃한",
                "부드러운", "아삭한", "진한", "풍미있는", "입맛당기는"
        ));

        // 우주 테마 형용사
        map.put(NicknameTheme.SPACE, Arrays.asList(
                "반짝이는", "신비로운", "무한한", "광활한", "영원한",
                "찬란한", "빛나는", "미지의", "우아한", "장엄한",
                "몽환적인", "별빛가득", "은하수같은", "초월적인", "눈부신"
        ));

        // 판타지 테마 형용사
        map.put(NicknameTheme.FANTASY, Arrays.asList(
                "마법의", "전설의", "신성한", "고대의", "불멸의",
                "신비한", "환상적인", "기적의", "영웅적인", "전설속",
                "비밀스런", "신화속", "마력의", "축복받은", "운명의"
        ));

        return Collections.unmodifiableMap(map);
    }

    /**
     * 특정 테마에서 랜덤 형용사를 반환합니다.
     */
    public String getRandomAdjective(NicknameTheme theme) {
        if (theme == NicknameTheme.RANDOM) {
            return getRandomAdjectiveFromAllThemes();
        }

        List<String> adjectives = adjectivesByTheme.get(theme);
        return adjectives.get(random.nextInt(adjectives.size()));
    }

    /**
     * 모든 테마에서 랜덤 형용사를 반환합니다.
     */
    private String getRandomAdjectiveFromAllThemes() {
        NicknameTheme[] themes = NicknameTheme.values();
        NicknameTheme randomTheme;

        do {
            randomTheme = themes[random.nextInt(themes.length)];
        } while (randomTheme == NicknameTheme.RANDOM);

        return getRandomAdjective(randomTheme);
    }

    /**
     * 특정 테마의 모든 형용사를 반환합니다.
     */
    public List<String> getAdjectives(NicknameTheme theme) {
        if (theme == NicknameTheme.RANDOM) {
            return adjectivesByTheme.values().stream()
                    .flatMap(List::stream)
                    .toList();
        }
        return adjectivesByTheme.get(theme);
    }
}
