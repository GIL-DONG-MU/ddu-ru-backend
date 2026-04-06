package com.dduru.gildongmu.participation.repository;

import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import com.dduru.gildongmu.participation.dto.query.ParticipationRetrieveQueryResult;

import java.util.List;

public interface ParticipationRepositoryCustom {
    List<ParticipationRetrieveQueryResult> findReceivedRequestsByStatus(
            Long postOwnerId,
            ParticipationStatus status
    );
}
