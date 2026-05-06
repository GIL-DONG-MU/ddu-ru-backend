package com.dduru.gildongmu.journey.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.post.domain.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "journeys")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Journey extends BaseTimeEntity {
    /**
     * 공개 모집글과 1:1로 연결되는 나의 여정 워크스페이스.
     * 현재는 제목/대표 사진만 독립적으로 관리하고, 나머지 여행 정보는 post를 참조한다.
     */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    private Post post;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "photo_url", columnDefinition = "TEXT")
    private String photoUrl;

    @Builder(access = AccessLevel.PRIVATE)
    private Journey(
            Post post,
            String title,
            String photoUrl
    ) {
        this.post = post;
        this.title = title;
        this.photoUrl = photoUrl;
    }

    public static Journey create(Post post) {
        // 생성 시점에만 공개 모집글의 제목/대표 사진을 초기값으로 복사한다.
        return Journey.builder()
                .post(post)
                .title(post.getTitle())
                .photoUrl(post.getPhotoUrl())
                .build();
    }

    public void updateBasicInfo(String title, String photoUrl) {
        if (title != null) {
            this.title = title;
        }
        if (photoUrl != null) {
            this.photoUrl = photoUrl;
        }
    }
}
