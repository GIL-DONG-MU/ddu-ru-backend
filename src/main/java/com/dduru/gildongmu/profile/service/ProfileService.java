package com.dduru.gildongmu.profile.service;

import com.dduru.gildongmu.profile.dto.NicknameRandomResponse;
import com.dduru.gildongmu.profile.dto.NicknameUpdateRequest;
import com.dduru.gildongmu.profile.dto.NicknameValidateResponse;
import com.dduru.gildongmu.profile.dto.ProfileSetupRequest;
import com.dduru.gildongmu.profile.dto.request.ProfileUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @deprecated 이 클래스는 더 이상 사용되지 않습니다.
 * 대신 다음의 특화된 서비스들을 사용하세요:
 * - {@link NicknameService} : 닉네임 관련 기능
 * - {@link ProfileSetupService} : 온보딩 및 프로필 초기 설정
 * - {@link ProfileManagementService} : 프로필 업데이트 및 관리
 */
@Deprecated
@Slf4j
@RequiredArgsConstructor
@Service
public class ProfileService {

    private final NicknameService nicknameService;
    private final ProfileSetupService profileSetupService;
    private final ProfileManagementService profileManagementService;

    @Deprecated
    public void updateNickname(Long userId, NicknameUpdateRequest request) {
        nicknameService.updateNickname(userId, request);
    }

    @Deprecated
    public NicknameValidateResponse checkNickname(String nickname) {
        return nicknameService.checkNickname(nickname);
    }

    @Deprecated
    public NicknameRandomResponse generateRandomNickname() {
        return nicknameService.generateRandomNickname();
    }

    @Deprecated
    public void setupInitialProfile(Long userId, ProfileSetupRequest request) {
        profileSetupService.setupInitialProfile(userId, request);
    }

    @Deprecated
    public void updateProfile(Long userId, ProfileUpdateRequest request) {
        profileManagementService.updateProfile(userId, request);
    }

    @Deprecated
    public void updateAvatar(Long userId, Long avatarId) {
        profileManagementService.updateAvatar(userId, avatarId);
    }
}
