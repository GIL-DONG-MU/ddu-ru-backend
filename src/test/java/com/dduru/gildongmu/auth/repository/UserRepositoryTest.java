package com.dduru.gildongmu.auth.repository;

import com.dduru.gildongmu.auth.domain.User;
import com.dduru.gildongmu.auth.enums.AgeRange;
import com.dduru.gildongmu.auth.enums.Gender;
import com.dduru.gildongmu.auth.enums.OauthType;
import com.dduru.gildongmu.config.QueryDslConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void 사용자_저장_및_조회_테스트() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .name("테스트사용자")
                .profileImage("http://example.com/profile.jpg")
                .oauthId("12345")
                .oauthType(OauthType.KAKAO)
                .gender(Gender.M)
                .ageRange(AgeRange.AGE_20s)
                .phoneNumber("010-1234-5678")
                .build();

        // when
        User savedUser = userRepository.save(user);

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
                .profileImage("http://example.com/profile.jpg")
                .oauthId("67890")
                .oauthType(OauthType.GOOGLE)
                .gender(Gender.F)
                .ageRange(AgeRange.AGE_30s)
                .build();
        
        userRepository.save(user);

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
                .profileImage("http://example.com/profile.jpg")
                .oauthId("oauth123")
                .oauthType(OauthType.KAKAO)
                .gender(Gender.M)
                .ageRange(AgeRange.AGE_20s)
                .build();
        
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByOauthIdAndOauthType("oauth123", OauthType.KAKAO);

        // then
        assertThat(exists).isTrue();
    }
}
