package com.dduru.gildongmu.fcm.service;

import com.dduru.gildongmu.fcm.dto.request.FcmTokenDeleteRequest;
import com.dduru.gildongmu.fcm.dto.request.FcmTokenRegisterRequest;
import com.dduru.gildongmu.fcm.repository.UserFcmTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FcmTokenService {

    private final UserFcmTokenRepository userFcmTokenRepository;

    public void register(Long userId, FcmTokenRegisterRequest request) {
        userFcmTokenRepository.upsert(userId, request.token(), request.deviceType().name());
    }

    public void delete(Long userId, FcmTokenDeleteRequest request) {
        userFcmTokenRepository.deleteByUser_IdAndToken(userId, request.token());
    }
}
