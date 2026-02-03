package com.dduru.gildongmu.auth.repository;

import java.util.Optional;

public interface RefreshTokenRepository {

    void save(Long userId, String refreshToken);

    Optional<String> findByUserId(Long userId);

    boolean delete(Long userId);

    void extendExpiration(Long userId);
}
