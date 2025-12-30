package com.dduru.gildongmu.survey.service;

import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.survey.domain.AvatarProfile;
import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import com.dduru.gildongmu.survey.exception.AvatarProfileNotFoundException;
import com.dduru.gildongmu.survey.repository.AvatarProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvatarProfileService {

    private final AvatarProfileRepository avatarProfileRepository;
    private final JsonConverter jsonConverter;

    @Cacheable(value = "avatarProfiles", key = "#avatarType.name()")
    public AvatarProfileResponse getProfile(AvatarType avatarType) {
        log.debug("아바타 프로필 조회 - avatarType: {}", avatarType);

        AvatarProfile profile = avatarProfileRepository.findByAvatarType(avatarType)
                .orElseThrow(() -> {
                    log.error("아바타 프로필을 찾을 수 없음 - avatarType: {}", avatarType);
                    return AvatarProfileNotFoundException.of(avatarType);
                });

        List<String> tags = jsonConverter.convertJsonToList(profile.getTags());

        log.debug("아바타 프로필 조회 완료 - avatarType: {}", avatarType);
        return new AvatarProfileResponse(
                profile.getDescription(),
                profile.getPersonality(),
                profile.getStrength(),
                profile.getTip(),
                tags
        );
    }

    public record AvatarProfileResponse(String description, String personality, String strength, String tip, List<String> tags) {
    }
}
