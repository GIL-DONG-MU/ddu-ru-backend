package com.dduru.gildongmu.onboarding.repository;

import com.dduru.gildongmu.onboarding.domain.UserOnboarding;
import com.dduru.gildongmu.onboarding.exception.UserOnboardingNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserOnboardingRepository extends JpaRepository<UserOnboarding, Long> {
    Optional<UserOnboarding> findByUser_Id(Long userId);

    default UserOnboarding getByUserIdOrThrow(Long userId) {
        return findByUser_Id(userId)
                .orElseThrow(() -> UserOnboardingNotFoundException.of(userId));
    }
}
