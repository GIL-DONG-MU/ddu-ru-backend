package com.dduru.gildongmu.auth.repository;

import com.dduru.gildongmu.config.QueryDslConfig;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.repository.ProfileRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.enums.OauthType;
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
        
        Profile profile = Profile.builder()
                .user(savedUser)
                .uploadedImageUrl("http://example.com/profile.jpg")
                .profileImageType(ProfileImageType.UPLOADED)
                .gender(Gender.M)
                .phoneNumber("010-1234-5678")
                .build();
        profileRepository.save(profile);

        // then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getName()).isEqualTo("테스트사용자");
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
        
        Profile profile = Profile.builder()
                .user(savedUser)
                .nickname("users")
                .gender(Gender.F)
                .phoneNumber("01011111111")
                .birthday(LocalDate.of(1990, 1, 1))
                .uploadedImageUrl("http://example.com/profile.jpg")
                .profileImageType(ProfileImageType.UPLOADED)
                .build();
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
        
        Profile profile = Profile.builder()
                .user(savedUser)
                .avatarId(1L)
                .profileImageType(ProfileImageType.AVATAR)
                .gender(Gender.M)
                .build();
        profileRepository.save(profile);

        // when
        boolean exists = userRepository.existsByOauthIdAndOauthType("oauth123", OauthType.KAKAO);

        // then
        assertThat(exists).isTrue();
    }
}
