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
 * 닉네임 생성에 사용되는 명사를 제공하는 클래스
 */
@Component
public class NounProvider {

    private final Random random = new Random();
    private final Map<NicknameTheme, List<String>> nounsByTheme;

    public NounProvider() {
        this.nounsByTheme = initializeNouns();
    }

    private Map<NicknameTheme, List<String>> initializeNouns() {
        Map<NicknameTheme, List<String>> map = new EnumMap<>(NicknameTheme.class);

        // 여행 테마 명사
        map.put(NicknameTheme.TRAVEL, Arrays.asList(
                "여행자", "모험가", "탐험가", "방랑자", "나그네",
                "길동무", "순례자", "배낭객", "도보여행자", "세계일주자",
                "항해사", "파일럿", "여행작가", "사진가", "캠퍼",
                "히치하이커", "백패커", "노마드", "트래블러", "익스플로러"
        ));

        // 자연 테마 명사
        map.put(NicknameTheme.NATURE, Arrays.asList(
                "하늘", "바람", "구름", "산", "바다",
                "숲", "나무", "꽃", "별", "달",
                "강", "호수", "들판", "초원", "해변",
                "계곡", "폭포", "석양", "노을", "새벽"
        ));

        // 동물 테마 명사
        map.put(NicknameTheme.ANIMAL, Arrays.asList(
                "고양이", "강아지", "토끼", "곰돌이", "다람쥐",
                "펭귄", "부엉이", "여우", "사슴", "판다",
                "코알라", "수달", "햄스터", "치타", "독수리",
                "돌고래", "해달", "알파카", "라쿤", "미어캣"
        ));

        // 음식 테마 명사
        map.put(NicknameTheme.FOOD, Arrays.asList(
                "떡볶이", "붕어빵", "호떡", "마카롱", "푸딩",
                "초콜릿", "쿠키", "케이크", "빵", "도넛",
                "아이스크림", "젤리", "솜사탕", "크레페", "와플",
                "타르트", "크로와상", "머핀", "슈크림", "카스테라"
        ));

        // 우주 테마 명사
        map.put(NicknameTheme.SPACE, Arrays.asList(
                "별", "달", "태양", "행성", "은하",
                "혜성", "유성", "성운", "블랙홀", "우주",
                "오로라", "별똥별", "화성", "토성", "목성",
                "북극성", "시리우스", "오리온", "안드로메다", "카시오페아"
        ));

        // 판타지 테마 명사
        map.put(NicknameTheme.FANTASY, Arrays.asList(
                "마법사", "기사", "요정", "드래곤", "유니콘",
                "피닉스", "그리핀", "엘프", "드루이드", "현자",
                "수호자", "예언자", "정령", "천사", "수호신",
                "마녀", "연금술사", "성기사", "궁수", "힐러"
        ));

        return Collections.unmodifiableMap(map);
    }

    /**
     * 특정 테마에서 랜덤 명사를 반환합니다.
     */
    public String getRandomNoun(NicknameTheme theme) {
        if (theme == NicknameTheme.RANDOM) {
            return getRandomNounFromAllThemes();
        }

        List<String> nouns = nounsByTheme.get(theme);
        return nouns.get(random.nextInt(nouns.size()));
    }

    /**
     * 모든 테마에서 랜덤 명사를 반환합니다.
     */
    private String getRandomNounFromAllThemes() {
        NicknameTheme[] themes = NicknameTheme.values();
        NicknameTheme randomTheme;

        do {
            randomTheme = themes[random.nextInt(themes.length)];
        } while (randomTheme == NicknameTheme.RANDOM);

        return getRandomNoun(randomTheme);
    }

    /**
     * 특정 테마의 모든 명사를 반환합니다.
     */
    public List<String> getNouns(NicknameTheme theme) {
        if (theme == NicknameTheme.RANDOM) {
            return nounsByTheme.values().stream()
                    .flatMap(List::stream)
                    .toList();
        }
        return nounsByTheme.get(theme);
    }
}
