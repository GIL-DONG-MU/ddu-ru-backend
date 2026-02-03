package com.dduru.gildongmu.profile.service;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.dto.NicknameRandomResponse;
import com.dduru.gildongmu.profile.dto.NicknameUpdateRequest;
import com.dduru.gildongmu.profile.dto.NicknameValidateResponse;
import com.dduru.gildongmu.profile.repository.ProfileRepository;
import com.dduru.gildongmu.profile.utils.NicknameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class NicknameService {

    private static final int NICKNAME_MAX_RETRY_ATTEMPTS = 10;
    private static final String FALLBACK_NICKNAME_PREFIX = "뚜비";
    private static final int FALLBACK_RANDOM_BOUND = 10000;

    private final NicknameGenerator nicknameGenerator;
    private final ProfileRepository profileRepository;

    @Transactional
    public void updateNickname(Long userId, NicknameUpdateRequest request) {
        Profile profile = profileRepository.getByUserIdOrThrow(userId);
        checkDuplicateNicknameWithLock(request.nickname());

        profile.updateNickname(request.nickname());
    }

    @Transactional(readOnly = true)
    public NicknameValidateResponse checkNickname(String nickname) {
        checkDuplicateNickname(nickname);

        return NicknameValidateResponse.builder()
                .sanitizedNickname(nickname)
                .build();
    }

    /**
     * 중복되지 않는 유니크한 닉네임을 생성합니다.
     * 형용사 + 명사 조합에 랜덤 숫자를 추가하여 고유성을 보장합니다.
     * DB에 중복이 있으면 새로운 랜덤 숫자로 재시도합니다.
     *
     * @return 유니크한 닉네임 (예: "용감한여행자1234")
     */
    @Transactional(readOnly = true)
    public NicknameRandomResponse generateRandomNickname() {

        for (int attempt = 0; attempt < NICKNAME_MAX_RETRY_ATTEMPTS; attempt++) {
            String baseNickname = nicknameGenerator.generateBaseNickname();
            int randomNumber = nicknameGenerator.generateRandomNumber();
            String nickname = baseNickname + randomNumber;

            if (!profileRepository.existsByNickname(nickname)) {
                log.info("랜덤 닉네임 생성: {}", nickname);
                return NicknameRandomResponse.of(nickname);
            }

            log.debug("해당 닉네임이 이미 존재합니다: {}, 새로운 숫자를 부여하겠습니다.", nickname);
        }

        String fallbackNickname = FALLBACK_NICKNAME_PREFIX + (System.currentTimeMillis() % FALLBACK_RANDOM_BOUND);
        log.warn("유니크한 닉네임 생성에 {}회 실패하여 대체 닉네임 사용: {}", NICKNAME_MAX_RETRY_ATTEMPTS, fallbackNickname);
        return NicknameRandomResponse.of(fallbackNickname);
    }

    private void checkDuplicateNickname(String nickname) {
        if (profileRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_TAKEN);
        }
    }

    private void checkDuplicateNicknameWithLock(String nickname) {
        if (profileRepository.existsByNicknameWithLock(nickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_TAKEN);
        }
    }
}
