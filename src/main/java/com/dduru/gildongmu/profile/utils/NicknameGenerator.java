package com.dduru.gildongmu.profile.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 랜덤 닉네임 생성 서비스
 * <p>
 * 형용사 + 명사 조합에 랜덤 숫자를 추가하여 유니크한 닉네임을 생성합니다.
 * DB에 중복이 있으면 새로운 랜덤 숫자로 재시도합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class NicknameGenerator {

    private static final int RANDOM_NUMBER_MIN = 1000;
    private static final int RANDOM_NUMBER_MAX = 9999;

    private final NicknameAdjectiveProvider nicknameAdjectiveProvider;
    private final NicknameNounProvider nicknameNounProvider;

    /**
     * 1000~9999 범위의 랜덤 숫자 생성
     */
    public int generateRandomNumber() {
        return ThreadLocalRandom.current().nextInt(RANDOM_NUMBER_MIN, RANDOM_NUMBER_MAX + 1);
    }

    /**
     * 형용사 + 명사 조합의 기본 닉네임 생성
     */
    public String generateBaseNickname() {
        String adjective = nicknameAdjectiveProvider.getRandomAdjective();
        String noun = nicknameNounProvider.getRandomNoun();
        return adjective + " " + noun;
    }
}
