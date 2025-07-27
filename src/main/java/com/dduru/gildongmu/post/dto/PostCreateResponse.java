package com.dduru.gildongmu.post.dto;

import com.dduru.gildongmu.auth.domain.User;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.enums.Destination;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PostCreateResponse {
    private Long id;
    private UserInfo user;
    private DestinationInfo destination;
    private String title;
    private String content;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer recruitCapacity;
    private Integer recruitCount;
    private LocalDate recruitDeadline;
    private String preferredGender;
    private String preferredAgeMin;
    private String preferredAgeMax;
    private Integer budgetMin;
    private Integer budgetMax;
    private List<String> photoUrls;
    private List<String> tags;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean isRecruitmentClosed;
    private boolean isTravelStarted;
    private boolean isTravelEnded;

    public static PostCreateResponse of(Post post, User user, Destination destination, List<String> photoUrls, List<String> tags) {
        return PostCreateResponse.builder()
                .id(post.getId())
                .user(UserInfo.from(user))
                .destination(DestinationInfo.from(destination))
                .title(post.getTitle())
                .content(post.getContent())
                .startDate(post.getStartDate())
                .endDate(post.getEndDate())
                .recruitCapacity(post.getRecruitCapacity())
                .recruitCount(post.getRecruitCount())
                .recruitDeadline(post.getRecruitDeadline())
                .preferredGender(post.getPreferredGender().name())
                .preferredAgeMin(post.getPreferredAgeMin() != null ? post.getPreferredAgeMin().name() : null)
                .preferredAgeMax(post.getPreferredAgeMax() != null ? post.getPreferredAgeMax().name() : null)
                .budgetMin(post.getBudgetMin())
                .budgetMax(post.getBudgetMax())
                .photoUrls(photoUrls)
                .tags(tags)
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .isRecruitmentClosed(post.isRecruitmentClosed())
                .isTravelStarted(post.isTravelStarted())
                .isTravelEnded(post.isTravelEnded())
                .build();
    }
}
