package com.dduru.gildongmu.survey.repository;

import com.dduru.gildongmu.survey.domain.AvatarProfile;
import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import com.dduru.gildongmu.survey.exception.AvatarProfileNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AvatarProfileRepository extends JpaRepository<AvatarProfile, Long> {
    Optional<AvatarProfile> findByAvatarType(AvatarType avatarType);

    default AvatarProfile getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(AvatarProfileNotFoundException::new);
    }
}
