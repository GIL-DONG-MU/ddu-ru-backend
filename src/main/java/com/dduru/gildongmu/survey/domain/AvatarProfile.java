package com.dduru.gildongmu.survey.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "avatar_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AvatarProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "avatar_type", nullable = false, unique = true)
    private AvatarType avatarType;

    @Column(name = "display_name", nullable = false, length = 64)
    private String displayName;

    @Column(name = "description", nullable = false, length = 200)
    private String oneLineDescription;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(nullable = false, columnDefinition = "JSON")
    private String tags;

    @Builder
    public AvatarProfile(
            AvatarType avatarType,
            String displayName,
            String oneLineDescription,
            String body,
            String imageUrl,
            String tags
    ) {
        this.avatarType = avatarType;
        this.displayName = displayName;
        this.oneLineDescription = oneLineDescription;
        this.body = body;
        this.imageUrl = imageUrl;
        this.tags = tags;
    }
}
