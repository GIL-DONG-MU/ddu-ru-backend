package com.dduru.gildongmu.nickname.service;

import com.dduru.gildongmu.nickname.dto.NicknameGenerateRequest;
import com.dduru.gildongmu.nickname.dto.NicknameGenerateResponse;
import com.dduru.gildongmu.nickname.enums.NicknameTheme;
import com.dduru.gildongmu.nickname.generator.AdjectiveProvider;
import com.dduru.gildongmu.nickname.generator.NounProvider;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 랜덤 닉네임 생성 서비스
 * <p>
 * 형용사 + 명사 조합으로 유니크한 닉네임을 생성합니다.
 * 선택적으로 숫자 접미사를 추가할 수 있습니다.
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
     * 요청에 따라 랜덤 닉네임을 생성합니다.
     *
     * @param request 닉네임 생성 요청 (개수, 테마, 숫자 포함 여부)
     * @return 생성된 닉네임 목록
     */
    @Transactional(readOnly = true)
    public NicknameGenerateResponse generateNicknames(NicknameGenerateRequest request) {
        Set<String> generatedNicknames = new HashSet<>();
        int retryCount = 0;

        while (generatedNicknames.size() < request.count() && retryCount < MAX_RETRY_COUNT) {
            String nickname = generateSingleNickname(request.theme(), request.includeNumber());

            // 닉네임 길이 검증 (12자 이하)
            if (nickname.length() <= MAX_NICKNAME_LENGTH && isNicknameAvailable(nickname)) {
                generatedNicknames.add(nickname);
            }

            retryCount++;
        }

        List<String> result = new ArrayList<>(generatedNicknames);
        log.info("Generated {} nicknames with theme: {}", result.size(), request.theme());

        return NicknameGenerateResponse.of(result);
    }

    /**
     * 기본 설정으로 단일 닉네임을 생성합니다.
     *
     * @return 생성된 닉네임
     */
    @Transactional(readOnly = true)
    public String generateSingleNickname() {
        return generateSingleNickname(NicknameTheme.RANDOM, false);
    }

    /**
     * 테마에 맞는 단일 닉네임을 생성합니다.
     *
     * @param theme         닉네임 테마
     * @param includeNumber 숫자 접미사 포함 여부
     * @return 생성된 닉네임
     */
    public String generateSingleNickname(NicknameTheme theme, boolean includeNumber) {
        String adjective = adjectiveProvider.getRandomAdjective(theme);
        String noun = nounProvider.getRandomNoun(theme);

        StringBuilder nickname = new StringBuilder();
        nickname.append(adjective).append(" ").append(noun);

        if (includeNumber) {
            nickname.append(generateRandomNumberSuffix());
        }

        return nickname.toString();
    }

    /**
     * 중복되지 않는 유니크한 닉네임을 생성합니다.
     * 중복 시 숫자를 추가하여 재시도합니다.
     *
     * @param theme 닉네임 테마
     * @return 유니크한 닉네임
     */
    @Transactional(readOnly = true)
    public String generateUniqueNickname(NicknameTheme theme) {
        int retryCount = 0;

        while (retryCount < MAX_RETRY_COUNT) {
            boolean includeNumber = retryCount > 10; // 10회 이상 실패 시 숫자 추가
            String nickname = generateSingleNickname(theme, includeNumber);

            if (nickname.length() <= MAX_NICKNAME_LENGTH && isNicknameAvailable(nickname)) {
                return nickname;
            }

            retryCount++;
        }

        // 최후의 수단: 타임스탬프 기반 닉네임
        return "여행자" + System.currentTimeMillis() % 100000;
    }

    /**
     * 닉네임 사용 가능 여부를 확인합니다.
     *
     * @param nickname 확인할 닉네임
     * @return 사용 가능 여부
     */
    private boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    /**
     * 랜덤 숫자 접미사를 생성합니다.
     *
     * @return 1~999 사이의 랜덤 숫자
     */
    private int generateRandomNumberSuffix() {
        return random.nextInt(NUMBER_SUFFIX_MAX) + 1;
    }
}
