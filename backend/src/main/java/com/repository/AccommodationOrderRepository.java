package com.repository;

import com.model.entity.AccommodationOrder;

import java.util.Optional;

public interface AccommodationOrderRepository {

    void save(AccommodationOrder order);

    Optional<AccommodationOrder> findByRoomId(String roomId);
}
