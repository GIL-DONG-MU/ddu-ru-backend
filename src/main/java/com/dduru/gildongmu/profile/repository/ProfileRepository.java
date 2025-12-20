package com.dduru.gildongmu.profile.repository;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUser(User user);
    
    Optional<Profile> findByUser_Id(Long userId);
    
    boolean existsByNickname(String nickname);
    
    boolean existsByPhoneNumber(String phoneNumber);
}

