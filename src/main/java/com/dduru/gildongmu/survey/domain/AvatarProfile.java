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

    @Column(name = "speech_bubble_text", nullable = false, length = 200)
    private String speechBubbleText;

    @Column(name = "description_line_1", nullable = false, columnDefinition = "TEXT")
    private String descriptionLine1;

    @Column(name = "description_line_2", nullable = false, columnDefinition = "TEXT")
    private String descriptionLine2;

    @Column(name = "description_line_3", nullable = false, columnDefinition = "TEXT")
    private String descriptionLine3;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(nullable = false, columnDefinition = "JSON")
    private String tags;

    @Builder
    public AvatarProfile(
            AvatarType avatarType,
            String displayName,
            String speechBubbleText,
            String descriptionLine1,
            String descriptionLine2,
            String descriptionLine3,
            String imageUrl,
            String tags
    ) {
        this.avatarType = avatarType;
        this.displayName = displayName;
        this.speechBubbleText = speechBubbleText;
        this.descriptionLine1 = descriptionLine1;
        this.descriptionLine2 = descriptionLine2;
        this.descriptionLine3 = descriptionLine3;
        this.imageUrl = imageUrl;
        this.tags = tags;
    }
}
