package com.dduru.gildongmu.participation.repository;

import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import com.dduru.gildongmu.participation.dto.response.ParticipationRetrieveResponse;

import java.util.List;

public interface ParticipationRepositoryCustom {
    List<ParticipationRetrieveResponse> findReceivedRequestsByStatus(
            Long postOwnerId,
            ParticipationStatus status
    );
}
