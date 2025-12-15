package com.dduru.gildongmu.user.utils;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class NicknameNounProvider {

    private final Random random = new Random();
    private final List<String> nouns;

    public NicknameNounProvider() {
        this.nouns = initializeNouns();
    }

    private List<String> initializeNouns() {
        return Arrays.asList(
                "배낭", "지도", "여권", "티켓", "카메라",
                "모자", "우산", "운동화", "텀블러", "노트",
                "이어폰", "목베개", "파우치", "선글라스", "수첩",
                "지갑", "충전기", "손수건", "슬리퍼", "에코백",
                "칫솔", "치약", "샴푸", "린스", "바디워시",
                "폼클렌징", "면도기", "물티슈", "휴지", "선크림",
                "밴드", "셀카봉", "삼각대", "양말", "지퍼백",
                "잠옷", "스낵", "음료수", "책", "잡지"
        );
    }

    public String getRandomNoun() {
        return nouns.get(random.nextInt(nouns.size()));
    }
}
