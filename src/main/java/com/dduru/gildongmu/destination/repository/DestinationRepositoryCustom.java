package com.dduru.gildongmu.destination.repository;

import com.dduru.gildongmu.destination.domain.Destination;

import java.util.List;

public interface DestinationRepositoryCustom {
    List<Destination> searchByKeyword(String keyword);
}
