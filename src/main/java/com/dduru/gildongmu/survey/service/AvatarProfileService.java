package com.dduru.gildongmu.survey.service;

import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.survey.domain.AvatarProfile;
import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import com.dduru.gildongmu.survey.dto.response.AvatarProfileResponse;
import com.dduru.gildongmu.survey.exception.AvatarProfileNotFoundException;
import com.dduru.gildongmu.survey.repository.AvatarProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvatarProfileService {

    private final AvatarProfileRepository avatarProfileRepository;
    private final JsonConverter jsonConverter;

    @Cacheable(value = "avatarProfiles", key = "#avatarType.name()")
    public AvatarProfileResponse getProfile(AvatarType avatarType) {
        AvatarProfile profile = avatarProfileRepository.findByAvatarType(avatarType)
                .orElseThrow(AvatarProfileNotFoundException::new);

        List<String> tags = jsonConverter.convertJsonToList(profile.getTags());
        return new AvatarProfileResponse(
                profile.getDisplayName(),
                profile.getOneLineDescription(),
                tags,
                profile.getBody(),
                profile.getImageUrl()
        );
    }
}
