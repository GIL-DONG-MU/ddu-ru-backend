package com.dduru.gildongmu.auth.repository;

import com.dduru.gildongmu.common.config.QueryDslConfig;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.repository.ProfileRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import com.dduru.gildongmu.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Test
    void 사용자_저장_및_조회_테스트() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .name("테스트사용자")
                .oauthId("12345")
                .oauthType(OauthType.KAKAO)
                .build();

        // when
        User savedUser = userRepository.save(user);
        
        Profile profile = new Profile(savedUser);
        profileRepository.save(profile);

        // then
        assertThat(savedUser.getId()).isNotNull();
    }

    @Test
    void 이메일로_사용자_조회_테스트() {
        // given
        User user = User.builder()
                .email("find@example.com")
                .name("찾을사용자")
                .oauthId("67890")
                .oauthType(OauthType.GOOGLE)
                .build();
        
        User savedUser = userRepository.save(user);
        
        Profile profile = new Profile(savedUser);
        profile.setupInitialProfile(Gender.F, "01011111111", LocalDate.of(1990, 1, 1));
        profile.updateProfile("users", "http://example.com/profile.jpg", ProfileImageType.UPLOADED, null, null);
        profileRepository.save(profile);

        // when
        boolean exists = userRepository.existsByEmail("find@example.com");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void OAuth_정보로_사용자_존재_확인_테스트() {
        // given
        User user = User.builder()
                .email("oauth@example.com")
                .name("OAuth사용자")
                .oauthId("oauth123")
                .oauthType(OauthType.KAKAO)
                .build();
        
        User savedUser = userRepository.save(user);
        
        Profile profile = new Profile(savedUser);
        profile.setupInitialProfile(Gender.M, null, null);
        profile.updateProfile("users", null, ProfileImageType.AVATAR, null, null);
        profileRepository.save(profile);

        // when
        boolean exists = userRepository.existsByOauthIdAndOauthType("oauth123", OauthType.KAKAO);

        // then
        assertThat(exists).isTrue();
    }
}
