package com.dduru.gildongmu.fcm.repository;

import com.dduru.gildongmu.fcm.domain.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {

    @Modifying(clearAutomatically = true)
    @Query(value = """
            INSERT INTO user_fcm_tokens (user_id, token, device_type, created_at, modified_at)
            VALUES (:userId, :token, :deviceType, NOW(6), NOW(6))
            ON DUPLICATE KEY UPDATE token = VALUES(token), modified_at = NOW(6)
            """, nativeQuery = true)
    void upsert(@Param("userId") Long userId,
                @Param("token") String token,
                @Param("deviceType") String deviceType);

    void deleteByToken(String token);

    List<UserFcmToken> findAllByUserId(Long userId);

    List<UserFcmToken> findAllByUserIdIn(List<Long> userIds);

    void deleteByUser_IdAndToken(Long userId, String token);
}
