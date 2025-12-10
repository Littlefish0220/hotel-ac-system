package com.repository;

import com.model.entity.AccommodationBill;

import java.util.Optional;

public interface AccommodationBillRepository {

    void save(AccommodationBill bill);

    Optional<AccommodationBill> findByRoomId(String roomId);
}
