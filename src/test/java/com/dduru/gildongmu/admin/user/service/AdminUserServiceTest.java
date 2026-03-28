package com.dduru.gildongmu.admin.user.service;

import com.dduru.gildongmu.admin.user.dto.response.AdminUserDetailResponse;
import com.dduru.gildongmu.admin.user.dto.response.AdminUserListResponse;
import com.dduru.gildongmu.auth.exception.UserNotFoundException;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import com.dduru.gildongmu.user.domain.enums.Role;
import com.dduru.gildongmu.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserService 테스트")
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserService adminUserService;

    @DisplayName("사용자 목록 조회 시 페이지/요약 정보가 반환된다")
    @Test
    void findAll_returnsPagedUserSummaries() {
        Pageable pageable = PageRequest.of(0, 20);
        User user = createUserWithProfile(1L, "admin@dduru.com", "관리자", "길동", Role.ADMIN);
        Page<User> page = new PageImpl<>(List.of(user), pageable, 1);
        when(userRepository.findAllWithProfile(pageable)).thenReturn(page);

        AdminUserListResponse response = adminUserService.findAll(pageable);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).id()).isEqualTo(1L);
        assertThat(response.content().get(0).nickname()).isEqualTo("길동");
        assertThat(response.totalElements()).isEqualTo(1);
        verify(userRepository).findAllWithProfile(pageable);
    }

    @DisplayName("사용자 상세 조회 시 기본 정보와 프로필 요약이 반환된다")
    @Test
    void findById_returnsUserDetail() {
        Long userId = 7L;
        User user = createUserWithProfile(userId, "user@dduru.com", "일반유저", "홍길동", Role.USER);
        when(userRepository.findWithProfileById(userId)).thenReturn(Optional.of(user));

        AdminUserDetailResponse response = adminUserService.findById(userId);

        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("user@dduru.com");
        assertThat(response.nickname()).isEqualTo("홍길동");
        assertThat(response.profile()).isNotNull();
        assertThat(response.profile().gender()).isEqualTo(Gender.M);
    }

    @DisplayName("존재하지 않는 사용자 상세 조회 시 예외가 발생한다")
    @Test
    void findById_whenUserNotFound_throwsUserNotFoundException() {
        Long userId = 999L;
        when(userRepository.findWithProfileById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.findById(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    private User createUserWithProfile(Long id, String email, String name, String nickname, Role role) {
        User user = User.builder()
                .email(email)
                .name(name)
                .oauthId("kakao-" + id)
                .oauthType(OauthType.KAKAO)
                .role(role)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.now());

        Profile profile = new Profile(user);
        ReflectionTestUtils.setField(profile, "nickname", nickname);
        ReflectionTestUtils.setField(profile, "gender", Gender.M);
        ReflectionTestUtils.setField(profile, "birthday", LocalDate.of(1999, 1, 1));
        ReflectionTestUtils.setField(profile, "bio", "한 줄 소개");
        ReflectionTestUtils.setField(profile, "profileImageType", ProfileImageType.AVATAR);
        ReflectionTestUtils.setField(user, "profile", profile);
        return user;
    }
}
