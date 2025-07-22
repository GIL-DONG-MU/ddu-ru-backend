package com.dduru.gildongmu.auth.repository;

import com.dduru.gildongmu.auth.domain.User;
import com.dduru.gildongmu.auth.enums.OauthType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByOauthIdAndOauthType(String oauthId, OauthType oauthType);
    Optional<User> findByEmail(String email);
    boolean existsByOauthIdAndOauthType(String oauthId, OauthType oauthType);
    boolean existsByEmail(String email);
}
