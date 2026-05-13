package com.dduru.gildongmu.home.dto.response;

import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record HomeResponse(
        UpcomingTripResponse upcomingTrip,
        PopularDestinationsResponse popularDestinations,
        MateRecommendationResponse mateRecommendation,
        List<SuperHostResponse> superHosts,
        List<SameDestinationTripResponse> sameDestinationTrips,
        List<SameAgeTripResponse> sameAgeTrips
) {

    public record UpcomingTripResponse(
            Long journeyId,
            String title,
            int dDay,
            LocalDate startDate,
            LocalDate endDate,
            int currentMemberCount,
            int maxMemberCount,
            int pendingTaskCount
    ) {
    }

    public record PopularDestinationsResponse(
            LocalDateTime updateDateTime,
            List<PopularDestinationResponse> destinations
    ) {
    }

    public record PopularDestinationResponse(
            int rank,
            String regionName,
            int waitingMateCount
    ) {
    }

    public record MateRecommendationResponse(
            boolean isAvailable,
            int remainingFreeCount,
            List<MateRecommendationItemResponse> recommendations
    ) {
    }

    public record MateRecommendationItemResponse(
            Long recommendationId,
            Long postId,
            int matchPercentage,
            String title,
            String location,
            LocalDate startDate,
            LocalDate endDate,
            HostResponse host,
            int currentMemberCount,
            int maxMemberCount,
            String description,
            List<String> tags
    ) {
    }

    public record SuperHostResponse(
            Long postId,
            PostStatus status,
            String title,
            String location,
            LocalDate startDate,
            LocalDate endDate,
            int currentMemberCount,
            int maxMemberCount,
            List<String> tags,
            HostResponse host,
            int viewCount,
            String thumbnailUrl
    ) {
    }

    public record SameDestinationTripResponse(
            Long postId,
            String title,
            String location,
            LocalDate startDate,
            LocalDate endDate,
            int currentMemberCount,
            int maxMemberCount,
            String thumbnailUrl
    ) {
    }

    public record SameAgeTripResponse(
            Long postId,
            String title,
            String location,
            LocalDate startDate,
            int currentMemberCount,
            int maxMemberCount,
            String thumbnailUrl
    ) {
    }

    public record HostResponse(
            String nickname,
            ProfileImageInfoResponse profileImageInfo,
            int age,
            Gender gender
    ) {
    }

    public record ProfileImageInfoResponse(
            ProfileImageType profileImageType,
            String profileImageUrl,
            Long bgColorId
    ) {
    }
}
