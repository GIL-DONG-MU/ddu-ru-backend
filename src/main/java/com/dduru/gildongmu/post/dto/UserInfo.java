package com.dduru.gildongmu.post.dto;

import com.dduru.gildongmu.auth.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    private Long id;
    private String name;
    private String profileImage;
    private String gender;
    private String ageRange;

    public static UserInfo from(User user){
        return UserInfo.builder()
                .id(user.getId())
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .gender(user.getGender().name())
                .ageRange(user.getAgeRange().name())
                .build();
    }
}
