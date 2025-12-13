package com.dduru.gildongmu.nickname.service;

import com.dduru.gildongmu.nickname.generator.AdjectiveProvider;
import com.dduru.gildongmu.nickname.generator.NounProvider;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

/**
 * 랜덤 닉네임 생성 서비스
 * <p>
 * 형용사 + 명사 조합으로 유니크한 닉네임을 생성합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class NicknameGeneratorService {

    private static final int MAX_RETRY_COUNT = 50;
    private static final int NUMBER_SUFFIX_MAX = 999;
    private static final int MAX_NICKNAME_LENGTH = 12;

    private final AdjectiveProvider adjectiveProvider;
    private final NounProvider nounProvider;
    private final UserRepository userRepository;
    private final Random random = new Random();

    /**
     * 중복되지 않는 유니크한 닉네임을 생성합니다.
     * 중복 시 숫자를 추가하여 재시도합니다.
     *
     * @return 유니크한 닉네임
     */
    @Transactional(readOnly = true)
    public String generateUniqueNickname() {
        int retryCount = 0;

        while (retryCount < MAX_RETRY_COUNT) {
            boolean includeNumber = retryCount > 10; // 10회 이상 실패 시 숫자 추가
            String nickname = generateNickname(includeNumber);

            if (nickname.length() <= MAX_NICKNAME_LENGTH && isNicknameAvailable(nickname)) {
                log.info("Generated unique nickname: {}", nickname);
                return nickname;
            }

            retryCount++;
        }

        // 최후의 수단: 타임스탬프 기반 닉네임
        return "여행자" + System.currentTimeMillis() % 100000;
    }

    /**
     * 형용사 + 명사 조합의 닉네임을 생성합니다.
     */
    private String generateNickname(boolean includeNumber) {
        String adjective = adjectiveProvider.getRandomAdjective();
        String noun = nounProvider.getRandomNoun();

        StringBuilder nickname = new StringBuilder();
        nickname.append(adjective).append(" ").append(noun);

        if (includeNumber) {
            nickname.append(random.nextInt(NUMBER_SUFFIX_MAX) + 1);
        }

        return nickname.toString();
    }

    /**
     * 닉네임 사용 가능 여부를 확인합니다.
     */
    private boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }
}
