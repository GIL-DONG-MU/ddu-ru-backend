package com.dduru.gildongmu.recommendation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TravelPreferenceUpdateRequest(
        @NotNull List<@NotNull @Valid DestinationPreferenceRequest> destinationPreferences,
        @NotNull List<@NotNull @Valid AvailableDateRequest> availableDates
) {
}
