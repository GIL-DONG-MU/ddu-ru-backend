package com.dduru.gildongmu.user.utils;

import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

/**
 * 랜덤 닉네임 생성 서비스
 * <p>
 * 형용사 + 명사 조합에 랜덤 숫자를 추가하여 유니크한 닉네임을 생성합니다.
 * DB에 중복이 있으면 새로운 랜덤 숫자로 재시도합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class NicknameGeneratorService {

    private static final int RANDOM_NUMBER_MIN = 1000;
    private static final int RANDOM_NUMBER_MAX = 9999;
    private static final int MAX_RETRY_ATTEMPTS = 10; // 최대 재시도 횟수

    private final Random random = new Random();
    private final AdjectiveProvider adjectiveProvider;
    private final NounProvider nounProvider;
    private final UserRepository userRepository;

    /**
     * 중복되지 않는 유니크한 닉네임을 생성합니다.
     * 형용사 + 명사 조합에 랜덤 숫자를 추가하여 고유성을 보장합니다.
     * DB에 중복이 있으면 새로운 랜덤 숫자로 재시도합니다.
     *
     * @return 유니크한 닉네임 (예: "용감한 여행자1234")
     */
    @Transactional(readOnly = true)
    public String generateRandomNickname() {
        String baseNickname = generateBaseNickname();
        
        for (int attempt = 0; attempt < MAX_RETRY_ATTEMPTS; attempt++) {
            int randomNumber = generateRandomNumber();
            String nickname = baseNickname + randomNumber;
            
            if (!userRepository.existsByNickname(nickname)) {
                log.info("Generated nickname: {}", nickname);
                return nickname;
            }
            
            log.debug("Nickname already exists: {}, retrying with new random number", nickname);
        }
        
        // 최대 재시도 횟수 초과 시 타임스탬프 기반 닉네임 반환
        String fallbackNickname = baseNickname + (System.currentTimeMillis() % 10000);
        log.warn("Failed to generate unique nickname after {} attempts, using fallback: {}", MAX_RETRY_ATTEMPTS, fallbackNickname);
        return fallbackNickname;
    }
    
    /**
     * 1000~9999 범위의 랜덤 숫자를 생성합니다.
     */
    private int generateRandomNumber() {
        return random.nextInt(RANDOM_NUMBER_MAX - RANDOM_NUMBER_MIN + 1) + RANDOM_NUMBER_MIN;
    }

    /**
     * 형용사 + 명사 조합의 기본 닉네임을 생성합니다.
     */
    private String generateBaseNickname() {
        String adjective = adjectiveProvider.getRandomAdjective();
        String noun = nounProvider.getRandomNoun();
        return adjective + " " + noun;
    }
}
