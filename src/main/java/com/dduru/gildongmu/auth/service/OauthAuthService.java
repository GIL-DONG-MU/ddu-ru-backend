package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.domain.User;
import com.dduru.gildongmu.auth.dto.LoginResponse;
import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.enums.AgeRange;
import com.dduru.gildongmu.auth.enums.Gender;
import com.dduru.gildongmu.auth.repository.UserRepository;
import com.dduru.gildongmu.common.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OauthAuthService {

    private final OauthFactory oauthFactory;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public String getAuthorizationUrl(String provider) {
        OauthService oauthService = oauthFactory.getOauthService(provider);
        return oauthService.getAuthorizationUrl();
    }

    public LoginResponse processLogin(String provider, String code) {
        OauthService oauthService = oauthFactory.getOauthService(provider);

        String accessToken = oauthService.getAccessToken(code);

        OauthUserInfo oauthUserInfo = oauthService.getUserInfo(accessToken);

        User user = findOrCreateUser(oauthUserInfo);

        String jwtToken = jwtTokenProvider.createToken(user.getId().toString(), user.getEmail());

        return LoginResponse.builder()
                .accessToken(jwtToken)
                .name(user.getName())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .ageRange(user.getAgeRange() != null ? user.getAgeRange().name() : null)
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    private User findOrCreateUser(OauthUserInfo oauthUserInfo) {
        return userRepository.findByOauthIdAndOauthType(
                oauthUserInfo.oauthId(),
                oauthUserInfo.loginType()
        ).orElseGet(() -> {
            Gender gender = Gender.from(oauthUserInfo.gender());
            AgeRange ageRange = AgeRange.from(oauthUserInfo.ageRange());

            User newUser = User.builder()
                    .email(oauthUserInfo.email())
                    .name(oauthUserInfo.name())
                    .profileImage(oauthUserInfo.profileImage())
                    .oauthId(oauthUserInfo.oauthId())
                    .oauthType(oauthUserInfo.loginType())
                    .gender(gender)
                    .ageRange(ageRange)
                    .phoneNumber(oauthUserInfo.phoneNumber())
                    .build();

            return userRepository.save(newUser);
        });
    }
}
