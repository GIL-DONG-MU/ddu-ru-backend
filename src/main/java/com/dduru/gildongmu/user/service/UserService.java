package com.dduru.gildongmu.user.service;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.dto.NicknameRandomResponse;
import com.dduru.gildongmu.user.dto.UserCheckNicknameResponse;
import com.dduru.gildongmu.user.dto.UserUpdateNicknameRequest;
import com.dduru.gildongmu.user.repository.UserRepository;
import com.dduru.gildongmu.user.utils.NicknameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private static final int NICKNAME_MAX_RETRY_ATTEMPTS = 10;

    private final NicknameGenerator nicknameGenerator;
    private final UserRepository userRepository;

    @Transactional
    public void updateNickname(Long userId, UserUpdateNicknameRequest request) {
        User user = userRepository.getByIdOrThrow(userId);
        checkDuplicateNickname(request.nickname());

        user.updateNickname(request.nickname());
    }

    @Transactional(readOnly = true)
    public UserCheckNicknameResponse checkNickname(String nickname) {
        checkDuplicateNickname(nickname);

        return UserCheckNicknameResponse.builder()
                .sanitizedNickname(nickname)
                .build();
    }

    private void checkDuplicateNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_TAKEN);
        }
    }

    /**
     * 중복되지 않는 유니크한 닉네임을 생성합니다.
     * 형용사 + 명사 조합에 랜덤 숫자를 추가하여 고유성을 보장합니다.
     * DB에 중복이 있으면 새로운 랜덤 숫자로 재시도합니다.
     *
     * @return 유니크한 닉네임 (예: "용감한 여행자1234")
     */
    @Transactional(readOnly = true)
    public NicknameRandomResponse generateRandomNickname() {
        String baseNickname = nicknameGenerator.generateBaseNickname();

        for (int attempt = 0; attempt < NICKNAME_MAX_RETRY_ATTEMPTS; attempt++) {
            int randomNumber = nicknameGenerator.generateRandomNumber();
            String nickname = baseNickname + randomNumber;

            if (!userRepository.existsByNickname(nickname)) {
                log.info("랜덤 닉네임 생성: {}", nickname);
                return NicknameRandomResponse.of(nickname);
            }

            log.debug("해당 닉네임이 이미 존재합니다: {}, 새로운 숫자를 부여하겠습니다.", nickname);
        }

        String fallbackNickname = baseNickname + (System.currentTimeMillis() % 10000);
        log.warn("유니크한 닉네임 생성에 {}회 실패하여 대체 닉네임 사용: {}", NICKNAME_MAX_RETRY_ATTEMPTS, fallbackNickname);
        return NicknameRandomResponse.of(fallbackNickname);
    }
}
